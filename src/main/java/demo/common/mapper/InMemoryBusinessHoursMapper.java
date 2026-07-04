package demo.common.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * BusinessHoursMapper のインメモリ仮実装。
 * TODO: 業務サービス状況テーブルの定義確定後に MyBatis 実装へ置き換える。
 *       それまでは常に業務時間内として扱う（app.business-hours.force-closed=true で
 *       業務時間外の動作を確認できる）。
 */
@Repository
public class InMemoryBusinessHoursMapper implements BusinessHoursMapper {

    private final boolean forceClosed;

    public InMemoryBusinessHoursMapper(
            @Value("${app.business-hours.force-closed:false}") boolean forceClosed) {
        this.forceClosed = forceClosed;
    }

    @Override
    public boolean isBusinessHours() {
        return !forceClosed;
    }
}
