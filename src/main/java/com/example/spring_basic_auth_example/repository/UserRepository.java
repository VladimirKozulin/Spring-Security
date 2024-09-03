package com.example.spring_basic_auth_example.repository;

import com.example.spring_basic_auth_example.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"roles"}) //Считывание роли из БД
    Optional<User> findByUsername(String username);
}
