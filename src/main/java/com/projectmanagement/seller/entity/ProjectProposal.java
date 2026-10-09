package com.projectmanagement.seller.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_proposals")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "proposal_name", nullable = false, length = 100)
    private String proposalName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "one_time_price", precision = 12, scale = 2)
    private BigDecimal oneTimePrice;

    @Column(name = "recurring_amount", precision = 12, scale = 2)
    private BigDecimal recurringAmount;

    @Column(name = "billing_frequency", length = 20)
    private String billingFrequency; // Monthly, Yearly

    @Column(name = "validity_date")
    private LocalDate validityDate;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Draft"; // Draft, Submitted, Sent, Accepted, Rejected

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
