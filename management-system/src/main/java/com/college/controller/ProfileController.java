package com.college.controller;

import com.college.dto.ProfileDTO;
import com.college.model.Student;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.service.UserService;
import com.college.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private UserService userService;

    // 获取当前用户个人信息
    @GetMapping
    public ResponseEntity<Response<ProfileDTO>> getProfile(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            ProfileDTO profileDTO = convertToProfileDTO(currentUser);
            return ResponseEntity.ok(Response.success("获取个人信息成功", profileDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取个人信息失败: " + e.getMessage(), null));
        }
    }

    // 更新当前用户个人信息
    @PutMapping
    public ResponseEntity<Response<ProfileDTO>> updateProfile(@RequestBody ProfileDTO profileDTO, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            // 更新用户基本信息
            currentUser.setName(profileDTO.getName());
            currentUser.setEmail(profileDTO.getEmail());
            currentUser.setPhone(profileDTO.getPhone());
            
            // 根据用户角色更新特定信息
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                student.setGender(profileDTO.getGender());
                student.setAddress(profileDTO.getAddress());
                
                // 只更新允许学生修改的字段
                if (profileDTO.getClassName() != null) {
                    student.setClassName(profileDTO.getClassName());
                }
                if (profileDTO.getMajor() != null) {
                    student.setMajor(profileDTO.getMajor());
                }
            } else if (currentUser instanceof Teacher) {
                Teacher teacher = (Teacher) currentUser;
                teacher.setGender(profileDTO.getGender());
                teacher.setOffice(profileDTO.getOffice());
                teacher.setOfficePhone(profileDTO.getOfficePhone());
                teacher.setMobilePhone(profileDTO.getMobilePhone());
                
                // 只更新允许教师修改的字段
                if (profileDTO.getTitle() != null) {
                    teacher.setTitle(profileDTO.getTitle());
                }
                if (profileDTO.getEducation() != null) {
                    teacher.setEducation(profileDTO.getEducation());
                }
            }
            
            // 保存更新
            User updatedUser = userService.save(currentUser);
            ProfileDTO updatedProfileDTO = convertToProfileDTO(updatedUser);
            
            return ResponseEntity.ok(Response.success("更新个人信息成功", updatedProfileDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新个人信息失败: " + e.getMessage(), null));
        }
    }

    // 将User实体转换为ProfileDTO
    private ProfileDTO convertToProfileDTO(User user) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setName(user.getName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhone(user.getPhone());
        profileDTO.setRole(user.getRole().name());
        
        // 根据用户角色设置特定字段
        if (user instanceof Student) {
            Student student = (Student) user;
            profileDTO.setStudentId(student.getStudentId());
            profileDTO.setClassName(student.getClassName());
            profileDTO.setMajor(student.getMajor());
            profileDTO.setGender(student.getGender());
            profileDTO.setAddress(student.getAddress());
            profileDTO.setIdCard(student.getIdCard());
            profileDTO.setPoliticalStatus(student.getPoliticalStatus());
        } else if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            profileDTO.setTeacherId(teacher.getTeacherId());
            profileDTO.setTitle(teacher.getTitle());
            profileDTO.setEducation(teacher.getEducation());
            profileDTO.setOffice(teacher.getOffice());
            profileDTO.setOfficePhone(teacher.getOfficePhone());
            profileDTO.setMobilePhone(teacher.getMobilePhone());
            profileDTO.setGender(teacher.getGender());
        }
        
        // 设置头像URL，如果用户有自定义头像则使用自定义头像，否则使用默认头像
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            profileDTO.setAvatar("/avatar/" + user.getAvatar());
        } else {
            profileDTO.setAvatar("/avatar/default.jpg");
        }
        
        return profileDTO;
    }
    
    // 上传头像
    @PostMapping("/avatar")
    public ResponseEntity<Response<String>> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType != null && !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                return ResponseEntity.badRequest().body(Response.error("只支持JPG和PNG格式的图片", null));
            }
            
            // 检查文件大小 (最大2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Response.error("文件大小不能超过2MB", null));
            }
            
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + currentUser.getId() + "_" + System.currentTimeMillis() + extension;
            
            // 确保目录存在
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
            
            // 更新用户头像信息
            currentUser.setAvatar(filename);
            userService.save(currentUser);
            
            return ResponseEntity.ok(Response.success("头像上传成功", "/avatar/" + filename));
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Response.error("上传失败: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("上传失败: " + e.getMessage(), null));
        }
    }
}