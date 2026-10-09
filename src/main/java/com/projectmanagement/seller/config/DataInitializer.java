//package com.projectmanagement.seller.config;
//
//import com.projectmanagement.seller.entity.ProjectStatus;
//import com.projectmanagement.seller.entity.Role;
//import com.projectmanagement.seller.entity.SalesLevel;
//import com.projectmanagement.seller.entity.User;
//import com.projectmanagement.seller.repository.ProjectStatusRepository;
//import com.projectmanagement.seller.repository.RoleRepository;
//import com.projectmanagement.seller.repository.SalesLevelRepository;
//import com.projectmanagement.seller.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//    private final RoleRepository roleRepository;
//    private final SalesLevelRepository salesLevelRepository;
//    private final ProjectStatusRepository projectStatusRepository;
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//
//        seedAdminUser();
//    }
//
//    private void seedAdminUser() {
//        if (userRepository.findByEmailIgnoreCase("admin").isEmpty()) {
//            Role adminRole = roleRepository.findByNameIgnoreCase("ADMIN")
//                    .orElseGet(() -> roleRepository
//                            .save(Role.builder().name("ADMIN").description("System Administrator").build()));
//
//            SalesLevel bronzeLevel = salesLevelRepository.findByLevelNameIgnoreCase("Bronze")
//                    .orElseGet(() -> salesLevelRepository.findAll().stream().findFirst().orElse(null));
//
//            User admin = User.builder()
//                    .name("Admin")
//                    .email("admin")
//                    .phone("1234567890")
//                    .passwordHash(passwordEncoder.encode("admin"))
//                    .role(adminRole)
//                    .salesLevel(bronzeLevel)
//                    .status("ACTIVE")
//                    .totalProjects(0)
//                    .totalCommission(BigDecimal.ZERO)
//                    .totalRoyalty(BigDecimal.ZERO)
//                    .build();
//
//            userRepository.save(admin);
//            log.info("Initialized default admin user: username=admin, password=admin, role=ADMIN");
//        }
//    }
//}
