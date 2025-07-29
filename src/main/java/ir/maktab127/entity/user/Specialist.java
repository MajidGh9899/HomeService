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
import java.util.Set;

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
    @Column
    private String emailVerificationToken;
    @Column(nullable = false)
    private boolean emailVerified=false;
    @PostLoad
    public void setDefaultRole() {
        if (this.getRoles() == null || this.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.SPECIALIST);
            this.setRoles(roles);
        }
    }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }


}
