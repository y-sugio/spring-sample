# 機能要件: 案件（プロジェクト）管理

- ステータス: 実装済み（インメモリ実装。永続化は未確定 → [overview.md §6](../overview.md#6-未確定今後インプット待ちの要件)）
- 関連コード:
  - 画面: `src/main/java/demo/presentation/project/ProjectPageController.java`
  - ロジック: `src/main/java/demo/domain/project/ProjectService.java`
  - テンプレート: `src/main/resources/templates/projects/`

## 1. 概要

案件（プロジェクト）の一覧・詳細参照と新規作成を Thymeleaf 画面で提供する。

## 2. データ項目

| 項目 | 型 | 必須 | 制約 / 備考 |
|---|---|---|---|
| projectId | Long | ○ | サーバ採番（連番） |
| name | String | ○ | 案件名。最大 100 文字、空白のみ不可 |
| version | Integer | ○ | 楽観ロック用。新規作成時は 1（更新機能は未実装） |
| createDate | OffsetDateTime | ○ | 作成日時。サーバ側で JST を設定 |
| updateDate | OffsetDateTime | ○ | 更新日時。作成時は createDate と同値 |

## 3. 画面仕様（`/page/projects`）

| 画面 | パス | 説明 |
|---|---|---|
| 一覧 | GET `/page/projects` | 検索キーワード `q`・`page`（デフォルト size=20）で絞り込み、更新日時降順で表示 |
| 新規作成フォーム | GET `/page/projects/new` | 案件名の入力フォーム |
| 作成実行 | POST `/page/projects` | バリデーションエラー時はフォームを再描画。成功時は詳細画面へリダイレクト + フラッシュメッセージ「案件を作成しました」 |
| 詳細 | GET `/page/projects/{projectId}` | 案件 1 件の詳細表示 |

### 検索・ソート（一覧）

| クエリパラメータ | デフォルト | 説明 |
|---|---|---|
| q | なし | 案件名の部分一致（大文字小文字を区別しない）。未指定なら全件 |
| page | 0 | 0 始まりのページ番号 |
| size | 20 | 1 ページ件数（固定。画面から変更不可） |

- ソートは更新日時降順で固定（画面からは変更不可）。
- 範囲外ページは空リストを表示（エラーにしない）。

### バリデーション（作成フォーム）

- `name`: 必須、最大 100 文字（`@NotBlank` + `@Size(max=100)`）。
- エラー時は `th:errors` でフィールドの下にメッセージを表示し、入力値を保持したままフォームを再描画する。

## 4. 未実装 / 未確定

- 更新・削除（楽観ロックの `version` 突合方式を含む）
- name 以外の項目（案件からの項目定義インプット待ち）
- ページナビゲーション UI（総件数・前後ページリンク）
- 永続化（現状はアプリ再起動でデータ消失。シードデータ 3 件を初期投入）
