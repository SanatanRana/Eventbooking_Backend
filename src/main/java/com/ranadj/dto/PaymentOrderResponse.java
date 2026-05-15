package com.ranadj.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Payload class containing created Razorpay order details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
}
