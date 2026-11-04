package com.spliteasy.backend.balance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/balances")
    public List<BalanceResponse> balances(@PathVariable Long groupId,
                                          Authentication authentication) {
        return balanceService.getBalances(groupId, authentication.getName());
    }

    @GetMapping("/settlements")
    public List<TransferResponse> settlements(@PathVariable Long groupId,
                                              Authentication authentication) {
        return balanceService.getSimplifiedTransfers(groupId, authentication.getName());
    }
}

