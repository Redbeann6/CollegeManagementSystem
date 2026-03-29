package com.college.repository;

import com.college.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 添加按角色查询用户的方法
    List<User> findByRole(User.Role role);
    
    // 添加统计查询方法
    long countByRole(User.Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1 AND u.createdAt BETWEEN ?2 AND ?3")
    long countByRoleAndCreatedAtBetween(User.Role role, LocalDateTime start, LocalDateTime end);
    

}