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
| `common.util` | **共通フォーマッター**等のユーティリティ。日付の `yyyy/MM/dd` 整形、数値の 3 桁カンマ区切り変換など |
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

### 表示フォーマット（CommandOutput での変換）

画面表示用の整形は **CommandOutput（および Dto）のプレゼンテーションロジック内**で行う。
整形処理は `common.util` に置いた共通フォーマッタークラスを呼び出す。

| データ種別 | DB / Java 内部値 | 画面表示形式 | 変換箇所 |
|---|---|---|---|
| ユーザー入力日付 | `String`（`yyyyMMdd`）または `LocalDate` | `yyyy/MM/dd` | `CommandOutput` / `Dto` |
| 数値（金額・数量等） | `Integer` / `Long` / `BigDecimal` | 3 桁カンマ区切り（例: `1,234,567`） | `CommandOutput` / `Dto` |
| 区分値 | `String`（DB のコード値） | Enum のラベル（表示名） | `CommandOutput` / `Dto` |

**共通フォーマッタークラスの配置**: `demo.common.util.Formatter`（クラス名は仮。確定次第更新）

> Controller・Task・Command の計算ロジック内では整形を行わない。
> 整形済み文字列を受け取る Thymeleaf テンプレートは `th:text` で表示するだけでよい。

### 区分値（Enum）

DB はコード値（`String`）で管理する。Enum はコードを保持し、`CommandOutput` / `Dto` 内でコード → Enum に変換して表示ラベルを取得する。

**Enum の実装規約**:
- 全 Enum は `common.enums` パッケージに配置する（再掲）。
- 各 Enum はコード値フィールドと表示ラベルフィールドを持つ。
- コードから Enum を引くファクトリメソッド（例: `fromCode(String code)`）を実装する。

```java
// 実装イメージ（common.enums）
public enum StatusEnum {
    ACTIVE("1", "有効"),
    INACTIVE("2", "無効");

    private final String code;
    private final String label;

    public static StatusEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown code: " + code));
    }
}

// CommandOutput での使用イメージ
public String getStatusLabel() {
    return StatusEnum.fromCode(this.statusCode).getLabel();
}
```

> Entity はコード値（`String`）のまま保持する。Enum への変換は CommandOutput / Dto 内でのみ行う。

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

## 5. メッセージ管理

### プロパティファイル管理

メッセージは `src/main/resources/messages.properties` で一元管理する。

**フォーマット**:
```properties
メッセージID=メッセージID メッセージ本文
```

### メッセージ ID の種別プレフィックス

メッセージは以下の4種別に分類し、プレフィックスで識別する。
プレフィックスの具体的な文字列は未確定（→ §8 参照）。

| 種別 | 用途 | プレフィックス例（仮） |
|---|---|---|
| 処理完了メッセージ | 登録・更新・削除等の正常完了通知 | `MSG` |
| バリデーションエラー | 入力チェックエラー（Bean Validation を含む） | `VAL` |
| 業務例外 | 業務ルール違反（例: 重複登録、存在しないデータへの操作） | `BIZ` |
| システム例外 | 予期せぬエラー（DB 接続失敗、未ハンドル例外等） | `SYS` |

記載例:
```properties
# 処理完了
MSG001=MSG001 登録が完了しました。
MSG002=MSG002 更新が完了しました。

# バリデーションエラー
VAL001=VAL001 必須項目です。
VAL002=VAL002 {0}文字以内で入力してください。

# 業務例外
BIZ001=BIZ001 該当するデータが見つかりません。
BIZ002=BIZ002 既に登録されています。

# システム例外
SYS001=SYS001 システムエラーが発生しました。管理者にお問い合わせください。
```

> 値の先頭にメッセージ ID を含めることで、画面表示時にどのメッセージかを識別しやすくする。

### 利用箇所と取得方法

- Spring の `MessageSource` を DI して `getMessage(messageId, null, locale)` で取得する。
- バリデーションエラーメッセージも同プロパティで管理し、`@NotBlank` 等のアノテーションの `message` 属性にメッセージ ID を指定する。
- Thymeleaf テンプレートからは `#{メッセージID}` 構文で直接参照できる。

## 6. 画面共通要件

- ベースパス: `/page`
- ルート `/` は案件一覧（`/page/projects`）にリダイレクトする。
- 登録・更新完了時はフラッシュメッセージ（`RedirectAttributes`）で結果を通知し、詳細画面へリダイレクトする（PRG パターン）。
- バリデーションエラー時はフォーム画面を再描画し、Thymeleaf の `th:errors` でエラーメッセージを表示する。

## 7. セキュリティ

現状は開発フェーズの暫定設定。**本番導入時は要再検討**。

- 認証: 未導入。全リクエスト `permitAll`。
- CSRF: 無効化中（Cookie 認証を入れる場合は再有効化を検討）。
- CORS: 画面のみのため現時点では設定不要。

## 8. 未確定・今後インプット待ちの要件

案件から情報が入り次第、ここから各項目を確定させて該当セクション・機能ドキュメントに反映する。

- [ ] 認証・認可方式（フォームログイン？ OAuth2/OIDC？ 権限ロール体系？）
- [ ] 永続化先（RDBMS の種類、スキーマ設計、マイグレーションツール）
- [ ] 案件（プロジェクト）エンティティの正式な項目定義（現状は name のみ）
- [ ] 更新・削除系のユースケースと楽観ロック方式（`version` カラムは既に用意済み）
- [ ] ページング: 総件数・ページナビゲーションの要否
- [ ] メッセージ ID のプレフィックス文字列（MSG / VAL / BIZ / SYS は仮）
- [x] 日付の DB 格納フォーマット → ユーザー入力日付は `yyyyMMdd`（String）、登録・更新日時は `sysdate`
