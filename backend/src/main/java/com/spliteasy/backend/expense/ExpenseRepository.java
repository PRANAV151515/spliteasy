package com.spliteasy.backend.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupIdOrderByExpenseDateDescCreatedAtDesc(Long groupId);
}

