package com.clickstechnology.Brillo.Mall.domain.user;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status <> 'DELETED'")
@Table(
        name = "BRILLO_USER",
        indexes = {
                @Index(name = "idx_brillo_user_email", columnList = "email"),
                @Index(name = "idx_brillo_user_phone", columnList = "phone_number"),
                @Index(name = "idx_brillo_user_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brillo_user_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_brillo_user_phone", columnNames = {"phone_number"})
        }
)
class User extends JpaAuditor implements Serializable {

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "invited_by", length = 100)
    private String invitedBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "BRILLO_USER_ROLES",
            joinColumns = @JoinColumn(name = "user_id"),
            indexes = {
                    @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role", columnList = "role")
            }
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    private List<UserRole> roles;

    @Column(name = "referred_by", length = 100)
    private String referredBy;

    @Column(name = "oauth2_user")
    private boolean oauth2User = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private EntityStatus status;

    public UserDto dto() {
        return UserDto.builder()
                .id(reference)
                .fullName(fullName)
                .username(username)
                .email(email)
                .phoneNumber(phoneNumber)
                .roles(roles.stream().map(Enum::name).toList())
                .referredBy(referredBy)
                .oauth2User(oauth2User)
                .status(status)
                .build();
    }
}

