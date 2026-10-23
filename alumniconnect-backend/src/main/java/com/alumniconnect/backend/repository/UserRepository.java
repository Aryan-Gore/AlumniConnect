package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleSub(String googleSub);

    boolean existsByRole(Role role);

    long countByRoleAndActiveTrue(Role role);

    /**
     * People search for "start a chat": active accounts whose name contains
     * the text, never the caller themselves. Pass a PageRequest to cap the result size.
     */
    @Query("select u from User u where u.active = true and u.id <> :excludeId "
            + "and lower(u.fullName) like lower(concat('%', :q, '%')) order by u.fullName asc")
    List<User> searchByName(@Param("q") String q, @Param("excludeId") Long excludeId, Pageable pageable);

    /**
     * Admin user list: all accounts, optionally filtered by role and/or name/email, newest first.
     */
    @Query("""
            select u from User u
            where (:role is null or u.role = :role)
              and (:query is null or lower(u.fullName) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%')))
            order by u.id desc
            """)
    Page<User> findAdminUsers(
            @Param("role") Role role,
            @Param("query") String query,
            Pageable pageable);
}
