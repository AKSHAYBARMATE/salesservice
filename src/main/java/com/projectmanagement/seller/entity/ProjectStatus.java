package com.projectmanagement.seller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_status")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name; // e.g., Lead, Pitch, Negotiation, Won, Lost, Submitted, In Review

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "is_won_status")
    @Builder.Default
    private Boolean isWonStatus = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
