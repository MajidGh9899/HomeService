package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.User;
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
public class Payment extends BaseEntity<Long> {
    @Column
    private String token;
    @ManyToOne
    private Customer user;
    @Column
    private BigDecimal  amount;
    @Column
    private LocalDateTime expiresAt;
    @Column
    private boolean used;

}
