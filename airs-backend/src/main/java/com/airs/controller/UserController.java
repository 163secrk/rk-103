package com.airs.controller;

import com.airs.common.PageResult;
import com.airs.common.Result;
import com.airs.dto.ResetPasswordRequest;
import com.airs.dto.UserCreateRequest;
import com.airs.dto.UserUpdateRequest;
import com.airs.entity.User;
import com.airs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        PageResult<User> page = userService.getUserPage(pageNum, pageSize, keyword, role);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(id, request);
        user.setPassword(null);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return Result.success();
    }
}
