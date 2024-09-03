package com.example.spring_basic_auth_example.service;

import com.example.spring_basic_auth_example.entity.Role;
import com.example.spring_basic_auth_example.entity.User;
import com.example.spring_basic_auth_example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User createNewAccount(User user, Role role){
        user.setRoles(Collections.singletonList(role));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        role.setUser(user);
        return userRepository.saveAndFlush(user);
    }

    public User findByUserName(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Такого пользователя не существует!"));
    }
}
