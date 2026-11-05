package com.spliteasy.backend.balance;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DebtSimplifierTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    void simpleCase_twoDebtorsPayOneCreditor() {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("600.00"));
        balances.put(2L, bd("-300.00"));
        balances.put(3L, bd("-300.00"));

        List<DebtSimplifier.Transfer> result = DebtSimplifier.simplify(balances);

        assertEquals(2, result.size());
        for (DebtSimplifier.Transfer t : result) {
            assertEquals(1L, t.toUserId());
            assertEquals(0, bd("300.00").compareTo(t.amount()));
        }
    }

    @Test
    void chainDebt_AtoB_BtoC_becomesAtoC() {
        // A ne B ko 100 dene hain, B ne C ko 100 dene hain
        // net: A = -100, B = 0, C = +100
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("-100.00"));
        balances.put(2L, bd("0.00"));
        balances.put(3L, bd("100.00"));

        List<DebtSimplifier.Transfer> result = DebtSimplifier.simplify(balances);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).fromUserId());
        assertEquals(3L, result.get(0).toUserId());
        assertEquals(0, bd("100.00").compareTo(result.get(0).amount()));
    }

    @Test
    void everyoneSettled_noTransfers() {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, BigDecimal.ZERO);
        balances.put(2L, BigDecimal.ZERO);

        assertTrue(DebtSimplifier.simplify(balances).isEmpty());
    }

    @Test
    void transfersNeverExceedNMinusOne_andTotalsMatch() {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("500.00"));
        balances.put(2L, bd("200.00"));
        balances.put(3L, bd("-350.00"));
        balances.put(4L, bd("-150.00"));
        balances.put(5L, bd("-200.00"));

        List<DebtSimplifier.Transfer> result = DebtSimplifier.simplify(balances);

        assertTrue(result.size() <= balances.size() - 1);

        // jitna paisa gaya, utna hi aaya: har user ka balance transfers ke baad 0 hona chahiye
        Map<Long, BigDecimal> after = new LinkedHashMap<>(balances);
        for (DebtSimplifier.Transfer t : result) {
            after.merge(t.fromUserId(), t.amount(), BigDecimal::add);
            after.merge(t.toUserId(), t.amount().negate(), BigDecimal::add);
        }
        for (BigDecimal b : after.values()) {
            assertEquals(0, BigDecimal.ZERO.compareTo(b));
        }
    }
}
