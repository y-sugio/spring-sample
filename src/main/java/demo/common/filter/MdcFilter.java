package demo.common.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * リクエストごとにトラッキング ID・ユーザー ID・クライアント IP を MDC にセットする。
 * ログパターン（application.properties）で全ログ行に出力される。
 */
@Component
@Order(1)   // 最初に MDC をセットする（BusinessHoursFilter より前）
public class MdcFilter implements Filter {

    /** SAML 認証導入時にログイン ID を格納するセッションキー */
    public static final String SESSION_USER_ID = "userId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        MDC.put("trackingId", UUID.randomUUID().toString());
        MDC.put("clientIp", req.getRemoteAddr());
        MDC.put("userId", resolveUserId(req));
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute(SESSION_USER_ID);
            if (userId != null) {
                return userId.toString();
            }
        }
        // SAML 認証が未導入のため、未認証時は "-" を出力する
        return "-";
    }
}
