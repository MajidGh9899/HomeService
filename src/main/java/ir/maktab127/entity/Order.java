package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = Order.table_name)
public class Order extends BaseEntity<Long> {
    public static final String table_name="orders";
    public static final String customers="customers";
    public static final String services="services";
    public static final String descriptions_order= "descriptions_order";
    public static final String proposed_price="proposed_price";
    public static final String start_date="start_date";
    public static final String addresses="addresses";
    public static final String status_order="status";

    public static final String  specialists="specialists_id";

    @ManyToOne(optional = false)
    @JoinColumn(name = customers)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = services)
    private ServiceCategory service;

    @Column(name = descriptions_order, nullable = false)
    private String description;

    @Column(name = proposed_price, nullable = false)
    private BigDecimal proposedPrice;

    @Column(name = start_date, nullable = false)
    private LocalDateTime startDate;

    @Column(name = addresses, nullable = false)
    private String address;

   

    @Enumerated(EnumType.STRING)
    @Column(name = status_order, nullable = false)
    private OrderStatus status;

    @OneToOne(optional = true)
    @JoinColumn(name = specialists)
    private Specialist  specialist;
}
