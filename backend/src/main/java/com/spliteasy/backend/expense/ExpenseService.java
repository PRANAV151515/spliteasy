package com.spliteasy.backend.expense;

import com.spliteasy.backend.group.Group;
import com.spliteasy.backend.group.GroupRepository;
import com.spliteasy.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ONE_PAISA = new BigDecimal("0.01");

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public ExpenseResponse addExpense(Long groupId, ExpenseRequest request, String currentEmail) {
        Group group = findGroup(groupId);
        checkMember(group, currentEmail);

        Map<Long, User> members = group.getMembers().stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        User payer = members.get(request.paidBy());
        if (payer == null) {
            throw new IllegalArgumentException("The payer must be a member of this group");
        }

        if (request.amount().stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount can have at most 2 decimal places");
        }
        BigDecimal amount = request.amount().setScale(2, RoundingMode.UNNECESSARY);

        Map<Long, BigDecimal> shares = switch (request.splitType()) {
            case EQUAL -> equalSplit(amount, request.splits(), members);
            case EXACT -> exactSplit(amount, request.splits(), members);
            case PERCENTAGE -> percentageSplit(amount, request.splits(), members);
        };

        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setAmount(amount);
        expense.setGroup(group);
        expense.setPaidBy(payer);
        expense.setSplitType(request.splitType());
        if (request.expenseDate() != null) {
            expense.setExpenseDate(request.expenseDate());
        }

        for (Map.Entry<Long, BigDecimal> entry : shares.entrySet()) {
            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(expense);
            split.setUser(members.get(entry.getKey()));
            split.setAmount(entry.getValue());
            expense.getSplits().add(split);
        }

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(Long groupId, String currentEmail) {
        Group group = findGroup(groupId);
        checkMember(group, currentEmail);
        return expenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    // ---------- EQUAL ----------
    private Map<Long, BigDecimal> equalSplit(BigDecimal amount, List<SplitInput> inputs,
                                             Map<Long, User> members) {
        List<Long> ids;
        if (inputs == null || inputs.isEmpty()) {
            ids = new ArrayList<>(members.keySet());
        } else {
            ids = inputs.stream().map(SplitInput::userId).distinct().toList();
        }
        validateMembers(ids, members);

        int n = ids.size();
        BigDecimal base = amount.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        BigDecimal remainder = amount.subtract(base.multiply(BigDecimal.valueOf(n)));
        int extraPaise = remainder.movePointRight(2).intValue();

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            BigDecimal share = i < extraPaise ? base.add(ONE_PAISA) : base;
            result.put(ids.get(i), share);
        }
        return result;
    }

    // ---------- EXACT ----------
    private Map<Long, BigDecimal> exactSplit(BigDecimal amount, List<SplitInput> inputs,
                                             Map<Long, User> members) {
        requireSplits(inputs);
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (SplitInput in : inputs) {
            if (in.value() == null || in.value().signum() <= 0) {
                throw new IllegalArgumentException("Har member ka Amount must be greater than 0");
            }
            if (in.value().stripTrailingZeros().scale() > 2) {
                throw new IllegalArgumentException("Split Amount can have at most 2 decimal places");
            }
            if (result.containsKey(in.userId())) {
                throw new IllegalArgumentException("A user cannot appear twice in the split");
            }
            BigDecimal value = in.value().setScale(2, RoundingMode.UNNECESSARY);
            result.put(in.userId(), value);
            total = total.add(value);
        }
        validateMembers(new ArrayList<>(result.keySet()), members);

        if (total.compareTo(amount) != 0) {
            throw new IllegalArgumentException(
                    "The split total (" + total + ") must equal the expense amount (" + amount + ")");
        }
        return result;
    }

    // ---------- PERCENTAGE ----------
    private Map<Long, BigDecimal> percentageSplit(BigDecimal amount, List<SplitInput> inputs,
                                                  Map<Long, User> members) {
        requireSplits(inputs);
        Map<Long, BigDecimal> percents = new LinkedHashMap<>();
        BigDecimal totalPercent = BigDecimal.ZERO;

        for (SplitInput in : inputs) {
            if (in.value() == null || in.value().signum() <= 0) {
                throw new IllegalArgumentException("Each member's percentage must be greater than 0");
            }
            if (percents.containsKey(in.userId())) {
                throw new IllegalArgumentException("A user cannot appear twice in the split");
            }
            percents.put(in.userId(), in.value());
            totalPercent = totalPercent.add(in.value());
        }
        validateMembers(new ArrayList<>(percents.keySet()), members);

        if (totalPercent.compareTo(HUNDRED) != 0) {
            throw new IllegalArgumentException("Percentages must add up to 100 (currently" + totalPercent + ")");
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> e : percents.entrySet()) {
            BigDecimal share = amount.multiply(e.getValue())
                    .divide(HUNDRED, 2, RoundingMode.DOWN);
            result.put(e.getKey(), share);
            assigned = assigned.add(share);
        }

        // rounding se bacha hua paisa pehle member ko de do, taaki total exact rahe
        BigDecimal leftover = amount.subtract(assigned);
        if (leftover.signum() > 0) {
            Long firstId = result.keySet().iterator().next();
            result.put(firstId, result.get(firstId).add(leftover));
        }
        return result;
    }

    // ---------- helpers ----------
    private void requireSplits(List<SplitInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("Splits are required for this split type");
        }
    }

    private void validateMembers(List<Long> ids, Map<Long, User> members) {
        for (Long id : ids) {
            if (!members.containsKey(id)) {
                throw new IllegalArgumentException("User " + id + " is not a member of this group");
            }
        }
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

