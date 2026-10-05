package co.edu.unisimon.corenotificacion.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import static org.springframework.security.config.Customizer.withDefaults;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:3002}")
    private List<String> allowedOrigins;

    @Value("${swagger.security.username:swagger_admin}")
    private String swaggerUsername;

    @Value("${swagger.security.password}")
    private String swaggerPassword;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With",
                "X-Sistema-Uuid", "X-Sede-Uuid", "X-User-Uuid"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        UserDetails swaggerUser = User.builder()
                .username(swaggerUsername)
                .password(swaggerPassword)
                .authorities("ROLE_SWAGGER_ADMIN")
                .build();

        UserDetailsService swaggerUserDetailsService = new InMemoryUserDetailsManager(swaggerUser);

        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui", "/swagger-ui.html", "/swagger-ui/index.html")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAuthority("ROLE_SWAGGER_ADMIN"))
                .httpBasic(withDefaults())
                .userDetailsService(swaggerUserDetailsService);
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain proveedorApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher("/status", "/status/error-test", "/error", "/actuator/**", "/archivo/view/**", "/ws-chat/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/status", "/status/error-test", "/error", "/actuator/**", "/archivo/view/**", "/ws-chat/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}