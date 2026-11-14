package com.spliteasy.backend.payment;

import com.spliteasy.backend.group.Group;
import com.spliteasy.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    // jisne paise diye
    @ManyToOne(optional = false)
    @JoinColumn(name = "from_user")
    private User fromUser;

    // jisne paise receive kiye (wahi "received" mark karta hai)
    @ManyToOne(optional = false)
    @JoinColumn(name = "to_user")
    private User toUser;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDateTime createdAt = LocalDateTime.now();
}