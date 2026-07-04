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
| `common.exception` | `SystemException`、`BusinessException` |
| `common.db` | `DbCall`。Mapper 呼び出しの共通ラッパー、DB 例外を `SystemException` に変換 |
| `common.aspect` | `LoggingAspect`。各レイヤーの開始・終了ログを AOP で出力 |
| `common.filter` | `MdcFilter`（MDC セット）、`BusinessHoursFilter`（業務時間チェック） |
| `common.mapper` | 単一テーブルの Mapper・Entity |
| `common.mapper.typehandler` | MyBatis TypeHandler（`LocalDateTypeHandler` 等） |

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
| Mapper | 変換責務 | MyBatis の **TypeHandler** で `LocalDate ↔ String(yyyyMMdd)` を自動変換する |

**TypeHandler の実装・配置**:

- 配置: `common.mapper.typehandler.LocalDateTypeHandler`
- MyBatis の `BaseTypeHandler<LocalDate>` を継承して実装する。
- `setNonNullParameter`: `LocalDate → String(yyyyMMdd)` に変換して PreparedStatement にセット
- `getNullableResult`: `String(yyyyMMdd) → LocalDate` に変換して返す
- `application.properties`（または MyBatis 設定）でグローバル登録し、`LocalDate` 型のカラムに自動適用する。

```java
// common.mapper.typehandler.LocalDateTypeHandler
@MappedTypes(LocalDate.class)
public class LocalDateTypeHandler extends BaseTypeHandler<LocalDate> {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate date, JdbcType jdbcType) throws SQLException {
        ps.setString(i, date.format(FMT));
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String val = rs.getString(columnName);
        return val == null ? null : LocalDate.parse(val, FMT);
    }
    // getNullableResult(ResultSet, int) / getNullableResult(CallableStatement, int) も同様
}
```

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

## 6. 例外ハンドリング

### 例外クラスの種類と配置

| クラス | パッケージ | 用途 |
|---|---|---|
| `SystemException` | `common.exception` | システム例外。DB 障害・予期せぬエラー等 |
| `BusinessException` | `common.exception` | 業務例外。業務ルール違反で return ルートがない場合に throw |
| `DbCall` | `common.db` | DB 呼び出しの共通ラッパー。Mapper 呼び出しと メッセージ ID を受け取り、DB 例外を `SystemException` に変換する |

### ハンドリングの方針

#### システム例外

- `SystemException` は `GlobalExceptionHandler`（`@ControllerAdvice`）で一括キャッチする。
- 共通エラー画面（`error/system.html`）へ遷移し、`SYS` プレフィックスのメッセージを表示する。
- 配置: `demo.config.GlobalExceptionHandler`

#### DB 呼び出し（DbCall）

- Mapper の呼び出しは**直接呼ばず、必ず `DbCall` 経由**で行う。
- `DbCall` はメッセージ ID と Mapper 呼び出し（ラムダ）を受け取り、DB 例外（`DataAccessException` 等）を `SystemException` に変換して throw する。
- これにより、各 Task・Command の Mapper 呼び出し箇所で個別に try-catch を書かなくてよい。

#### 業務例外

- `BusinessException` は **Controller でキャッチ**する（`GlobalExceptionHandler` では処理しない）。
- Controller がキャッチ後、Model にメッセージを設定して元の画面を再描画する。
- throw するのは「CommandOutput でデータを返す処理においてエラーが発生し、通常の return ルートを使えないケース」。

### 例外の流れ

```
【システム例外】
任意のレイヤー
  └─ throw SystemException("SYS001")
        ↓
    GlobalExceptionHandler（@ControllerAdvice）
        ↓
    error/system.html（SYS メッセージを表示）

【業務例外】
Command / Task
  └─ throw BusinessException("BIZ001")
        ↓
    Controller（try-catch）
        ↓
    model.addAttribute("errorMessage", ...)
        ↓
    元の画面を return（re-render）
```

### 実装イメージ

```java
// common.exception.SystemException
public class SystemException extends RuntimeException {
    private final String messageId;
}

// common.exception.BusinessException
public class BusinessException extends RuntimeException {
    private final String messageId;
}

// common.db.DbCall
@Component
public class DbCall {
    // 戻り値ありの Mapper 呼び出し
    public <T> T execute(String messageId, Supplier<T> mapperCall) {
        try {
            return mapperCall.get();
        } catch (DataAccessException e) {
            throw new SystemException(messageId, e);
        }
    }
    // 戻り値なしの Mapper 呼び出し（INSERT / UPDATE / DELETE）
    public void execute(String messageId, Runnable mapperCall) {
        try {
            mapperCall.run();
        } catch (DataAccessException e) {
            throw new SystemException(messageId, e);
        }
    }
}

// Task / Command での使用イメージ
ProjectEntity entity = dbCall.execute("SYS001", () -> projectMapper.findById(id));
dbCall.execute("SYS001", () -> projectMapper.insert(entity));

// config.GlobalExceptionHandler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SystemException.class)
    public String handleSystem(SystemException ex, Model model) {
        model.addAttribute("errorMessage", /* messageSource.getMessage(ex.getMessageId()) */);
        return "error/system";
    }
}

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

## 7. ログ・トラッキング

### 方針

- 各レイヤーの開始・終了を AOP（Aspect）でログ出力する。
- リクエストごとにトラッキング ID を発行し、MDC に載せて全ログで串刺しできるようにする。
- MDC にはトラッキング ID・ユーザー ID・クライアント IP を設定する。

### クラス構成

| クラス | パッケージ | 役割 |
|---|---|---|
| `LoggingAspect` | `common.aspect` | 各レイヤーの開始・終了ログを出力する Aspect |
| `MdcFilter` | `common.filter` | リクエスト開始時に MDC をセット、終了時にクリアする Servlet Filter |

### MDC に設定する項目

| MDC キー | 内容 | 取得元 |
|---|---|---|
| `trackingId` | リクエストごとに発行する UUID | `MdcFilter` で `UUID.randomUUID()` |
| `userId` | ログイン中のユーザー ID | セッション（認証未導入のうちは固定値または空） |
| `clientIp` | クライアントの IP アドレス | `HttpServletRequest.getRemoteAddr()` |

### LoggingAspect の対象レイヤーと出力内容

| ポイントカット対象 | 出力タイミング | 出力内容 |
|---|---|---|
| `*.controller.*Controller` | 開始・終了 | クラス名、メソッド名、引数 |
| `*.command.*Command` | 開始・終了 | クラス名、メソッド名、引数 |
| `*.task.*Task` | 開始・終了 | クラス名、メソッド名、引数 |
| `*.mapper.*Mapper` | 開始・終了 | クラス名、メソッド名、引数 |

- 例外発生時は終了ログに例外情報を付与する。
- ログレベルは DEBUG（本番では INFO 以上に絞ることを想定）。

### 実装イメージ

```java
// common.filter.MdcFilter
@Component
public class MdcFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        MDC.put("trackingId", UUID.randomUUID().toString());
        MDC.put("clientIp", req.getRemoteAddr());
        MDC.put("userId", /* セッションからユーザーID取得。未認証時は "-" */);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

// common.aspect.LoggingAspect
@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* demo..controller.*Controller.*(..))" +
              " || execution(* demo..command.*Command.*(..))" +
              " || execution(* demo..task.*Task.*(..))" +
              " || execution(* demo..mapper.*Mapper.*(..))")
    public void layerMethods() {}

    @Around("layerMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String cls = pjp.getSignature().getDeclaringType().getSimpleName();
        String method = pjp.getSignature().getName();
        log.debug("[START] {}.{} args={}", cls, method, pjp.getArgs());
        try {
            Object result = pjp.proceed();
            log.debug("[END]   {}.{}", cls, method);
            return result;
        } catch (Throwable t) {
            log.debug("[END]   {}.{} exception={}", cls, method, t.getMessage());
            throw t;
        }
    }
}
```

### ログ出力フォーマット（application.properties / logback）

MDC の値をパターンに含めることで、トラッキング ID・ユーザー ID・IP を全ログ行に出力する。

```
[%X{trackingId}] [%X{userId}] [%X{clientIp}] %-5level %logger{36} - %msg%n
```

## 8. 画面共通要件

- ベースパス: `/page`
- ルート `/` は案件一覧（`/page/projects`）にリダイレクトする。
- 登録・更新完了時はフラッシュメッセージ（`RedirectAttributes`）で結果を通知し、詳細画面へリダイレクトする（PRG パターン）。
- バリデーションエラー時はフォーム画面を再描画し、Thymeleaf の `th:errors` でエラーメッセージを表示する。

## 9. セッション管理

### セッションタイムアウト設定

- タイムアウト時間は `application.properties` で設定する。
  ```properties
  server.servlet.session.timeout=30m  # 時間は未確定（→ §12 参照）
  ```
- タイムアウトが発生したリクエスト（無効なセッション）はセッションタイムアウト画面へ遷移する。

### タイムアウト検知と遷移

Spring Security の `invalidSessionUrl` を使い、無効なセッション ID を持つリクエストを一括でタイムアウト画面へリダイレクトする。

```java
// config.SecurityConfig（抜粋）
http.sessionManagement(session -> session
    .invalidSessionUrl("/error/session-timeout")
);
```

- タイムアウト画面: `error/session-timeout.html`
- タイムアウト画面自体は認証不要（`permitAll`）とする。

---

## 10. 業務時間チェック

### 概要

リクエストのたびに **業務時間チェックフィルタ** を実行し、業務サービス状況テーブルを参照して業務時間外であれば業務時間外エラー画面へ遷移する。

### クラス構成

| クラス | パッケージ | 役割 |
|---|---|---|
| `BusinessHoursFilter` | `common.filter` | リクエストごとに業務時間チェックを行う Servlet Filter |
| `BusinessHoursMapper` | `common.mapper` | 業務サービス状況テーブルを参照（単一テーブルのため common）|

### フィルタの動作

1. リクエストを受け取る
2. `BusinessHoursMapper` で業務サービス状況テーブルを参照する（`DbCall` 経由）
3. 業務時間内 → そのまま次のフィルタ・処理へ
4. 業務時間外 → 業務時間外エラー画面（`error/business-hours.html`）へ遷移

### フィルタの適用除外

SAML 認証エンドポイント（`/login/saml2/**`）等、認証フローに関わるパスはチェック対象外とする（未確定 → §10 参照）。

### 業務サービス状況テーブル

テーブル名・カラム定義は未確定（→ §10 参照）。

### 実装イメージ

```java
// common.filter.BusinessHoursFilter
@Component
public class BusinessHoursFilter implements Filter {

    private final BusinessHoursMapper businessHoursMapper;
    private final DbCall dbCall;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean isOpen = dbCall.execute("SYS001",
                () -> businessHoursMapper.isBusinessHours());
        if (!isOpen) {
            HttpServletRequest req = (HttpServletRequest) request;
            req.getRequestDispatcher("/error/business-hours").forward(request, response);
            return;
        }
        chain.doFilter(request, response);
    }
}
```

## 11. セキュリティ・認証

### 認証方式

| 項目 | 内容 |
|---|---|
| IdP | Azure Entra ID |
| プロトコル | SAML 2.0 |
| SP（このアプリ） | Spring Security SAML SP（`spring-security-saml2-service-provider`） |
| リクエスト元 | 別システムの URL からこのアプリサーバへリクエストが届く |

### SAML アサーションで受け取る属性

| 属性 | 内容 | セッション格納キー（仮） |
|---|---|---|
| ログイン ID | 別システムのユーザー識別子 | `session.userId` |
| ユーザー種別 | 別システムで管理するユーザーの種別 | `session.userType` |

- SAML 認証成功後、アサーションからこれらの属性を取り出しセッションに格納する。
- `MdcFilter` はセッションの `userId` を MDC にセットする（§7 参照）。

### CSRF / CORS

- CSRF: SAML の POST Binding でレスポンスが飛んでくるため、SAML エンドポイント（`/login/saml2/sso/**`）は CSRF 除外が必要。その他の画面は要検討。
- CORS: 画面のみのため現時点では不要。

### 未確定事項（→ §10）

- SAML アサーションの属性名（Azure Entra ID 側のクレーム設定に依存）
- ユーザー種別の値体系（Enum 定義に必要）
- 認可ルール（ロールによる画面・操作の制限）

## 12. 未確定・今後インプット待ちの要件

案件から情報が入り次第、ここから各項目を確定させて該当セクション・機能ドキュメントに反映する。

- [x] 認証方式 → Azure Entra ID + SAML 2.0。別システム経由でログイン ID・ユーザー種別を受け取る
- [ ] SAML アサーションの属性名（Azure Entra ID 側のクレーム設定に依存）
- [ ] ユーザー種別の値体系（Enum 定義に必要）
- [ ] 認可ルール（ロールによる画面・操作の制限）
- [ ] 永続化先（RDBMS の種類、スキーマ設計、マイグレーションツール）
- [ ] 案件（プロジェクト）エンティティの正式な項目定義（現状は name のみ）
- [ ] 更新・削除系のユースケースと楽観ロック方式（`version` カラムは既に用意済み）
- [ ] ページング: 総件数・ページナビゲーションの要否
- [ ] セッションタイムアウト時間（`server.servlet.session.timeout` の値）
- [ ] 業務サービス状況テーブルのテーブル名・カラム定義・業務時間の判定ロジック
- [ ] 業務時間チェックフィルタの除外パス（SAML 認証エンドポイント等）
- [ ] メッセージ ID のプレフィックス文字列（MSG / VAL / BIZ / SYS は仮）
- [x] 日付の DB 格納フォーマット → ユーザー入力日付は `yyyyMMdd`（String）、登録・更新日時は `sysdate`
