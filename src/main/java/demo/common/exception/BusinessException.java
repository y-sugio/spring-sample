package demo.common.exception;

/**
 * 業務例外。業務ルール違反があり、CommandOutput を返す通常ルートで
 * 結果を返せない場合に throw する。
 * GlobalExceptionHandler では処理せず、必ず Controller でキャッチして
 * Model にメッセージを設定し、元の画面を再描画する。
 */
public class BusinessException extends RuntimeException {

    private final String messageId;

    public BusinessException(String messageId) {
        super(messageId);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
