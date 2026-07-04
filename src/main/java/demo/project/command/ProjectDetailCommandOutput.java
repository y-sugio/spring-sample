package demo.project.command;

import demo.common.mapper.ProjectEntity;
import demo.common.util.Formatters;

/**
 * 案件詳細表示ユースケースの出力。
 * プレゼンテーションロジック（日時の表示整形）はここで実装する。
 */
public record ProjectDetailCommandOutput(
        Long projectId,
        String name,
        Integer version,
        String createDateDisplay,
        String updateDateDisplay
) {

    public static ProjectDetailCommandOutput from(ProjectEntity entity) {
        return new ProjectDetailCommandOutput(
                entity.getProjectId(),
                entity.getName(),
                entity.getVersion(),
                Formatters.dateTime(entity.getCreateDate()),
                Formatters.dateTime(entity.getUpdateDate())
        );
    }
}
