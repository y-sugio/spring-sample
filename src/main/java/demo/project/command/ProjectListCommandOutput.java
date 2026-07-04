package demo.project.command;

import java.util.List;

/** 案件一覧検索ユースケースの出力。Model に設定して Thymeleaf から参照する */
public record ProjectListCommandOutput(
        List<ProjectDto> projects
) {
}
