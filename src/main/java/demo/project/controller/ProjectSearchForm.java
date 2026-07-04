package demo.project.controller;

/**
 * 案件検索フォーム。
 * 検索と登録は入力チェックが異なるため、登録用（ProjectForm）とは別に定義する
 * （coding-rules.md §3）。検索は必須項目なし。
 */
public record ProjectSearchForm(
        String q
) {
}
