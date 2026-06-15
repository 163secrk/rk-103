package com.airs.service;

import com.airs.common.PageResult;
import com.airs.dto.LoginRequest;
import com.airs.dto.LoginResponse;
import com.airs.dto.ResetPasswordRequest;
import com.airs.dto.UserCreateRequest;
import com.airs.dto.UserUpdateRequest;
import com.airs.entity.User;
import com.airs.repository.UserRepository;
import com.airs.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getRealName(),
                user.getAvatar()
        );
        return new LoginResponse(token, userInfo);
    }

    public User getCurrentUser(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public PageResult<User> getUserPage(int pageNum, int pageSize, String keyword, String role) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> page = userRepository.findByConditions(keyword, role, pageable);
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                pageNum,
                pageSize
        );
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public List<User> getDoctorList() {
        return userRepository.findByRoleAndStatus("DOCTOR", 1);
    }

    @Transactional
    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User existing = getUserById(id);

        if (request.getRealName() != null) {
            existing.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            existing.setAvatar(request.getAvatar());
        }
        if (request.getRole() != null) {
            existing.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
