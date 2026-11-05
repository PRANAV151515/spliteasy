package com.spliteasy.backend.expense;

import com.spliteasy.backend.group.Group;
import com.spliteasy.backend.group.GroupRepository;
import com.spliteasy.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private static final String CALLER = "rahul@gmail.com";

    @BeforeEach
    void setUp() {
        User rahul = user(1L, "Rahul", "rahul@gmail.com");
        User amit = user(2L, "Amit", "amit@gmail.com");
        User karan = user(3L, "Karan", "karan@gmail.com");

        Group group = new Group();
        group.setId(1L);
        group.setName("Goa Trip");
        group.setCreatedBy(rahul);
        group.getMembers().add(rahul);
        group.getMembers().add(amit);
        group.getMembers().add(karan);

        lenient().when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        lenient().when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static User user(Long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("hashed");
        return u;
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void equalSplit_100Among3_totalStaysExact() {
        ExpenseRequest req = new ExpenseRequest("Cab", bd("100"), 1L, SplitType.EQUAL, null, null);

        ExpenseResponse res = expenseService.addExpense(1L, req, CALLER);

        assertEquals(3, res.splits().size());

        BigDecimal total = res.splits().stream()
                .map(ExpenseResponse.SplitResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, bd("100.00").compareTo(total));

        long with3334 = res.splits().stream()
                .filter(s -> s.amount().compareTo(bd("33.34")) == 0).count();
        long with3333 = res.splits().stream()
                .filter(s -> s.amount().compareTo(bd("33.33")) == 0).count();
        assertEquals(1, with3334);
        assertEquals(2, with3333);
    }

    @Test
    void exactSplit_validTotal_works() {
        ExpenseRequest req = new ExpenseRequest("Dinner", bd("900"), 1L, SplitType.EXACT, null,
                List.of(new SplitInput(1L, bd("500")), new SplitInput(2L, bd("400"))));

        ExpenseResponse res = expenseService.addExpense(1L, req, CALLER);

        assertEquals(2, res.splits().size());
        BigDecimal rahulShare = res.splits().stream()
                .filter(s -> s.user().id().equals(1L))
                .findFirst().orElseThrow().amount();
        assertEquals(0, bd("500.00").compareTo(rahulShare));
    }

    @Test
    void exactSplit_wrongTotal_throwsError() {
        ExpenseRequest req = new ExpenseRequest("Dinner", bd("900"), 1L, SplitType.EXACT, null,
                List.of(new SplitInput(1L, bd("500")), new SplitInput(2L, bd("300"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> expenseService.addExpense(1L, req, CALLER));

        assertTrue(ex.getMessage().contains("barabar hona chahiye"));
    }

    @Test
    void percentageSplit_50_30_20_on1000() {
        ExpenseRequest req = new ExpenseRequest("Hotel", bd("1000"), 1L, SplitType.PERCENTAGE, null,
                List.of(new SplitInput(1L, bd("50")),
                        new SplitInput(2L, bd("30")),
                        new SplitInput(3L, bd("20"))));

        ExpenseResponse res = expenseService.addExpense(1L, req, CALLER);

        BigDecimal amitShare = res.splits().stream()
                .filter(s -> s.user().id().equals(2L))
                .findFirst().orElseThrow().amount();
        assertEquals(0, bd("300.00").compareTo(amitShare));
    }

    @Test
    void percentageSplit_totalNot100_throwsError() {
        ExpenseRequest req = new ExpenseRequest("Hotel", bd("1000"), 1L, SplitType.PERCENTAGE, null,
                List.of(new SplitInput(1L, bd("50")), new SplitInput(2L, bd("30"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> expenseService.addExpense(1L, req, CALLER));

        assertTrue(ex.getMessage().contains("100"));
    }

    @Test
    void nonMember_cannotAddExpense() {
        ExpenseRequest req = new ExpenseRequest("Cab", bd("100"), 1L, SplitType.EQUAL, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.addExpense(1L, req, "bahari@gmail.com"));
    }
}

