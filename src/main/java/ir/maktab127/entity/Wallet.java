package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Wallet extends BaseEntity<Long> {

    @OneToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal balance;


    public void setUserId(Long id) {
        this.user.setId(this.getId());
    }
}
