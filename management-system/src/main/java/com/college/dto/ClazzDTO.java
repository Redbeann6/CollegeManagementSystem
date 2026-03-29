package com.college.dto;

import lombok.Data;

@Data
public class ClazzDTO {
    private Long id;
    private String name;
    private Long majorId;
    private Long departmentId;
    private Integer enrollmentYear;
    private Integer capacity;
}