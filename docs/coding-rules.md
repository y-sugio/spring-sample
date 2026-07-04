# コーディングルール

このドキュメントは、コードを**どう書くか**の規約をまとめたもの。
構造・仕組みの定義は [architecture.md](./architecture.md)、満たすべき要件は [requirements/overview.md](./requirements/overview.md) を参照。

## 1. 命名規約

| レイヤ | クラス | 命名 |
|---|---|---|
| Controller | コントローラ | `XxxController` |
| Controller | フォーム | `XxxForm` |
| Controller | 複合バリデータ | `XxxValidator` |
| Command | コマンド | `XxxCommand`（ユースケースと 1:1） |
| Command | 入力 | `XxxCommandInput` |
| Command | 出力 | `XxxCommandOutput` |
| Command | 子データ | `XxxDto` |
| Task | タスク | `XxxTask` |
| Task | 入力 | `XxxTaskInput` |
| Task | 出力 | `XxxTaskOutput` |
| Mapper | マッパー | `XxxMapper` |
| Mapper | エンティティ | `XxxEntity` |

- Enum は `common.enums` に置き、`XxxEnum` とする。

## 2. クラス作成の判断基準

作る／作らないの判断はこの表に従う。

| クラス | 作る条件 |
|---|---|
| `XxxTaskInput` | Task への入力値が **6 つ以上**になる場合のみ。それ未満はメソッド引数で渡す |
| `XxxTaskOutput` | Task が**オブジェクトを返す必要がある**場合のみ。原則 Task は 1 項目を返す |
| `XxxDto` | `CommandOutput` がネスト構造を持ち、**子データにもプレゼンテーションロジックが必要**な場合のみ。不要なら Entity をそのまま子データに使う |
| `XxxValidator` | Form 単体（アノテーション）では表現できない複合バリデーションがある場合のみ |

## 3. レイヤー間の呼び出しルール

- 呼び出し方向は `Controller → Command → Task → Mapper`。逆方向・飛び越しの呼び出しをしない。
  - 例外: Command から Mapper を直接呼ぶのは可（architecture.md のデータフロー参照）。
- `@Transactional` は **Command のみ**に付ける。Task・Mapper にトランザクション境界を作らない。
- `CommandInput` の生成は **Controller の責務**。Command 側で Form を受け取らない。
- Mapper は**直接呼ばず、必ず `DbCall` 経由**で呼ぶ。

```java
// ○ 正しい呼び出し
ProjectEntity entity = dbCall.execute("SYS001", () -> projectMapper.findById(id));
dbCall.execute("SYS001", () -> projectMapper.insert(entity));

// × Mapper を直接呼ばない
ProjectEntity entity = projectMapper.findById(id);
```

## 4. 例外の使い分けルール

| 例外 | throw する条件 | キャッチする場所 |
|---|---|---|
| `SystemException` | DB 障害・予期せぬエラー。`DbCall` が自動変換するため、手動 throw は稀 | `GlobalExceptionHandler`（個別にキャッチしない） |
| `BusinessException` | 業務ルール違反があり、**CommandOutput を返す通常ルートで結果を返せない**場合 | **Controller** で try-catch |

- `BusinessException` を `GlobalExceptionHandler` で処理しない。必ず投げた Command を呼んだ Controller でキャッチし、Model にメッセージを設定して**元の画面を再描画**する。
- 例外には必ず**メッセージ ID** を渡す。

```java
// Controller での業務例外キャッチ
@PostMapping
public String create(...) {
    try {
        command.execute(input);
    } catch (BusinessException ex) {
        model.addAttribute("errorMessage", /* messageSource.getMessage(ex.getMessageId()) */);
        return "projects/new";  // 元の画面を再描画
    }
    return "redirect:/page/projects/...";
}
```

## 5. メッセージ ID 規約

- プロパティの記載フォーマット: **`メッセージID=メッセージID メッセージ本文`**（値の先頭に ID を重ねて書く）。
- 種別ごとにプレフィックスを分ける（文字列は仮。確定次第更新）:

| 種別 | 用途 | プレフィックス例（仮） |
|---|---|---|
| 処理完了メッセージ | 登録・更新・削除等の正常完了通知 | `MSG` |
| バリデーションエラー | 入力チェックエラー（Bean Validation を含む） | `VAL` |
| 業務例外 | 業務ルール違反（例: 重複登録、存在しないデータへの操作） | `BIZ` |
| システム例外 | 予期せぬエラー（DB 接続失敗、未ハンドル例外等） | `SYS` |

```properties
# 処理完了
MSG001=MSG001 登録が完了しました。

# バリデーションエラー
VAL001=VAL001 必須項目です。
VAL002=VAL002 {0}文字以内で入力してください。

# 業務例外
BIZ001=BIZ001 該当するデータが見つかりません。

# システム例外
SYS001=SYS001 システムエラーが発生しました。管理者にお問い合わせください。
```

- メッセージをコードにハードコードしない。必ず ID でプロパティから引く。

## 6. 日付の扱い

| 層 | 型 |
|---|---|
| Form / Controller / Command / Task | `LocalDate` |
| Entity（ユーザー入力日付） | `LocalDate`（TypeHandler が DB の `String(yyyyMMdd)` と自動変換） |

- **Mapper（TypeHandler）以外の層で日付の文字列変換をしない。**
- 登録日時・更新日時は Java でセットせず、**SQL 内で `sysdate`** を指定する。

## 7. 表示フォーマット規約

画面表示用の整形は **CommandOutput（および Dto）のプレゼンテーションロジック内**で行い、`common.util` の共通フォーマッターを呼び出す。

| データ種別 | 整形内容 | 整形箇所 |
|---|---|---|
| 日付 | `yyyy/MM/dd` | `CommandOutput` / `Dto` |
| 数値 | 3 桁カンマ区切り | `CommandOutput` / `Dto` |
| 区分値 | コード → Enum ラベル | `CommandOutput` / `Dto` |

- Controller・Command（計算ロジック）・Task で表示整形をしない。
- 独自の `SimpleDateFormat` / `DateTimeFormatter` / `String.format` を各所に書かず、共通フォーマッター（`demo.common.util.Formatter`、クラス名は仮）に寄せる。

## 8. Enum 規約

- 全 Enum は `common.enums` に配置する（業務パッケージに置かない）。
- コード値フィールド・表示ラベルフィールドを持たせ、`fromCode(String)` ファクトリメソッドを実装する。
- Entity は DB のコード値（`String`）のまま保持し、Enum への変換は `CommandOutput` / `Dto` 内でのみ行う。

```java
// common.enums
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

// CommandOutput での使用
public String getStatusLabel() {
    return StatusEnum.fromCode(this.statusCode).getLabel();
}
```

## 9. Thymeleaf テンプレート規約

- record にアクセスするときはメソッド呼び出し構文 `${dto.fieldName()}` を使う。
- フォームのバリデーションエラーは `BindingResult` で受け取り、`th:errors` で表示する。
- CommandOutput で整形済みの値は `th:text` で表示するだけにする（テンプレート内で `#numbers` / `#temporals` による再整形をしない）。
- メッセージは `#{メッセージID}` でプロパティから参照する。
- 共通部品（ナビゲーション等）は `fragments/` に切り出し、`th:replace` で読み込む。

### プルダウン

- プルダウンは**共通フラグメント**（`fragments/pulldown.html`）で描画する。テンプレートに `<select>` / `<option>` のループを手書きしない。
- 選択肢は **DB（マスタテーブル）または Enum** から取得し、`CommandOutput` で `List<PulldownItem>` に変換して渡す（仕組みは architecture.md 参照）。
  - DB 由来: Command が Mapper（`DbCall` 経由）で取得 → CommandOutput で変換
  - Enum 由来: CommandOutput で `Enum.values()` から変換
- テンプレート内で Enum を直接参照（`T(...)` 構文）しない。選択肢の組み立てはテンプレートでやらず、必ず CommandOutput 側で行う。

## 10. 設定ファイル規約

- `application.properties` にパスワード等の機密情報を**直書きしない**。Key Vault のシークレット名参照（`${db-password}` 形式）で書く。
- ローカル専用の機密値ファイルを作らない（ローカルも Key Vault に接続する）。
