package com.spliteasy.backend.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponse markReceived(@PathVariable Long groupId,
                                        @Valid @RequestBody SettleRequest request,
                                        Authentication authentication) {
        return paymentService.markReceived(groupId, request, authentication.getName());
    }

    @GetMapping
    public List<PaymentResponse> list(@PathVariable Long groupId, Authentication authentication) {
        return paymentService.getPayments(groupId, authentication.getName());
    }
}