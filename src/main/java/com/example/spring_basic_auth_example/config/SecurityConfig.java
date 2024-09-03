package com.example.spring_basic_auth_example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity //Настройка базовой веб безопасности
@EnableMethodSecurity //Поддержка безопасности на уровне метода
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "inMemory")
    public PasswordEncoder inMemoryPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    //Загрузка пользователей и их роли
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "inMemory")
    public UserDetailsService inMemoryUserDetailService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        //Храним пользователей в приложении
        manager.createUser(User.withUsername("user")
                .password("user")
                .roles("USER")
                .build());

        manager.createUser(User.withUsername("admin")
                .password("admin")
                .roles("ADMIN", "USER")
                .build());
        return manager;
    }

    //Аутентификация
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "inMemory")
    public AuthenticationManager inMemoryAuthenticationManager(
                    HttpSecurity http,
                    UserDetailsService inMemoryUserDetailService) throws Exception {

        var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.userDetailsService(inMemoryUserDetailService);

        return authManagerBuilder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "db")
    public PasswordEncoder passwordEncoder(){ //Кодирование паролей
        return new BCryptPasswordEncoder(12);
    }


    //Аутентификация в БД
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "db")
    public AuthenticationManager databaseAuthenticationManager(HttpSecurity http,
                                                               UserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) throws Exception {
        var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.userDetailsService(userDetailsService);

        var authProvider = new DaoAuthenticationProvider(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);

        authManagerBuilder.authenticationProvider(authProvider);

        return authManagerBuilder.build();
    }


    //Отвечает за основные настройки безопасности
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/user/**") //Определяем правила авторизации
                .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN")
                .requestMatchers("/api/v1/public/**").permitAll() //Разрешено всем пользователям
                .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable) //Отключаем CSRF
                .httpBasic(Customizer.withDefaults()) //Базовая аутентификация
                .sessionManagement(httpSecuritySessionManagementConfigurer -> //Управление сессии
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(authenticationManager);

        return http.build();
    }
}
