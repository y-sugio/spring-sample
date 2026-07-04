package demo.project.command;

import demo.common.mapper.ProjectEntity;
import demo.common.util.Formatters;

/**
 * 一覧の 1 行分（CommandOutput の子データ）。
 * 子データにプレゼンテーションロジック（日時の表示整形）が必要なため Dto を実装する
 * （不要であれば Entity をそのまま使う。coding-rules.md §2）。
 */
public record ProjectDto(
        Long projectId,
        String name,
        Integer version,
        String createDateDisplay,
        String updateDateDisplay
) {

    public static ProjectDto from(ProjectEntity entity) {
        return new ProjectDto(
                entity.getProjectId(),
                entity.getName(),
                entity.getVersion(),
                Formatters.dateTime(entity.getCreateDate()),
                Formatters.dateTime(entity.getUpdateDate())
        );
    }
}
