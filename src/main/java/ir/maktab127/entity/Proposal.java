package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Proposal extends BaseEntity<Long> {

    @ManyToOne(optional = false)
    private Specialist specialist;

    @ManyToOne(optional = false)
    private Order order;

    @Column(nullable = false)
    private BigDecimal proposedPrice;

    @Column(nullable = false)
    private LocalDateTime proposedStartTime;

    @Column(nullable = false)
    private LocalDateTime endDate;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;


    private String description;
}
