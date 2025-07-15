package ir.maktab127.entity.user;

import ir.maktab127.entity.Comment;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.ServiceCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("specialist")
public class Specialist  extends User {
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @ManyToMany
    @JoinTable(
            name = "specialist_service_category",
            joinColumns = @JoinColumn(name = "specialist_id"),
            inverseJoinColumns = @JoinColumn(name = "service_category_id")
    )
    private List<ServiceCategory> serviceCategories = new ArrayList<>();

    @OneToMany(mappedBy = "specialist")
    private List<Comment> comments = new ArrayList<>();



}
