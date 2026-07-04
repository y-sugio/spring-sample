# 全体要件（システム共通）

このドキュメントは、本リポジトリのサンプルコード全体が前提とする要件・技術方針をまとめたもの。
機能別の要件は [features/](./features/) 配下に機能単位で置く。

> **運用ルール**: 案件からインプットされた要件は、会話やチャットの中に置いたままにせず、
> 必ずこのディレクトリのドキュメントに反映してからコードを書く。
> コードと要件が食い違ったら、要件ドキュメント側を正として扱い、どちらかを直す。

## 1. システム概要

- 案件（プロジェクト）を管理する業務アプリケーションのサンプル実装。
- 同一アプリケーション内に以下の2系統のインターフェースを持つ。
  - **REST API**（`/api/**`）: SPA・外部システム連携を想定した JSON API
  - **サーバサイドレンダリング画面**（`/page/**`）: JSP による画面

## 2. 技術スタック

| 項目 | 採用技術 | 備考 |
|---|---|---|
| 言語 | Java 21 | Gradle toolchain で固定 |
| フレームワーク | Spring Boot 3.5.x | spring-boot-starter-web / validation / security |
| ビュー | JSP + JSTL (Jakarta) | 実行可能 JAR で動かすため JSP は `src/main/resources/META-INF/resources/WEB-INF/jsp/` に配置 |
| CSS | Bootstrap 5 (WebJars) | |
| 永続化 | 現状はインメモリ（`Map`） | JPA 導入を見据えて `spring-boot-starter-data-jpa` は依存に追加済み |
| ビルド | Gradle | `./gradlew build` |
| API ドキュメント | springdoc-openapi | `/v3/api-docs`, `/swagger-ui` |

## 3. アーキテクチャ / パッケージ構成

レイヤードアーキテクチャ。ルートパッケージは `demo`。

```
demo/
├── config/            … Security, CORS などの横断設定
├── domain/
│   └── <feature>/     … ビジネスロジック（Service）。将来的にエンティティ・リポジトリもここ
└── presentation/
    ├── error/         … API 共通エラーハンドリング
    └── <feature>/     … Controller（REST / 画面）、リクエスト・レスポンス DTO
```

- REST 用 Controller は `@RestController`、画面用は `@Controller` として同一 feature パッケージに並置する。
- 画面表示用のモデルは DTO と分けて `XxxView` クラスを用意する（JSP/EL から getter で参照するため）。

## 4. API 共通要件

- ベースパス: `/api`
- リクエスト/レスポンスは JSON。日時は ISO-8601（オフセット付き、JST `+09:00`）で返す。
- 作成系 API は `201 Created` + `Location` ヘッダを返す。
- 入力チェックは Bean Validation（`@Valid`）で行う。
- エラーレスポンスは **RFC 7807 ProblemDetail** 形式で統一する（`ApiErrorHandler`）。
  - 404: 対象リソースなし（`NoSuchElementException` → Not Found）
  - 400: バリデーションエラー。拡張プロパティ `errors` に「フィールド名: メッセージ」の配列を積む

## 5. 画面共通要件

- ベースパス: `/page`
- ルート `/` は案件一覧（`/page/projects`）にリダイレクトする。
- 登録・更新完了時はフラッシュメッセージ（`RedirectAttributes`）で結果を通知し、詳細画面へリダイレクトする（PRG パターン）。
- 文字コードは UTF-8 を強制する。

## 6. セキュリティ / CORS

現状は開発フェーズの暫定設定。**本番導入時は要再検討**（TODO として明示的に残す）。

- 認証: 未導入。全リクエスト `permitAll`。
- CSRF: 無効化中（Cookie 認証を入れる場合は再有効化を検討）。
- CORS: `/api/**` に対して `http://localhost:*` のみ許可。
  - credentials 許可のためワイルドカードでなく origin patterns を使用。
  - `Location` ヘッダを expose（作成 API のレスポンス参照用）。
- OPTIONS（プリフライト）は全許可。

## 7. 未確定・今後インプット待ちの要件

案件から情報が入り次第、ここから各項目を確定させて該当セクション・機能ドキュメントに反映する。

- [ ] 認証・認可方式（フォームログイン？ OAuth2/OIDC？ 権限ロール体系？）
- [ ] 永続化先（RDBMS の種類、スキーマ設計、マイグレーションツール）
- [ ] 案件（プロジェクト）エンティティの正式な項目定義（現状は name のみ）
- [ ] 更新・削除系のユースケースと楽観ロック方式（`version` カラムは既に用意済み）
- [ ] ページングレスポンス形式（現状は配列のみ。総件数を返すか）
- [ ] 本番向け CORS / セキュリティヘッダ要件
