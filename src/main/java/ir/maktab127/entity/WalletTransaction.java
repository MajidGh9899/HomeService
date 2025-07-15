package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class WalletTransaction extends BaseEntity<Long> {

    @ManyToOne(optional = false)
    private Wallet wallet;

    @Column(nullable = false)
    private BigDecimal amount;
    @Column
    private String description;

}
