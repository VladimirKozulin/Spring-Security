package com.example.spring_basic_auth_example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping
    public ResponseEntity<String> getPublic() { //Не защищённый метод - доступен всем
        return ResponseEntity.ok("Вызван публичный метод");
    }
}
