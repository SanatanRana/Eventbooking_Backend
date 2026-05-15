package com.ranadj.controller;

import com.ranadj.dto.*;
import com.ranadj.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller exposing Payment Integration and Razorpay endpoints.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Creates a new secure Razorpay payment order for a booking.
     * Accessible by authenticated Customers and Admins.
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @Valid @RequestBody PaymentOrderRequest request,
            Principal principal) {

        PaymentOrderResponse order = paymentService.createOrder(request, principal.getName());
        ApiResponse<PaymentOrderResponse> response = ApiResponse.<PaymentOrderResponse>builder()
                .success(true)
                .message("Razorpay payment order created successfully")
                .data(order)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verifies payment signature securely and updates booking payment status.
     * Accessible by authenticated Customers and Admins.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request,
            Principal principal) {

        PaymentResponse verifiedPayment = paymentService.verifyPayment(request, principal.getName());
        ApiResponse<PaymentResponse> response = ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment verified and processed successfully")
                .data(verifiedPayment)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves transaction and payment history.
     * Accessible by authenticated Customers (returns own) and Admins (returns all).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(Principal principal) {
        List<PaymentResponse> history = paymentService.getPaymentHistory(principal.getName());
        ApiResponse<List<PaymentResponse>> response = ApiResponse.<List<PaymentResponse>>builder()
                .success(true)
                .message("Payment history retrieved successfully")
                .data(history)
                .build();
        return ResponseEntity.ok(response);
    }
}
