package demo.project.command;

/** 案件登録ユースケースの出力。リダイレクト先 URL の組み立てに使う */
public record ProjectCreateCommandOutput(
        Long projectId
) {
}
