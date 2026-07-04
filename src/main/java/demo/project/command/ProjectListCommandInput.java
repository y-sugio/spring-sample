package demo.project.command;

/** 案件一覧検索ユースケースへの入力値。Controller が Form から生成する */
public record ProjectListCommandInput(
        String q,
        int page,
        int size
) {
}
