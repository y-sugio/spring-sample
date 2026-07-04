# 機能要件: 案件（プロジェクト）管理

- ステータス: 実装済み（インメモリ実装。永続化は未確定 → [overview.md §7](../overview.md#7-未確定今後インプット待ちの要件)）
- 関連コード:
  - REST: `src/main/java/demo/presentation/project/ProjectController.java`
  - 画面: `src/main/java/demo/presentation/project/ProjectPageController.java`
  - ロジック: `src/main/java/demo/domain/project/ProjectService.java`
  - JSP: `src/main/resources/META-INF/resources/WEB-INF/jsp/projects/`

## 1. 概要

案件（プロジェクト）の一覧・詳細参照と新規作成を提供する。REST API と JSP 画面の両方から同一の Service を利用する。

## 2. データ項目

| 項目 | 型 | 必須 | 制約 / 備考 |
|---|---|---|---|
| projectId | Long | ○ | サーバ採番（連番） |
| name | String | ○ | 案件名。最大 100 文字、空白のみ不可 |
| version | Integer | ○ | 楽観ロック用。新規作成時は 1（更新機能は未実装） |
| createDate | OffsetDateTime | ○ | 作成日時。サーバ側で JST を設定 |
| updateDate | OffsetDateTime | ○ | 更新日時。作成時は createDate と同値 |

## 3. REST API 仕様

### 3.1 一覧取得 `GET /api/projects`

| クエリ | デフォルト | 説明 |
|---|---|---|
| q | なし | 案件名の部分一致（大文字小文字を区別しない）。未指定なら全件 |
| page | 0 | 0 始まりのページ番号 |
| size | 10 | 1 ページ件数 |
| sortKey | updateDate | projectId / name / version / createDate / updateDate。不正値は updateDate 扱い |
| sortDir | desc | asc / desc |

- レスポンス: `ProjectDto` の JSON 配列（ページ情報のエンベロープなし ※総件数の要否は未確定要件）。
- ソートは null を末尾に寄せる。name のソートは大文字小文字を区別しない。
- 範囲外ページは空配列を返す（エラーにしない）。

### 3.2 詳細取得 `GET /api/projects/{projectId}`

- 200: `ProjectDto` を返す。
- 404: 該当なしの場合 ProblemDetail（title: "Not Found"）。

### 3.3 作成 `POST /api/projects`

- リクエスト: `{ "name": "..." }`（`CreateProjectRequest`。§2 の name 制約でバリデーション）
- 201: 作成した `ProjectDto` を body で返し、`Location: /api/projects/{projectId}` を付与。
- 400: バリデーションエラー時 ProblemDetail + `errors` 配列。

リクエスト/レスポンス例: `src/main/resources/openapi/examples/` 参照。

## 4. 画面仕様（`/page/projects`）

| 画面 | パス | 内容 |
|---|---|---|
| 一覧 | GET `/page/projects` | 検索キーワード `q`・`page`（デフォルト size=20）で絞り込み、更新日時降順で表示 |
| 新規作成フォーム | GET `/page/projects/new` | 案件名の入力フォーム |
| 作成実行 | POST `/page/projects` | 作成後、詳細画面へリダイレクト + フラッシュメッセージ「案件を作成しました」 |
| 詳細 | GET `/page/projects/{projectId}` | 案件 1 件の詳細表示 |

## 5. 未実装 / 未確定

- 更新・削除（楽観ロックの `version` 突合方式を含む）
- name 以外の項目（案件からの項目定義インプット待ち）
- 画面側フォームのサーバサイドバリデーションエラー表示（現状 API 経由のみ整備）
- 永続化（現状はアプリ再起動でデータ消失。シードデータ 3 件を初期投入）
