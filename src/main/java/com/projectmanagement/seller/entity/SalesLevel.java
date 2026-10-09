package com.projectmanagement.seller.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_levels")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesLevel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_name", nullable = false, length = 50, unique = true)
    private String levelName; // e.g. Bronze, Silver, Gold, Platinum

    @Column(name = "min_projects")
    private Integer minProjects;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate; // e.g. 5.00 (%)

    @Column(name = "royalty_rate", precision = 5, scale = 2)
    private BigDecimal royaltyRate; // e.g. 2.00 (%)

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
