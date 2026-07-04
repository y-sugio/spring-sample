package demo.common.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 画面表示用の共通フォーマッター。
 * 表示整形は CommandOutput / Dto からこのクラスを呼び出して行う（coding-rules.md §8）。
 */
public final class Formatters {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private Formatters() {
    }

    /** 日付 → yyyy/MM/dd */
    public static String date(LocalDate value) {
        return value == null ? "" : value.format(DATE);
    }

    /** 日時 → yyyy/MM/dd HH:mm */
    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    /** 数値 → 3 桁カンマ区切り */
    public static String number(Number value) {
        return value == null ? "" : NumberFormat.getNumberInstance(Locale.JAPAN).format(value);
    }
}
