package com.paletteroom.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import com.paletteroom.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean // cors 추가 
    public CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration config = new CorsConfiguration();
    	config.setAllowedOrigins(List.of("http://localhost:5173"));
    	config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    	config.setAllowedHeaders(List.of("*"));
    	config.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
    }
    

    @Bean // cors 추가 
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/artworks/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}