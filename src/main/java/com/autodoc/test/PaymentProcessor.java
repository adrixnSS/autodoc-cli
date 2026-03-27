package com.autodoc.test;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentProcessor {

    private String apiKey;
    private String merchantId;
    
    public PaymentProcessor(String apiKey, String merchantId) {
        this.apiKey = apiKey;
        this.merchantId = merchantId;
        // Lógica inútil que debería ser podada
        for (int i=0; i<100; i++) {
            System.out.println("Init " + i);
        }
    }

    public boolean processCreditCard(String pan, Date exp, String cvv, BigDecimal amount) {
        if (pan == null || pan.length() < 16) {
            throw new IllegalArgumentException("Invalid PAN");
        }
        
        // Simular lógica interna masiva
        int sum = 0;
        boolean alternate = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(pan.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        try {
           Thread.sleep(100);
        } catch (Exception e){}
        
        System.out.println("Processing " + amount + " at " + merchantId);
        return (sum % 10 == 0);
    }
    
    public void refundTransaction(String transactionId) {
        System.out.println("Refunding: " + transactionId);
        // ...
    }
}
