package com.college.service;

import com.college.model.User;
import com.college.dto.UserDTO;
import com.college.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User findByUsername(String username);
    User save(User user);
    Optional<User> findById(Long id);
    User findByIdOrThrow(Long id) throws ResourceNotFoundException;
    List<User> findAll();
    void deleteById(Long id);
    UserDTO convertToDTO(User user);
    User convertToEntity(UserDTO userDTO);
    boolean existsByUsername(String username);
    
    // 添加按角色查询用户的方法
    List<User> findByRole(User.Role role);
    
    // 更新用户状态
    User updateEnabledStatus(Long id, boolean enabled);
}