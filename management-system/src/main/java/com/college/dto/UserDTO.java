package com.college.dto;

import com.college.model.User;
import com.college.model.Student;
import com.college.model.Teacher;
import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private User.Role role;
    private boolean enabled;
    private String avatar;

    // 用于返回学生或教师特有的ID
    private String userSpecificId;

    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        
        // 设置头像URL
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            dto.setAvatar("/avatar/" + user.getAvatar());
        } else {
            dto.setAvatar("/avatar/default.jpg");
        }
        
        // 根据用户类型设置特有ID
        if (user instanceof Student) {
            dto.setUserSpecificId(((Student) user).getStudentId());
        } else if (user instanceof Teacher) {
            dto.setUserSpecificId(((Teacher) user).getTeacherId());
        }
        
        return dto;
    }
}