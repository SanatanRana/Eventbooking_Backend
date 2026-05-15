package com.ranadj.service.impl;

import com.ranadj.config.RazorpayConfig;
import com.ranadj.dto.PaymentOrderRequest;
import com.ranadj.dto.PaymentOrderResponse;
import com.ranadj.dto.PaymentResponse;
import com.ranadj.dto.PaymentVerificationRequest;
import com.ranadj.entity.*;
import com.ranadj.exception.ApiException;
import com.ranadj.mapper.PaymentMapper;
import com.ranadj.repository.BookingRepository;
import com.ranadj.repository.PaymentRepository;
import com.ranadj.repository.UserRepository;
import com.ranadj.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Payments and Razorpay checkout operations.
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              UserRepository userRepository,
                              PaymentMapper paymentMapper,
                              java.util.Optional<RazorpayClient> razorpayClient) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentMapper = paymentMapper;
        this.razorpayClient = razorpayClient.orElse(null);
    }

    @Override
    public PaymentOrderResponse createOrder(PaymentOrderRequest request, String userEmail) {
        // 1. Fetch User and Booking
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ApiException("Booking not found", HttpStatus.NOT_FOUND));

        // 2. Validate booking ownership (Admins can pay for any booking, Customers only for their own)
        if (user.getRole() != Role.ADMIN && !booking.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access Denied: You cannot create payment for this booking", HttpStatus.FORBIDDEN);
        }

        // 3. Prevent payments on cancelled or rejected bookings
        if (booking.getBookingStatus() == BookingStatus.CANCELLED || booking.getBookingStatus() == BookingStatus.REJECTED) {
            throw new ApiException("Cannot pay for a " + booking.getBookingStatus() + " booking", HttpStatus.BAD_REQUEST);
        }

        // 4. Check if already fully paid
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ApiException("This booking is already fully paid", HttpStatus.BAD_REQUEST);
        }

        // 5. Ensure payment amount doesn't exceed the remaining amount
        if (request.getAmount().compareTo(booking.getRemainingAmount()) > 0) {
            throw new ApiException("Payment amount exceeds remaining due amount of " + booking.getRemainingAmount(), HttpStatus.BAD_REQUEST);
        }

        String razorpayOrderId;

        // 6. Handle Sandbox Mock vs Live SDK integration
        if ("rzp_test_RanaDJFakeKey123".equals(keyId)) {
            // Generate realistic mock order ID
            razorpayOrderId = "order_MOCK_" + System.currentTimeMillis() + "_" + booking.getId();
        } else {
            try {
                if (razorpayClient == null) {
                    throw new ApiException("Razorpay Gateway client is uninitialized", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                // Prepare Razorpay Order request (amount must be in paise)
                JSONObject orderParams = new JSONObject();
                orderParams.put("amount", request.getAmount().multiply(new BigDecimal(100)).intValue());
                orderParams.put("currency", "INR");
                orderParams.put("receipt", "txn_booking_" + booking.getId());

                Order order = razorpayClient.orders.create(orderParams);
                razorpayOrderId = order.get("id");
            } catch (Exception e) {
                throw new ApiException("Failed to generate Razorpay order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // 7. Save Payment record in pending state
        Payment payment = Payment.builder()
                .booking(booking)
                .razorpayOrderId(razorpayOrderId)
                .amount(request.getAmount())
                .paymentStatus("PENDING")
                .build();

        paymentRepository.save(payment);

        return PaymentOrderResponse.builder()
                .razorpayOrderId(razorpayOrderId)
                .amount(request.getAmount())
                .currency("INR")
                .keyId(keyId)
                .build();
    }

    @Override
    public PaymentResponse verifyPayment(PaymentVerificationRequest request, String userEmail) {
        // 1. Fetch User
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        // 2. Fetch Payment record
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ApiException("Payment transaction not initiated for this order ID", HttpStatus.NOT_FOUND));

        Booking booking = payment.getBooking();

        // 3. Verify signature securely from backend (DO NOT trust frontend directly!)
        if (request.getRazorpayOrderId().startsWith("order_MOCK_")) {
            // Validate mock environment signatures
            if (!"mock_signature_approved".equals(request.getRazorpaySignature())) {
                throw new ApiException("Secure Signature verification failed: Mock token signature invalid!", HttpStatus.BAD_REQUEST);
            }
        } else {
            // Compute live Hmac-SHA256 signature
            try {
                String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
                String computedSignature = calculateHmacSha256(payload, keySecret);
                if (!computedSignature.equals(request.getRazorpaySignature())) {
                    throw new ApiException("Secure Signature verification failed: Payment is untrusted!", HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                throw new ApiException("Signature computation error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // 4. If signature is valid, update payment entity to SUCCESS
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI");
        payment.setPaymentStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 5. Update booking balance sheet and payment statuses dynamically
        BigDecimal newAdvance = booking.getAdvanceAmount().add(payment.getAmount());
        booking.setAdvanceAmount(newAdvance);
        booking.setRemainingAmount(booking.getTotalAmount().subtract(newAdvance));

        if (booking.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            booking.setPaymentStatus(PaymentStatus.PAID);
        } else {
            booking.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        bookingRepository.save(booking);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        List<Payment> payments;

        if (user.getRole() == Role.ADMIN) {
            // Admin sees only transactions for bookings linked to their own packages
            payments = paymentRepository.findByBookingDjPackageAdminId(user.getId());
        } else {
            // Customer only sees their own transactions
            payments = paymentRepository.findByBookingUserId(user.getId());
        }

        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled cleanup: Expires PENDING payments older than 30 minutes.
     * When a user starts checkout but abandons it, the PENDING row stays forever.
     * This marks them as EXPIRED so they stop polluting the ledger.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000) // every 5 minutes
    public void expireStalePendingPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<Payment> stalePayments = paymentRepository.findStalePendingPayments(cutoff);
        for (Payment p : stalePayments) {
            p.setPaymentStatus("EXPIRED");
        }
        if (!stalePayments.isEmpty()) {
            paymentRepository.saveAll(stalePayments);
        }
    }

    /**
     * Computes HMAC-SHA256 hash representation of payload for secure payment signature checks.
     */
    private String calculateHmacSha256(String data, String key) throws SignatureException {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : rawHmac) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new SignatureException("Failed to generate Secure HMAC Signature : " + e.getMessage());
        }
    }
}
