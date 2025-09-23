package demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())        // ★ CORS を Security で有効化
                .csrf(AbstractHttpConfigurer::disable)           // Dev中はCSRFを無効（Cookie認証を入れるなら要再検討）
                .authorizeHttpRequests(auth -> auth
                        // プリフライトを通す
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // API と OpenAPI/Swagger UI は誰でもアクセス可（必要なら締めてOK）
                        .requestMatchers("/api/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}