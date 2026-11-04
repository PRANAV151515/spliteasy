package com.spliteasy.backend.balance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class DebtSimplifier {

    public record Transfer(Long fromUserId, Long toUserId, BigDecimal amount) {}

    private record Entry(Long userId, BigDecimal amount) {}

    public static List<Transfer> simplify(Map<Long, BigDecimal> balances) {
        // sabse bada amount sabse pehle nikle, isliye b ko a se compare kiya
        PriorityQueue<Entry> creditors = new PriorityQueue<>((a, b) -> b.amount().compareTo(a.amount()));
        PriorityQueue<Entry> debtors = new PriorityQueue<>((a, b) -> b.amount().compareTo(a.amount()));

        for (Map.Entry<Long, BigDecimal> e : balances.entrySet()) {
            if (e.getValue().signum() > 0) {
                creditors.add(new Entry(e.getKey(), e.getValue()));
            } else if (e.getValue().signum() < 0) {
                debtors.add(new Entry(e.getKey(), e.getValue().negate()));
            }
        }

        List<Transfer> result = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Entry creditor = creditors.poll();
            Entry debtor = debtors.poll();

            BigDecimal settled = creditor.amount().min(debtor.amount());
            result.add(new Transfer(debtor.userId(), creditor.userId(), settled));

            BigDecimal creditorLeft = creditor.amount().subtract(settled);
            BigDecimal debtorLeft = debtor.amount().subtract(settled);

            if (creditorLeft.signum() > 0) {
                creditors.add(new Entry(creditor.userId(), creditorLeft));
            }
            if (debtorLeft.signum() > 0) {
                debtors.add(new Entry(debtor.userId(), debtorLeft));
            }
        }
        return result;
    }
}
