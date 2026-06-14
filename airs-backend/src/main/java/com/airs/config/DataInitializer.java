package com.airs.config;

import com.airs.entity.User;
import com.airs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfNotExists("admin", "123456", "ADMIN", "系统管理员");
        createUserIfNotExists("doc", "123456", "DOCTOR", "队医");
        createUserIfNotExists("therapist", "123456", "THERAPIST", "康复师");
        createUserIfNotExists("coach", "123456", "COACH", "教练");
        createUserIfNotExists("player", "123456", "ATHLETE", "运动员");
    }

    private void createUserIfNotExists(String username, String password, String role, String realName) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setRealName(realName);
            user.setStatus(1);
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
            userRepository.save(user);
        }
    }
}
