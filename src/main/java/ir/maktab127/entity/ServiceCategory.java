package ir.maktab127.entity;


import ir.maktab127.entity.baseEntity.BaseEntity;
import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ServiceCategory extends BaseEntity< Long> {


    @Column(nullable = false,unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column( )
    private String description;

    @ManyToOne
    private ServiceCategory parent;

    @OneToMany(mappedBy = "parent")
    private List<ServiceCategory> subServices;
    @ManyToMany(mappedBy = "serviceCategories")
    private List<Specialist> specialists = new ArrayList<>();

}
