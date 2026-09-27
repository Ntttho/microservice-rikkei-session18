package com.rikkeibank.identity.config;

import com.rikkeibank.identity.entity.Role;
import com.rikkeibank.identity.entity.User;
import com.rikkeibank.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users for RikkeiBank...");

            // Admin User
            User admin = User.builder()
                    .email("admin@rikkeibank.com")
                    .password(passwordEncoder.encode("Admin@123456"))
                    .fullName("System Administrator")
                    .phoneNumber("0988888888")
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_TELLER, Role.ROLE_CUSTOMER))
                    .active(true)
                    .build();
            userRepository.save(admin);

            // Teller User
            User teller = User.builder()
                    .email("teller@rikkeibank.com")
                    .password(passwordEncoder.encode("Teller@123456"))
                    .fullName("RikkeiBank Teller Staff")
                    .phoneNumber("0977777777")
                    .roles(Set.of(Role.ROLE_TELLER))
                    .active(true)
                    .build();
            userRepository.save(teller);

            // Customer User 1
            User customer1 = User.builder()
                    .email("customer1@rikkeibank.com")
                    .password(passwordEncoder.encode("Customer@123456"))
                    .fullName("Nguyen Van A")
                    .phoneNumber("0966666666")
                    .roles(Set.of(Role.ROLE_CUSTOMER))
                    .active(true)
                    .build();
            userRepository.save(customer1);

            // Customer User 2
            User customer2 = User.builder()
                    .email("customer2@rikkeibank.com")
                    .password(passwordEncoder.encode("Customer@123456"))
                    .fullName("Tran Thi B")
                    .phoneNumber("0955555555")
                    .roles(Set.of(Role.ROLE_CUSTOMER))
                    .active(true)
                    .build();
            userRepository.save(customer2);

            log.info("Initialized default users: admin@rikkeibank.com, teller@rikkeibank.com, customer1@rikkeibank.com, customer2@rikkeibank.com");
        }
    }
}
