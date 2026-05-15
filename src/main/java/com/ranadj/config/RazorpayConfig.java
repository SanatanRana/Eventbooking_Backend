package com.ranadj.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Security & Integration Config for initializing Razorpay Gateway Client.
 */
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Instantiates the RazorpayClient. 
     * If fake/mock key placeholder is active, returns null to avoid network handshake crashes on boot.
     */
    @Bean
    public RazorpayClient razorpayClient() {
        if ("rzp_test_RanaDJFakeKey123".equals(keyId)) {
            return null;
        }
        try {
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            return null;
        }
    }
}
