package demo.common.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import demo.common.db.DbCall;
import demo.common.mapper.BusinessHoursMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * リクエストのたびに業務サービス状況テーブルを参照し、
 * 業務時間外であれば業務時間外エラー画面へ遷移させるフィルタ。
 * MdcFilter（@Order(1)）の後に実行する。
 */
@Component
@Order(2)
public class BusinessHoursFilter implements Filter {

    /**
     * チェック対象外パス。
     * TODO: 除外パスは未確定（requirements/overview.md §7）。SAML 認証エンドポイント
     *       （/login/saml2/**、/saml2/**）は認証フロー保護のため、導入時に必ず追加する。
     */
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/error/",
            "/webjars/",
            "/favicon.ico"
    );

    private final BusinessHoursMapper businessHoursMapper;
    private final DbCall dbCall;

    public BusinessHoursFilter(BusinessHoursMapper businessHoursMapper, DbCall dbCall) {
        this.businessHoursMapper = businessHoursMapper;
        this.dbCall = dbCall;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (isExcluded(path)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isOpen = dbCall.execute("SYS001", businessHoursMapper::isBusinessHours);
        if (!isOpen) {
            req.getRequestDispatcher("/error/business-hours").forward(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isExcluded(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }
}
