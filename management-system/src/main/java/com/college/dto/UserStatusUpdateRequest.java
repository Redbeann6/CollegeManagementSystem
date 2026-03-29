package com.college.dto;

import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    private Boolean enabled;
    
    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}