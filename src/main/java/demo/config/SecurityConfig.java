package demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * セキュリティ設定。
 * TODO: SAML 認証（Azure Entra ID）導入時は SamlConfig を有効化し、このチェーンを置き換える。
 *       認可ルール（ユーザー種別による画面・操作の制限）は未確定（requirements/overview.md §7）。
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // TODO: SAML 導入時は SAML エンドポイント（/login/saml2/sso/**）のみ CSRF 除外し、
                //       画面側の CSRF は有効化を検討する
                .csrf(AbstractHttpConfigurer::disable)
                // セッションタイムアウト: 無効なセッション ID のリクエストをタイムアウト画面へ
                .sessionManagement(session -> session
                        .invalidSessionUrl("/error/session-timeout"))
                .authorizeHttpRequests(auth -> auth
                        // TODO: 認可ルール確定後、ユーザー種別に応じた制限を設定する
                        .anyRequest().permitAll());
        return http.build();
    }
}
