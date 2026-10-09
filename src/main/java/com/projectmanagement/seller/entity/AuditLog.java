package com.projectmanagement.seller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action", nullable = false, length = 100)
    private String action; // e.g. CREATE, UPDATE, DELETE, ASSIGN, STATUS_CHANGE, SUBMIT, APPROVE, REJECT, MARK_WON, MARK_LOST

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // e.g. USER, ROLE, SALES_LEVEL, CLIENT, PROJECT, PROPOSAL, PAYMENT, COMMISSION, ROYALTY

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}
