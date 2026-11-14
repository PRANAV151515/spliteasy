package com.spliteasy.backend.payment;

import com.spliteasy.backend.balance.BalanceService;
import com.spliteasy.backend.balance.TransferResponse;
import com.spliteasy.backend.group.Group;
import com.spliteasy.backend.group.GroupRepository;
import com.spliteasy.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final GroupRepository groupRepository;
    private final BalanceService balanceService;

    @Transactional
    public PaymentResponse markReceived(Long groupId, SettleRequest request, String currentEmail) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        User receiver = group.getMembers().stream()
                .filter(m -> m.getEmail().equals(currentEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this group"));

        User payer = group.getMembers().stream()
                .filter(m -> m.getId().equals(request.fromUserId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The payer must be a member of this group"));

        if (payer.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("You cannot settle with yourself");
        }
        if (request.amount().stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount can have at most 2 decimal places");
        }
        BigDecimal amount = request.amount().setScale(2, RoundingMode.UNNECESSARY);

        // sirf wahi payment allowed hai jo abhi pending settlement mein hai
        List<TransferResponse> pending = balanceService.getSimplifiedTransfers(groupId, currentEmail);
        boolean valid = pending.stream().anyMatch(t ->
                t.from().id().equals(payer.getId())
                        && t.to().id().equals(receiver.getId())
                        && amount.compareTo(t.amount()) <= 0);
        if (!valid) {
            throw new IllegalArgumentException("No pending payment of this amount from this member to you");
        }

        Payment payment = new Payment();
        payment.setGroup(group);
        payment.setFromUser(payer);
        payment.setToUser(receiver);
        payment.setAmount(amount);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(Long groupId, String currentEmail) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getEmail().equals(currentEmail));
        if (!isMember) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
        return paymentRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream().map(PaymentResponse::from).toList();
    }
}