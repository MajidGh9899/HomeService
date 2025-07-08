package ir.maktab127.entity.baseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;


@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity<ID extends Serializable> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "CREATE_DATE")
    @CreatedDate
    private ZonedDateTime createDate;

    @Column(name = "LAST_UPDATE_DATE")
    @LastModifiedDate
    private ZonedDateTime lastUpdateDate;



    @PrePersist
    public void registrationDate() {
        createDate = ZonedDateTime.now();

    }

}
