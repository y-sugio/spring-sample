package demo.common.mapper;

/**
 * 業務サービス状況テーブル（単一テーブル）の Mapper。
 * TODO: テーブル名・カラム定義・業務時間の判定ロジックが未確定（requirements/overview.md §7）。
 *       確定後に MyBatis 実装（SQL）へ置き換える。
 */
public interface BusinessHoursMapper {

    /** 現在が業務時間内かどうかを業務サービス状況テーブルから判定する */
    boolean isBusinessHours();
}
