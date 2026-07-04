package demo.project.command;

/** 案件登録ユースケースへの入力値。Controller が Form から生成する */
public record ProjectCreateCommandInput(
        String name
) {
}
