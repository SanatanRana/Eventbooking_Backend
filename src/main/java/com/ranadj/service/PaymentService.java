package com.ranadj.service;

import com.ranadj.dto.PaymentOrderRequest;
import com.ranadj.dto.PaymentOrderResponse;
import com.ranadj.dto.PaymentResponse;
import com.ranadj.dto.PaymentVerificationRequest;

import java.util.List;

/**
 * Service interface for managing Razorpay Payment processing, orders, signature verification, and history.
 */
public interface PaymentService {

    /**
     * Initiates a payment transaction by creating a secure Razorpay order.
     */
    PaymentOrderResponse createOrder(PaymentOrderRequest request, String userEmail);

    /**
     * Validates payment signatures securely from the backend and updates the booking payment status.
     */
    PaymentResponse verifyPayment(PaymentVerificationRequest request, String userEmail);

    /**
     * Fetches transaction/payment history. Customers see their own, Admins can see all.
     */
    List<PaymentResponse> getPaymentHistory(String userEmail);
}
