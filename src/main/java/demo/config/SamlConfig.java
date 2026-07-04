package demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * SAML 認証（Azure Entra ID）の設定スケルトン。
 *
 * 未決事項（requirements/overview.md §7）:
 * TODO: Entra ID テナントのメタデータ URL（テナント ID・アプリケーション ID）
 * TODO: SAML アサーションの属性名（Entra ID 側のクレーム設定に依存）
 * TODO: ユーザー種別の値体系（common.enums に UserTypeEnum を定義する）
 *
 * 導入手順:
 * 1. build.gradle の spring-security-saml2-service-provider と Shibboleth リポジトリを有効化
 * 2. application.properties に app.saml.enabled=true を設定
 * 3. 下記コメントアウトの実装を有効化し、TODO 箇所を確定値で置き換える
 * 4. SecurityConfig 側のチェーンに @ConditionalOnProperty(name = "app.saml.enabled",
 *    havingValue = "false", matchIfMissing = true) を付けて共存させる
 * 5. BusinessHoursFilter の除外パスに /login/saml2/**・/saml2/** を追加する
 */
@Configuration
@ConditionalOnProperty(name = "app.saml.enabled", havingValue = "true")
public class SamlConfig {

    /* TODO: SAML 依存の有効化後にコメントを外す（依存がないと現状はコンパイル不可のため全体をコメントアウト）

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        RelyingPartyRegistration registration = RelyingPartyRegistrations
                // TODO: テナント ID・アプリケーション ID を確定値に置き換える
                .fromMetadataLocation(
                        "https://login.microsoftonline.com/{tenant-id}/federationmetadata/2007-06/federationmetadata.xml?appid={app-id}")
                .registrationId("entra-id")
                .build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    @Bean
    public SecurityFilterChain samlSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .saml2Login(saml -> saml.successHandler(samlSuccessHandler()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/login/saml2/sso/**"))
                .sessionManagement(session -> session
                        .invalidSessionUrl("/error/session-timeout"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error/**", "/webjars/**").permitAll()
                        // TODO: 認可ルール確定後、ユーザー種別に応じた制限を設定する
                        .anyRequest().authenticated());
        return http.build();
    }

    // 認証成功時にアサーションからログイン ID・ユーザー種別を取り出しセッションへ格納する。
    // MdcFilter はセッションの userId を MDC に載せる。
    private AuthenticationSuccessHandler samlSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler delegate =
                new SavedRequestAwareAuthenticationSuccessHandler();
        return (request, response, authentication) -> {
            Saml2AuthenticatedPrincipal principal =
                    (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            HttpSession session = request.getSession();
            // TODO: 属性名は Entra ID のクレーム設定確定後に置き換える
            session.setAttribute(MdcFilter.SESSION_USER_ID,
                    principal.getFirstAttribute("loginId"));
            session.setAttribute("userType",
                    principal.getFirstAttribute("userType"));
            delegate.onAuthenticationSuccess(request, response, authentication);
        };
    }
    */
}
