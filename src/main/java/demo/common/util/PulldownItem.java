package demo.common.util;

/**
 * プルダウン（select）の選択肢 1 件。
 * 選択肢は DB（マスタテーブル）または Enum から取得し、CommandOutput で
 * List&lt;PulldownItem&gt; に変換して fragments/pulldown.html に渡す（architecture.md §3）。
 *
 * TODO: 利用箇所は案件エンティティの項目定義（区分値）確定後に追加する。
 *       Enum 由来の例: Arrays.stream(StatusEnum.values())
 *                          .map(e -> new PulldownItem(e.getCode(), e.getLabel())).toList()
 */
public record PulldownItem(
        String value,
        String label
) {
}
