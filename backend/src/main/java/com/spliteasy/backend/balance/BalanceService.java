package com.spliteasy.backend.balance;

import com.spliteasy.backend.expense.Expense;
import com.spliteasy.backend.expense.ExpenseRepository;
import com.spliteasy.backend.expense.ExpenseSplit;
import com.spliteasy.backend.group.Group;
import com.spliteasy.backend.group.GroupRepository;
import com.spliteasy.backend.user.User;
import com.spliteasy.backend.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<BalanceResponse> getBalances(Long groupId, String currentEmail) {
        Group group = findGroup(groupId);
        checkMember(group, currentEmail);

        Map<Long, User> users = new LinkedHashMap<>();
        Map<Long, BigDecimal> balances = computeBalances(group, users);

        return balances.entrySet().stream()
                .map(e -> new BalanceResponse(UserResponse.from(users.get(e.getKey())), e.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getSimplifiedTransfers(Long groupId, String currentEmail) {
        Group group = findGroup(groupId);
        checkMember(group, currentEmail);

        Map<Long, User> users = new LinkedHashMap<>();
        Map<Long, BigDecimal> balances = computeBalances(group, users);

        return DebtSimplifier.simplify(balances).stream()
                .map(t -> new TransferResponse(
                        UserResponse.from(users.get(t.fromUserId())),
                        UserResponse.from(users.get(t.toUserId())),
                        t.amount()))
                .toList();
    }

    private Map<Long, BigDecimal> computeBalances(Group group, Map<Long, User> users) {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();

        // pehle sab members ka balance 0 se shuru
        for (User m : group.getMembers()) {
            users.put(m.getId(), m);
            balances.put(m.getId(), BigDecimal.ZERO);
        }

        List<Expense> expenses = expenseRepository
                .findByGroupIdOrderByExpenseDateDescCreatedAtDesc(group.getId());

        for (Expense expense : expenses) {
            // payer ko poora amount plus
            Long payerId = expense.getPaidBy().getId();
            users.putIfAbsent(payerId, expense.getPaidBy());
            balances.merge(payerId, expense.getAmount(), BigDecimal::add);

            // har split wale ka hissa minus
            for (ExpenseSplit split : expense.getSplits()) {
                Long userId = split.getUser().getId();
                users.putIfAbsent(userId, split.getUser());
                balances.merge(userId, split.getAmount().negate(), BigDecimal::add);
            }
        }
        return balances;
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    private void checkMember(Group group, String email) {
        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getEmail().equals(email));
        if (!isMember) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
    }
}

