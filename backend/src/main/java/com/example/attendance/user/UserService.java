package com.example.attendance.user;

import com.example.attendance.common.exception.ConflictException;
import com.example.attendance.common.exception.NotFoundException;
import com.example.attendance.user.dto.CreateUserRequest;
import com.example.attendance.user.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request, Role role) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setRollNumber(role == Role.STUDENT ? blankToNull(request.rollNumber()) : null);
        user.setEmployeeCode(role == Role.FACULTY ? blankToNull(request.employeeCode()) : null);
        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> byRole(Role role) {
        return userRepository.findByRoleOrderByNameAsc(role).stream().map(UserResponse::from).toList();
    }

    public AppUser requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
