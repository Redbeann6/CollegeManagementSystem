package com.college.controller;

import com.college.dto.Response;
import com.college.dto.UserDTO;
import com.college.model.User;
import com.college.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(userService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取用户列表成功", userDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(authentication, #id)")
    public ResponseEntity<Response<UserDTO>> getUserById(@PathVariable Long id) {
        User user = userService.findByIdOrThrow(id);
        UserDTO userDTO = userService.convertToDTO(user);
        return ResponseEntity.ok(Response.success("获取用户成功", userDTO));
    }

    @GetMapping("/current")
    public ResponseEntity<Response<UserDTO>> getCurrentUser(@RequestAttribute("currentUser") User currentUser) {
        UserDTO userDTO = userService.convertToDTO(currentUser);
        return ResponseEntity.ok(Response.success("获取当前用户成功", userDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(authentication, #id)")
    public ResponseEntity<Response<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        User existingUser = userService.findByIdOrThrow(id);
        
        User userToUpdate = userService.convertToEntity(userDTO);
        userToUpdate.setId(id); // 确保ID不变
        // 不允许更新密码，密码更新应通过专门的接口
        userToUpdate.setPassword(existingUser.getPassword());
        
        User updatedUser = userService.save(userToUpdate);
        UserDTO updatedDTO = userService.convertToDTO(updatedUser);
        return ResponseEntity.ok(Response.success("更新用户成功", updatedDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteUser(@PathVariable Long id) {
        // 先检查用户是否存在，如果不存在会抛出ResourceNotFoundException
        userService.findByIdOrThrow(id);
        
        userService.deleteById(id);
        return ResponseEntity.ok(Response.success("删除用户成功", null));
    }
}