package com.clickstechnology.Brillo.Mall.domain.user;

import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCaseAndStatus(String username, EntityStatus status);

    @Query("""
                SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
                FROM User u
                WHERE LOWER(u.username) = :username
                  AND u.status = com.clickstechnology.Brillo.Mall.application.enums.EntityStatus.ACTIVE
            """)
    boolean existsByUserName(@Param("username") String username);

    @Query("""
                SELECT u.email AS email,
                       u.phoneNumber AS phoneNumber,
                       u.password AS password,
                       u.status AS status
                FROM User u
                JOIN u.roles r
                WHERE (LOWER(u.email) = LOWER(:username) OR u.phoneNumber = :username)
            """)
    Optional<AuthUser> findAuthUserByEmailOrPhone(@Param("username") String username);

    @Query("""
                SELECT u.email AS email,
                       u.phoneNumber AS phoneNumber,
                       u.username AS username,
                       u.password AS password,
                       u.status AS status
                FROM User u
                JOIN u.roles r
                WHERE (LOWER(u.username) = :username)
            """)
    Optional<AuthUser> findAuthUserByUsername(@Param("username") String username);

    @Query("""
                SELECT u
                FROM User u
                LEFT JOIN FETCH u.roles
                WHERE LOWER(u.username) = :username
            """)
    Optional<User> fetchAllByUsername(String username);
}
