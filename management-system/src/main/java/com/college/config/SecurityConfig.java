package com.college.config;

import com.college.security.UserDetailsServiceImpl;
import com.college.security.JwtAuthorizationFilter;
import com.college.util.JwtUtils;
import com.college.repository.UserRepository;
import com.college.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        // 设置隐藏用户未找到异常，避免暴露用户信息
        authProvider.setHideUserNotFoundExceptions(false);

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // 前端地址
        configuration.addAllowedOrigin("http://localhost:3001"); // 备用前端地址
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        // 添加必要的响应头
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Access-Control-Allow-Origin");
        configuration.addExposedHeader("Access-Control-Allow-Credentials");
        configuration.addExposedHeader("X-Requested-With");
        configuration.addExposedHeader("Content-Type");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 不再将JwtAuthorizationFilter定义为@Bean，而是直接在filterChain中创建
    // @Bean
    // public JwtAuthorizationFilter jwtAuthorizationFilter() {
    //     return new JwtAuthorizationFilter(jwtUtils, userDetailsService);
    // }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()  // 允许认证相关请求
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()  // 允许API认证相关请求
                        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll() // 允许公共请求
                        .requestMatchers(new AntPathRequestMatcher("/student/dashboard/public-test")).permitAll() // 测试端点完全公开
                        .requestMatchers(new AntPathRequestMatcher("/api/course-schedules/**")).hasAnyRole("ADMIN", "TEACHER") // 课程安排API需要ADMIN或TEACHER角色
                        .requestMatchers(new AntPathRequestMatcher("/course-schedules/**")).hasAnyRole("ADMIN", "TEACHER") // 课程安排API需要ADMIN或TEACHER角色
                        .requestMatchers(new AntPathRequestMatcher("/api/avatar/**")).permitAll() // 头像访问公开
                        .requestMatchers(new AntPathRequestMatcher("/avatar/**")).permitAll() // 头像访问公开（Vite代理后路径）
                        .requestMatchers(new AntPathRequestMatcher("/api/student/health")).permitAll() // 健康检查端点公开访问
                        .requestMatchers(new AntPathRequestMatcher("/api/student/**")).hasAnyRole("STUDENT", "ADMIN") // 学生API需要STUDENT或ADMIN角色
                        .requestMatchers(new AntPathRequestMatcher("/api/dashboard/**")).hasAnyRole("STUDENT", "ADMIN", "TEACHER") // 仪表盘API需要相应角色
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")  // admin路径需要ADMIN角色
                        .requestMatchers(new AntPathRequestMatcher("/teacher/**")).hasAnyRole("TEACHER", "ADMIN") // teacher路径需要TEACHER或ADMIN角色
                        .requestMatchers(new AntPathRequestMatcher("/api/teacher/**")).hasAnyRole("TEACHER", "ADMIN") // api teacher路径需要TEACHER或ADMIN角色
                        .requestMatchers(new AntPathRequestMatcher("/student/**")).authenticated()
                        .anyRequest().authenticated()                 // 其他请求需要认证
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                            response.getWriter().write("{\"error\": \"Access Denied\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                        }))
                .authenticationProvider(authenticationProvider());
        
        // 添加JWT授权过滤器
        http.addFilterBefore(new JwtAuthorizationFilter(jwtUtils, userDetailsService, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}