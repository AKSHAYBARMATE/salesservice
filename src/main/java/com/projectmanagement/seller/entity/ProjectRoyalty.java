package com.projectmanagement.seller.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_royalty")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRoyalty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private SalesLevel salesLevel;

    @Column(name = "royalty_rate", precision = 5, scale = 2)
    private BigDecimal royaltyRate;

    @Column(name = "royalty_amount", precision = 12, scale = 2)
    private BigDecimal royaltyAmount;

    @Column(name = "period_type", length = 20)
    private String periodType; // Monthly, Yearly

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Pending"; // Pending, Paid

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
