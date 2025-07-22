package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    public Proposal(LocalDateTime zonedDateTime, LocalDateTime now, Order order, Specialist specialist) {
        super();
        this.proposedStartTime = zonedDateTime;
        this.endDate = now;
        this.order = order;
        this.specialist = specialist;
    }
}
