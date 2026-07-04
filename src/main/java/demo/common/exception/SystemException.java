package demo.common.exception;

/**
 * システム例外。DB 障害・予期せぬエラー等で throw する。
 * GlobalExceptionHandler が一括キャッチして共通エラー画面へ遷移する。
 */
public class SystemException extends RuntimeException {

    private final String messageId;

    public SystemException(String messageId) {
        super(messageId);
        this.messageId = messageId;
    }

    public SystemException(String messageId, Throwable cause) {
        super(messageId, cause);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
