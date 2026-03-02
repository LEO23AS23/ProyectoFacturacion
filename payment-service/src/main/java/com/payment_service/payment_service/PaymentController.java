package com.payment_service.payment_service;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/card")
    public Map<String, Object> processPayment(@RequestBody Map<String, Object> request) {
        return Map.of(
            "status", "approved",
            "message", "Pago procesado correctamente",
            "data", request
        );
    }
}