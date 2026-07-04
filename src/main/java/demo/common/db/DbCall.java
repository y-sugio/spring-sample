package demo.common.db;

import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import demo.common.exception.SystemException;

/**
 * Mapper 呼び出しの共通ラッパー。
 * Mapper は直接呼ばず、必ずこのクラス経由で呼ぶ（coding-rules.md §4）。
 * DB 例外（DataAccessException）をメッセージ ID 付きの SystemException に変換する。
 */
@Component
public class DbCall {

    /** 戻り値ありの Mapper 呼び出し（SELECT） */
    public <T> T execute(String messageId, Supplier<T> mapperCall) {
        try {
            return mapperCall.get();
        } catch (DataAccessException e) {
            throw new SystemException(messageId, e);
        }
    }

    /** 戻り値なしの Mapper 呼び出し（INSERT / UPDATE / DELETE） */
    public void execute(String messageId, Runnable mapperCall) {
        try {
            mapperCall.run();
        } catch (DataAccessException e) {
            throw new SystemException(messageId, e);
        }
    }
}
