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

### パッケージ分割の基準: 業務単位 × レイヤー

**第1階層: 業務単位**でパッケージを切り、その中を**レイヤードアーキテクチャ**で構成する。

> **業務の定義**: ユーザーが一連として行う作業のまとまり。  
> 例）案件に対して「新規作成・一覧確認・詳細確認・更新」を行うなら、それらを一まとめにして一つの業務とする。  
> 複数のドメインをまたぐケースでも、ユーザーの業務としての結びつきが強ければ同一パッケージに収める。

```
demo/
├── config/                     … Security など横断設定
├── common/
│   ├── enums/                  … 全 Enum（業務・レイヤーを問わず全てここに集約）
│   └── mapper/                 … 単一テーブルを扱う Mapper・Entity
└── <業務名>/                   … 業務パッケージ（例: project）
    ├── controller/             … Controller レイヤ
    ├── command/                … Command レイヤ
    ├── task/                   … Task レイヤ
    └── mapper/                 … 複数テーブルを扱い業務との結びつきが強い Mapper・Entity
```

---

### 各レイヤーの責務

#### Controller レイヤ (`<業務>.controller`)

| クラス種別 | 役割 |
|---|---|
| `XxxController` | ユーザーリクエストの受け取り、セッション管理、Command レイヤの呼び出し、Model への設定 |
| `XxxForm` | 画面入力値のバインディング。Bean Validation アノテーションを付与 |
| `XxxValidator` | Form 単体では表現できない複合バリデーション（`Validator` 実装） |
| `XxxConstraint` 等 | カスタムバリデーションアノテーション |

- Command レイヤへ渡す `CommandInput` を生成するのも Controller の責務。

#### Command レイヤ (`<業務>.command`)

| クラス種別 | 役割 |
|---|---|
| `XxxCommand` | **トランザクション境界**。ユースケースと 1:1 対応。Mapper・Task を呼び出す。業務に関係のない判定処理もここ |
| `XxxCommandInput` | Command への入力値 |
| `XxxCommandOutput` | Model に設定するクラス。**プレゼンテーションロジック**（表示用フォーマット、ラベル変換など）を実装する |
| `XxxDto` | `CommandOutput` がネスト構造を持つ場合の子データ用クラス。子データにもプレゼンテーションロジックが必要な場合に実装。子データへのプレゼンテーションロジックが不要なら Entity をそのまま利用 |

#### Task レイヤ (`<業務>.task`)

| クラス種別 | 役割 |
|---|---|
| `XxxTask` | **業務ロジックの実装**。Mapper を呼び出す。原則 1 項目を返す |
| `XxxTaskInput` | Task への入力値。**入力値が 6 つ以上になる場合に実装**する（それ未満は引数で渡す） |
| `XxxTaskOutput` | Task がオブジェクトを返す必要がある場合に実装 |

#### Mapper レイヤ

| 配置 | 条件 |
|---|---|
| `common.mapper` | **単一テーブル**を扱う Mapper・Entity |
| `<業務>.mapper` | **複数テーブル**をまたぎ、業務との結びつきが強い Mapper・Entity |

#### common パッケージ

| 配置 | 内容 |
|---|---|
| `common.enums` | **全 Enum**。業務・レイヤーを問わず一切ここに集約する |
| `common.mapper` | 単一テーブルの Mapper・Entity |

---

### テンプレート構成（業務単位）

```
src/main/resources/templates/
├── fragments/          … 共通部品（ナビゲーション等）
└── <業務名>/           … 業務ごとのテンプレート（複数 HTML）
    ├── list.html
    ├── new.html
    └── detail.html
```

---

### データの流れ（概略）

```
[画面] → Form → Controller → CommandInput
                    ↓
                Command（@Transactional）
                  ├── TaskInput → Task → TaskOutput
                  └── Mapper
                    ↓
                CommandOutput（プレゼンテーションロジック）
                    ↓
                Model → [Thymeleaf テンプレート]
```

## 4. データ型規約

### 日付

| 層 | 型 | 備考 |
|---|---|---|
| 画面（Form） | `LocalDate` | Thymeleaf の日付入力から直接バインド |
| Controller → Command | `LocalDate` | そのまま `CommandInput` に渡す |
| DB（ユーザー入力日付） | `String`（`yyyyMMdd`） | 例: `"20250104"` |
| DB（登録・更新日時） | DB の `sysdate` で登録 | Java 側からは値を渡さない。INSERT/UPDATE 文で `sysdate` を直接指定 |
| Mapper | 変換責務 | `LocalDate ↔ String(yyyyMMdd)` の変換は **Mapper 層で行う**。登録・更新時は `LocalDate → String`、取得時は `String → LocalDate` に変換する |

> Entity のユーザー入力日付フィールドは `String` で定義し、Service・Task・Command 側は `LocalDate` のまま扱う。  
> Mapper 以外の層で日付の文字列変換を行わない。  
> 登録・更新日時は Java 側でセットせず、SQL の `sysdate` に任せる。

## 5. 画面共通要件

- ベースパス: `/page`
- ルート `/` は案件一覧（`/page/projects`）にリダイレクトする。
- 登録・更新完了時はフラッシュメッセージ（`RedirectAttributes`）で結果を通知し、詳細画面へリダイレクトする（PRG パターン）。
- バリデーションエラー時はフォーム画面を再描画し、Thymeleaf の `th:errors` でエラーメッセージを表示する。

## 6. セキュリティ

現状は開発フェーズの暫定設定。**本番導入時は要再検討**。

- 認証: 未導入。全リクエスト `permitAll`。
- CSRF: 無効化中（Cookie 認証を入れる場合は再有効化を検討）。
- CORS: 画面のみのため現時点では設定不要。

## 7. 未確定・今後インプット待ちの要件

案件から情報が入り次第、ここから各項目を確定させて該当セクション・機能ドキュメントに反映する。

- [ ] 認証・認可方式（フォームログイン？ OAuth2/OIDC？ 権限ロール体系？）
- [ ] 永続化先（RDBMS の種類、スキーマ設計、マイグレーションツール）
- [ ] 案件（プロジェクト）エンティティの正式な項目定義（現状は name のみ）
- [ ] 更新・削除系のユースケースと楽観ロック方式（`version` カラムは既に用意済み）
- [ ] ページング: 総件数・ページナビゲーションの要否
- [x] 日付の DB 格納フォーマット → ユーザー入力日付は `yyyyMMdd`（String）、登録・更新日時は `sysdate`
