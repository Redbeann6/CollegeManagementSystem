package com.college.diagnostics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DependencyTest implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Dependency Test Started ===");
        
        try {
            // Test if we can retrieve the key components without circular dependency errors
            System.out.println("1. Retrieving SecurityConfig...");
            Object securityConfig = applicationContext.getBean("securityConfig");
            System.out.println("✓ SecurityConfig retrieved successfully");
            
            System.out.println("2. Retrieving AuthServiceImpl...");
            Object authServiceImpl = applicationContext.getBean("authServiceImpl");
            System.out.println("✓ AuthServiceImpl retrieved successfully");
            
            System.out.println("3. Retrieving AuthenticationManager...");
            Object authenticationManager = applicationContext.getBean("authenticationManager");
            System.out.println("✓ AuthenticationManager retrieved successfully");
            
            System.out.println("4. Retrieving JwtUtils...");
            Object jwtUtils = applicationContext.getBean("jwtUtils");
            System.out.println("✓ JwtUtils retrieved successfully");
            
            System.out.println("5. Retrieving UserDetailsServiceImpl...");
            Object userDetailsService = applicationContext.getBean("userDetailsServiceImpl");
            System.out.println("✓ UserDetailsServiceImpl retrieved successfully");
            
            System.out.println("\n=== All components retrieved successfully! Circular dependency issue is resolved. ===");
            
        } catch (Exception e) {
            System.err.println("\n✗ Dependency test failed:");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
