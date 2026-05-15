package com.ranadj.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Payload class containing signature fields needed for backend verification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerificationRequest {

    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;

    private String paymentMethod;
}
