package ir.maktab127.entity.user;


import ir.maktab127.entity.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@DiscriminatorColumn(name = "user_type")
@Table(name = User.table)
public  class User extends BaseEntity<Long> {
    public static final String table = "users";
    public static final String first_names= "first_name";
    public static final String last_names= "last_name";
    public static final String emails= "email";
    public static final String passwords= "password";


    @Column(name=first_names,nullable = false)
    private String firstName;

    @Column(name = last_names, nullable = false)
    private String lastName;

    @Column(name=emails,nullable = false, unique = true)
    private String email;

    @Column(name = passwords, nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;



}
