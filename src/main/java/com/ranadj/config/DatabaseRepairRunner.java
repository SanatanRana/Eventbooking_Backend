package com.ranadj.config;

import com.ranadj.entity.User;
import com.ranadj.entity.Role;
import com.ranadj.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class DatabaseRepairRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseRepairRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DatabaseRepairRunner(UserRepository userRepository, 
                                PasswordEncoder passwordEncoder,
                                org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking database consistency for packages and admin references...");

        // We use JDBC query to find package IDs where admin_id = 0 or admin is missing
        List<Long> invalidPackageIds = jdbcTemplate.query(
                "SELECT id FROM packages WHERE admin_id = 0 OR admin_id NOT IN (SELECT id FROM users)",
                (rs, rowNum) -> rs.getLong("id")
        );

        if (!invalidPackageIds.isEmpty()) {
            log.warn("Found {} packages with invalid/missing admin_id! Initiating database repair...", invalidPackageIds.size());

            // Find an existing admin user
            User adminUser = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .findFirst()
                    .orElse(null);

            // If no admin user exists, create a default one
            if (adminUser == null) {
                log.info("No existing admin user found. Creating a default system administrator account...");
                User defaultAdmin = User.builder()
                        .fullName("System Admin")
                        .email("admin@ranadj.com")
                        .phone("1234567890")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .isVerified(true)
                        .build();
                adminUser = userRepository.save(defaultAdmin);
                log.info("Created system admin account with email 'admin@ranadj.com' and password 'admin123'");
            }

            // Repair each invalid package
            for (Long pkgId : invalidPackageIds) {
                jdbcTemplate.update("UPDATE packages SET admin_id = ? WHERE id = ?", adminUser.getId(), pkgId);
            }
            log.info("Successfully repaired references for {} packages.", invalidPackageIds.size());
        } else {
            log.info("Database consistency check completed successfully. All packages are correctly associated.");
        }
    }
}
