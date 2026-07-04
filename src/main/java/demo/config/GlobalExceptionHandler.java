package demo.config;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import demo.common.exception.SystemException;

/**
 * システム例外の共通ハンドラ。
 * SystemException を一括キャッチし、共通エラー画面にメッセージを表示する。
 * BusinessException はここでは処理しない（Controller でキャッチする）。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(SystemException.class)
    public String handleSystem(SystemException ex, Model model) {
        log.error("システム例外が発生しました messageId={}", ex.getMessageId(), ex);
        model.addAttribute("errorMessage",
                messageSource.getMessage(ex.getMessageId(), null, Locale.getDefault()));
        return "error/system";
    }
}
