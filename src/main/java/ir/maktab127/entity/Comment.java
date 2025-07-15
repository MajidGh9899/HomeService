package ir.maktab127.entity;

import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = Comment.table_name)
public class Comment extends BaseEntity<Long> {
    public static final String table_name= "comments";
    public static final String customers= "customers";
    public static final String specialists= "specialists";
    public static final String rate= "rating";
    public static final String text_comment= "text_comments";
    public static final String created_at= "created_at";




    @ManyToOne(optional = false)
    @JoinColumn(name = customers, nullable = false)
    private Customer customer;

    @OneToOne(optional = true)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = specialists, nullable = false)
    private Specialist specialist;

    @Column(name = rate, nullable = false )
    private Integer rating;

    @Column(name = text_comment, nullable = false)
    private String text;

    @Column(name = created_at, nullable = false)
    private LocalDateTime createdAt;
}
