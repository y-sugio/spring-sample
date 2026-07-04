# 全体要件（システム共通）

このドキュメントは、本リポジトリのサンプルコード全体が前提とする要件・技術方針をまとめたもの。
機能別の要件は [features/](./features/) 配下に機能単位で置く。

> **運用ルール**: 案件からインプットされた要件は、会話やチャットの中に置いたままにせず、
> 必ずこのディレクトリのドキュメントに反映してからコードを書く。
> コードと要件が食い違ったら、要件ドキュメント側を正として扱い、どちらかを直す。

## 1. システム概要

- 案件（プロジェクト）を管理する業務アプリケーションのサンプル実装。
- インターフェースは **Thymeleaf によるサーバサイドレンダリング画面のみ**（`/page/**`）。
  - REST API は提供しない。

## 2. 技術スタック

| 項目 | 採用技術 | 備考 |
|---|---|---|
| 言語 | Java 21 | Gradle toolchain で固定 |
| フレームワーク | Spring Boot 3.5.x | spring-boot-starter-web / validation / security |
| ビュー | Thymeleaf | `spring-boot-starter-thymeleaf`。テンプレートは `src/main/resources/templates/` |
| CSS | Bootstrap 5 (WebJars) | |
| 永続化 | 現状はインメモリ（`Map`） | JPA 導入を見据えて `spring-boot-starter-data-jpa` は依存に追加済み |
| ビルド | Gradle | `./gradlew build` |

## 3. アーキテクチャ / パッケージ構成

レイヤードアーキテクチャ。ルートパッケージは `demo`。

```
demo/
├── config/            … Security などの横断設定
├── domain/
│   └── <feature>/     … ビジネスロジック（Service）。将来的にエンティティ・リポジトリもここ
└── presentation/
    └── <feature>/     … Controller、リクエスト DTO
```

- Controller は `@Controller`。
- Service の返り値 DTO（record）をそのまま Model に渡す。
  Thymeleaf テンプレートからはメソッド呼び出し構文 `${dto.fieldName()}` で参照する。
- 共通ナビゲーションバーは `src/main/resources/templates/fragments/nav.html` に切り出す。

## 4. 画面共通要件

- ベースパス: `/page`
- ルート `/` は案件一覧（`/page/projects`）にリダイレクトする。
- 登録・更新完了時はフラッシュメッセージ（`RedirectAttributes`）で結果を通知し、詳細画面へリダイレクトする（PRG パターン）。
- バリデーションエラー時はフォーム画面を再描画し、Thymeleaf の `th:errors` でエラーメッセージを表示する。

## 5. セキュリティ

現状は開発フェーズの暫定設定。**本番導入時は要再検討**。

- 認証: 未導入。全リクエスト `permitAll`。
- CSRF: 無効化中（Cookie 認証を入れる場合は再有効化を検討）。
- CORS: 画面のみのため現時点では設定不要。

## 6. 未確定・今後インプット待ちの要件

案件から情報が入り次第、ここから各項目を確定させて該当セクション・機能ドキュメントに反映する。

- [ ] 認証・認可方式（フォームログイン？ OAuth2/OIDC？ 権限ロール体系？）
- [ ] 永続化先（RDBMS の種類、スキーマ設計、マイグレーションツール）
- [ ] 案件（プロジェクト）エンティティの正式な項目定義（現状は name のみ）
- [ ] 更新・削除系のユースケースと楽観ロック方式（`version` カラムは既に用意済み）
- [ ] ページング: 総件数・ページナビゲーションの要否
