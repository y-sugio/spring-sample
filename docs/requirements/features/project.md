# 機能要件: 案件（プロジェクト）管理

- ステータス: 実装済み（インメモリ実装。永続化は未確定 → [overview.md §7](../overview.md#7-未確定今後インプット待ちの要件)）
- 関連コード:
  - Controller: `src/main/java/demo/project/controller/`
  - Command: `src/main/java/demo/project/command/`
  - Task: `src/main/java/demo/project/task/`
  - Mapper: `src/main/java/demo/common/mapper/`（単一テーブルのため common）
  - テンプレート: `src/main/resources/templates/projects/`

## 1. 概要

案件（プロジェクト）の一覧・詳細参照と新規作成を Thymeleaf 画面で提供する。
実装はレイヤー別サンプルの規範になっている（[coding-rules.md §12](../../coding-rules.md)）。

## 2. データ項目

| 項目 | 型 | 必須 | 制約 / 備考 |
|---|---|---|---|
| projectId | Long | ○ | サーバ採番（連番） |
| name | String | ○ | 案件名。最大 100 文字、空白のみ不可 |
| version | Integer | ○ | 楽観ロック用。新規作成時は 1（更新機能は未実装） |
| createDate | LocalDateTime | ○ | 登録日時。DB では `sysdate` で登録（アプリから値を渡さない） |
| updateDate | LocalDateTime | ○ | 更新日時。DB では `sysdate` で登録。作成時は createDate と同値 |

## 3. 業務ルール

| ルール | 内容 | 実装 |
|---|---|---|
| 案件名の重複禁止 | 同名の案件は登録できない。重複時は `BIZ002` を表示してフォーム再描画 | `ProjectDuplicateCheckTask` → `BusinessException("BIZ002")` |

> 重複禁止ルールは**サンプル実装のための仮ルール**（案件要件としては未確定）。

## 4. 画面仕様（`/page/projects`）

| 画面 | パス | 説明 |
|---|---|---|
| 一覧 | GET `/page/projects` | 検索キーワード `q`・`page`（デフォルト size=20）で絞り込み、更新日時降順で表示 |
| 新規作成フォーム | GET `/page/projects/new` | 案件名の入力フォーム |
| 作成実行 | POST `/page/projects` | バリデーションエラー・業務エラー時はフォームを再描画。成功時は詳細画面へリダイレクト + 完了メッセージ（`MSG001`） |
| 検索・登録画面 | GET `/page/projects/entry` | 1 画面で検索・登録の 2 アクションを持つ（画面遷移パターン B・C のサンプル）。一覧からの「戻る」時はセッションから検索条件を復元して表示 |
| 検索・登録画面: 検索実行 | GET `/page/projects`（entry の検索フォームから） | パターン B: 一覧へ遷移 |
| 検索・登録画面: 登録実行 | POST `/page/projects/entry` | パターン C: 成功時は**自画面へリダイレクト** + 完了メッセージ（`MSG001`）。バリデーションエラー・業務エラー時は自画面を再描画 |
| 詳細 | GET `/page/projects/{projectId}` | 案件 1 件の詳細表示。該当なしは `BIZ001` をフラッシュメッセージにして一覧へリダイレクト |

### 検索・ソート（一覧）

| クエリパラメータ | デフォルト | 説明 |
|---|---|---|
| q | なし | 案件名の部分一致（大文字小文字を区別しない）。未指定なら全件 |
| page | 0 | 0 始まりのページ番号 |
| size | 20 | 1 ページ件数（固定。画面から変更不可） |

- ソートは更新日時降順で固定（画面からは変更不可）。
- 範囲外ページは空リストを表示（エラーにしない）。

### バリデーション（作成フォーム）

- `name`: 必須（`VAL001`）、最大 100 文字（`VAL002`）。`ProjectForm` のアノテーションで実装。
- エラー時は `th:errors` でフィールドの下にメッセージを表示し、入力値を保持したままフォームを再描画する。
- 検索フォーム（`ProjectSearchForm`）は必須項目なし（検索と登録でバリデーションを分ける）。

## 5. 未実装 / 未確定

- 更新・削除（楽観ロックの `version` 突合方式を含む）
- name 以外の項目（案件からの項目定義インプット待ち）
- ページナビゲーション UI（総件数・前後ページリンク）
- 永続化（現状はインメモリ仮実装 `InMemoryProjectMapper`。アプリ再起動でデータ消失。シードデータ 3 件を初期投入）
- 案件名の重複禁止ルールの正式確定（現状はサンプル用の仮ルール）

## 6. 仮実装（未決事項を TODO コメントで残して実装済み）

- 「戻る」時の検索条件・ページング復元: セッション保持で仮実装（詳細画面の戻るリンクに `?restore=1`）。
  実現方式（セッション vs hidden 引き回し）の確定待ち。
