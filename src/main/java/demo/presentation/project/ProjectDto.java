package demo.presentation.project;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "案件情報")
public record ProjectDto(
        @Schema(description = "案件ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long projectId,
        @Schema(description = "案件名", example = "2025年度 園児管理システム刷新")
        String name,
        @Schema(description = "バージョン", example = "1")
        Integer version,
        @Schema(description = "作成日時", example = "2025-01-10T18:00:00+09:00")
        OffsetDateTime createDate,
        @Schema(description = "更新日時", example = "2025-03-02T00:30:00+09:00")
        OffsetDateTime updateDate
) {}
