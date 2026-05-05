package com.example.attendance.course.dto;

import com.example.attendance.course.Course;

public record CourseResponse(Long id, String name, String code) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getName(), course.getCode());
    }
}
