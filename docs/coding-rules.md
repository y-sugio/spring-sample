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

## 3. バリデーション規約

バリデーションは対象の項目数で実装方法を使い分ける。

| 対象 | 実装方法 | 例 |
|---|---|---|
| **単項目**（1 フィールドで完結） | Form のフィールドに **Bean Validation アノテーション**を付与 | `@NotBlank`、`@Size(max=100)`、`@Pattern` 等。汎用チェックはカスタムアノテーション（`XxxConstraint`）化してよい |
| **複数項目**（フィールド間の相関） | **カスタムバリデーション**（`XxxValidator`、Spring の `Validator` 実装） | 開始日 ≦ 終了日、いずれか一方は必須、等 |

- エラーメッセージはどちらも `messages.properties` のメッセージ ID で指定する（§6 参照）。
- どちらのエラーも `BindingResult` に集約し、テンプレートの `th:errors` で表示する（表示方法は §10 参照）。
- 単項目チェックを `XxxValidator` に書かない。逆に、相関チェックをアノテーションで無理に表現しない。
- DB 参照が必要なチェック（重複チェック等）はバリデーションではなく **Command 側の業務チェック**（`BusinessException`）で行う（§5 参照）。

### 検索 / 登録を兼ねる画面のバリデーション

1 画面（1 Controller）が検索・登録の 2 アクションを扱う場合（要件の画面遷移パターン B・C）、**検索用と登録用で Form・Validator を分ける**。1 つの Form・Validator を条件分岐で使い回さない。

| アクション | クラス例 |
|---|---|
| 検索 | `XxxSearchForm` / （相関チェックがあれば）`XxxSearchValidator` |
| 登録 | `XxxForm` / `XxxValidator` |

- Controller のハンドラメソッドはアクションごとに分け、それぞれの `@Valid` 対象を専用 Form にする。
- 同じ画面項目名でも、検索は必須なし・登録は必須あり、のように制約が異なるのが前提のため、アノテーションを条件付きで無効化するような実装（バリデーショングループの使い回し等）はしない。

```java
// 単項目: Form のアノテーション
public record ProjectForm(
        @NotBlank(message = "VAL001")
        @Size(max = 100, message = "VAL002")
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}

// 複数項目: カスタムバリデーション
@Component
public class ProjectValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ProjectForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProjectForm form = (ProjectForm) target;
        if (form.startDate() != null && form.endDate() != null
                && form.startDate().isAfter(form.endDate())) {
            errors.rejectValue("endDate", "VAL004");  // 開始日は終了日以前にしてください 等
        }
    }
}
```

## 4. レイヤー間の呼び出しルール

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

## 5. 例外の使い分けルール

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

## 6. メッセージ ID 規約

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

## 7. 日付の扱い

| 層 | 型 |
|---|---|
| Form / Controller / Command / Task | `LocalDate` |
| Entity（ユーザー入力日付） | `LocalDate`（TypeHandler が DB の `String(yyyyMMdd)` と自動変換） |

- **Mapper（TypeHandler）以外の層で日付の文字列変換をしない。**
- 登録日時・更新日時は Java でセットせず、**SQL 内で `sysdate`** を指定する。

## 8. 表示フォーマット規約

画面表示用の整形は **CommandOutput（および Dto）のプレゼンテーションロジック内**で行い、`common.util` の共通フォーマッターを呼び出す。

| データ種別 | 整形内容 | 整形箇所 |
|---|---|---|
| 日付 | `yyyy/MM/dd` | `CommandOutput` / `Dto` |
| 数値 | 3 桁カンマ区切り | `CommandOutput` / `Dto` |
| 区分値 | コード → Enum ラベル | `CommandOutput` / `Dto` |

- Controller・Command（計算ロジック）・Task で表示整形をしない。
- 独自の `SimpleDateFormat` / `DateTimeFormatter` / `String.format` を各所に書かず、共通フォーマッター **`demo.common.util.Formatters`** に寄せる。

## 9. Enum 規約

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

## 10. Thymeleaf テンプレート規約

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

## 11. 設定ファイル規約

- `application.properties` にパスワード等の機密情報を**直書きしない**。Key Vault のシークレット名参照（`${db-password}` 形式）で書く。
- ローカル専用の機密値ファイルを作らない（ローカルも Key Vault に接続する）。

## 12. レイヤー別実装サンプル

案件管理（`demo.project`）の実コードを規範とする。新しい業務を実装するときはこの形に揃える。

### Controller レイヤ

**Form（登録用）** — `src/main/java/demo/project/controller/ProjectForm.java`

単項目チェックはアノテーション。`message` にはメッセージ ID を `{ }` で指定する。

```java
public record ProjectForm(
        @NotBlank(message = "{VAL001}")
        @Size(max = 100, message = "{VAL002}")
        String name
) {
}
```

**Form（検索用）** — `src/main/java/demo/project/controller/ProjectSearchForm.java`

検索と登録でバリデーションが異なるため、Form は別クラスにする。

```java
public record ProjectSearchForm(
        String q      // 検索は必須項目なし
) {
}
```

**Controller** — `src/main/java/demo/project/controller/ProjectController.java`

CommandInput の生成・Command 呼び出し・Model 設定・業務例外のキャッチを行う。

```java
@Controller
@RequestMapping("/page/projects")
public class ProjectController {

    /** 一覧（検索） */
    @GetMapping
    public String list(@ModelAttribute("searchForm") ProjectSearchForm searchForm,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        ProjectListCommandInput input = new ProjectListCommandInput(searchForm.q(), page, size);
        model.addAttribute("output", listCommand.execute(input));
        return "projects/list";
    }

    /** 登録実行 */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "projects/new";                  // バリデーションエラー: フォーム再描画
        }
        ProjectCreateCommandOutput output;
        try {
            output = createCommand.execute(new ProjectCreateCommandInput(form.name()));
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", resolve(e.getMessageId()));
            return "projects/new";                  // 業務例外: 元の画面を再描画
        }
        ra.addFlashAttribute("message", resolve("MSG001"));
        return "redirect:/page/projects/" + output.projectId();   // PRG
    }
}
```

### Command レイヤ

**CommandInput** — `src/main/java/demo/project/command/ProjectCreateCommandInput.java`

```java
public record ProjectCreateCommandInput(
        String name
) {
}
```

**Command** — `src/main/java/demo/project/command/ProjectCreateCommand.java`

ユースケースと 1:1。トランザクション境界。Task・Mapper（DbCall 経由）を呼ぶ。

```java
@Component
public class ProjectCreateCommand {

    @Transactional
    public ProjectCreateCommandOutput execute(ProjectCreateCommandInput input) {
        if (duplicateCheckTask.execute(input.name())) {
            throw new BusinessException("BIZ002");   // 通常ルートで返せない → 業務例外
        }
        ProjectEntity entity = new ProjectEntity();
        entity.setName(input.name());
        dbCall.execute("SYS001", () -> projectMapper.insert(entity));
        return new ProjectCreateCommandOutput(entity.getProjectId());
    }
}
```

**CommandOutput** — `src/main/java/demo/project/command/ProjectDetailCommandOutput.java`

プレゼンテーションロジック（表示整形）はここ。`Formatters` を呼ぶ。

```java
public record ProjectDetailCommandOutput(
        Long projectId,
        String name,
        Integer version,
        String createDateDisplay,
        String updateDateDisplay
) {
    public static ProjectDetailCommandOutput from(ProjectEntity entity) {
        return new ProjectDetailCommandOutput(
                entity.getProjectId(),
                entity.getName(),
                entity.getVersion(),
                Formatters.dateTime(entity.getCreateDate()),
                Formatters.dateTime(entity.getUpdateDate())
        );
    }
}
```

**Dto（子データ）** — `src/main/java/demo/project/command/ProjectDto.java`

CommandOutput の子データ（一覧の 1 行）にプレゼンテーションロジックが必要なため Dto を実装。
不要なら Entity をそのまま使う。

```java
public record ProjectDto(
        Long projectId, String name, Integer version,
        String createDateDisplay, String updateDateDisplay
) {
    public static ProjectDto from(ProjectEntity entity) { /* Formatters で整形 */ }
}
```

### Task レイヤ

**Task** — `src/main/java/demo/project/task/ProjectDuplicateCheckTask.java`

業務ロジック。原則 1 項目を返す。入力 6 つ未満なので TaskInput は作らない。

```java
@Component
public class ProjectDuplicateCheckTask {

    /** @return 同名の案件が既に存在する場合 true */
    public boolean execute(String name) {
        return dbCall.execute("SYS001", () -> projectMapper.existsByName(name));
    }
}
```

### Mapper レイヤ

**Mapper** — `src/main/java/demo/common/mapper/ProjectMapper.java`（単一テーブルなので common）

```java
public interface ProjectMapper {
    List<ProjectEntity> search(String q, int offset, int limit);
    ProjectEntity findById(Long projectId);
    boolean existsByName(String name);
    void insert(ProjectEntity entity);   // 登録・更新日時は SQL 側で sysdate
}
```

※ MyBatis 導入までは `InMemoryProjectMapper` が仮実装（`@Repository`）。導入後は `@Mapper` + SQL に置き換える。

**Entity** — `src/main/java/demo/common/mapper/ProjectEntity.java`

区分値はコード値（`String`）のまま保持する getter / setter を持つクラス。

### 共通機構の使い方（実装済みクラス）

| クラス | 場所 | 使い方 |
|---|---|---|
| `DbCall` | `common.db` | `dbCall.execute("SYS001", () -> mapper.xxx())` |
| `BusinessException` / `SystemException` | `common.exception` | メッセージ ID を渡して throw |
| `Formatters` | `common.util` | `Formatters.date(...)` / `dateTime(...)` / `number(...)` |
| `LoggingAspect` | `common.aspect` | 自動適用（`*Controller` / `*Command` / `*Task` / `*Mapper` 命名が条件） |
| `MdcFilter` | `common.filter` | 自動適用。ログイン ID はセッションキー `userId` から取得 |
| `GlobalExceptionHandler` | `config` | `SystemException` を自動キャッチ → `error/system.html` |
| `ValidationConfig` | `config` | `{VAL001}` 形式のメッセージ ID 解決を有効化 |

> **命名が規約通りでないと LoggingAspect のポイントカットに乗らない**。クラス名のサフィックス（Controller / Command / Task / Mapper）は必ず守ること。
