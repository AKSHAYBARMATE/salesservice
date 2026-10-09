package com.projectmanagement.seller.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "project_payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "payment_type", length = 20, nullable = false)
    private String paymentType; // One Time, Recurring

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "billing_period", length = 20)
    private String billingPeriod; // Monthly, Yearly

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Pending"; // Pending, Received, Overdue

    @Column(name = "notes", length = 255)
    private String notes;
}
