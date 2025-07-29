package ir.maktab127.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("customer")

public class Customer extends User{

    @Column
    private String emailVerificationToken;
    @Column(nullable = false)
    private boolean emailVerified=false;
    @PostLoad
    public void setDefaultRole() {
        if (this.getRoles() == null || this.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.CUSTOMER);
            this.setRoles(roles);
        }
    }

}
