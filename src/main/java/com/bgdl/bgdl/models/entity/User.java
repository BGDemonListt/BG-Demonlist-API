package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    @Size(min = 2, message = "Името трябва да е поне 2 символа!")
    private String name;

    @Email(message = "Имейлът трябва да бъде в правилен формат!")
    @NotNull(message = "Имейлът не може да бъде празен!")
    @Column(unique = true)
    private String email;

    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Embedded
    private DiscordProfile discord;

    @Column(name = "enabled")
    private boolean enabled;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @PrePersist
    void prePersist() {
        if (this.provider == null) {
            this.provider = Provider.LOCAL;
        }
    }
}
