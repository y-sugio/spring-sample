# アーキテクチャ

このドキュメントは、[要件](./requirements/overview.md)を**どういう構造・仕組みで実現するか**をまとめたもの。
コードの書き方の規約は [coding-rules.md](./coding-rules.md) を参照。

## 1. 技術スタック

| 項目 | 採用技術 | 備考 |
|---|---|---|
| 言語 | Java 21 | Gradle toolchain で固定 |
| フレームワーク | Spring Boot 3.5.x | spring-boot-starter-web / validation / security |
| ビュー | Thymeleaf | `spring-boot-starter-thymeleaf`。テンプレートは `src/main/resources/templates/` |
| CSS | Bootstrap 5 (WebJars) | |
| DB | Oracle | JDBC + TCPS。Oracle Wallet で SSL/TLS 認証 |
| O/R マッパー | MyBatis（予定） | 現状は Mapper インターフェースの裏でインメモリ仮実装（`InMemoryProjectMapper`） |
| 認証 | Spring Security SAML SP | `spring-security-saml2-service-provider`。IdP は Azure Entra ID |
| シークレット管理 | Azure Key Vault | `spring-cloud-azure-starter-keyvault-secrets` + `DefaultAzureCredential` |
| ビルド | Gradle | `./gradlew build` |

## 2. パッケージ構成

**第1階層: 業務単位**でパッケージを切り、その中を**レイヤードアーキテクチャ**で構成する。

> **業務の定義**: ユーザーが一連として行う作業のまとまり。  
> 例）案件に対して「新規作成・一覧確認・詳細確認・更新」を行うなら、それらを一まとめにして一つの業務とする。  
> 複数のドメインをまたぐケースでも、ユーザーの業務としての結びつきが強ければ同一パッケージに収める。

```
demo/
├── config/                     … Security、GlobalExceptionHandler など横断設定
├── common/
│   ├── enums/                  … 全 Enum（業務・レイヤーを問わず全てここに集約）
│   ├── util/                   … 共通フォーマッター（Formatters）等のユーティリティ
│   ├── exception/              … SystemException、BusinessException
│   ├── db/                     … DbCall（Mapper 呼び出しの共通ラッパー）
│   ├── aspect/                 … LoggingAspect
│   ├── filter/                 … MdcFilter、BusinessHoursFilter
│   └── mapper/                 … 単一テーブルを扱う Mapper・Entity
│       └── typehandler/        … MyBatis TypeHandler
└── <業務名>/                   … 業務パッケージ（例: project）
    ├── controller/             … Controller レイヤ
    ├── command/                … Command レイヤ
    ├── task/                   … Task レイヤ
    └── mapper/                 … 複数テーブルを扱い業務との結びつきが強い Mapper・Entity
```

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
| `XxxDto` | `CommandOutput` の子データ用クラス（ネスト構造の場合） |

#### Task レイヤ (`<業務>.task`)

| クラス種別 | 役割 |
|---|---|
| `XxxTask` | **業務ロジックの実装**。Mapper を呼び出す。原則 1 項目を返す |
| `XxxTaskInput` | Task への入力値（作成基準は coding-rules.md 参照） |
| `XxxTaskOutput` | Task がオブジェクトを返す場合の戻り値 |

#### Mapper レイヤ

| 配置 | 条件 |
|---|---|
| `common.mapper` | **単一テーブル**を扱う Mapper・Entity |
| `<業務>.mapper` | **複数テーブル**をまたぎ、業務との結びつきが強い Mapper・Entity |

### データの流れ

```
[画面] → Form → Controller → CommandInput
                    ↓
                Command（@Transactional）
                  ├── TaskInput → Task → TaskOutput
                  └── Mapper（DbCall 経由）
                    ↓
                CommandOutput（プレゼンテーションロジック）
                    ↓
                Model → [Thymeleaf テンプレート]
```

## 3. テンプレート構成

Java パッケージと同じく**業務単位**でディレクトリを切る。1業務に複数の HTML を置く。

```
src/main/resources/templates/
├── fragments/          … 共通部品（ナビゲーション、プルダウン等）
├── error/              … エラー画面（system / business-hours / session-timeout / 404）
└── <業務名>/           … 業務ごとのテンプレート
    ├── list.html
    ├── new.html
    └── detail.html
```

### プルダウン部品（共通フラグメント）

プルダウン（`<select>`）は共通フラグメントで描画する。各テンプレートで `<option>` のループを手書きしない。

| 構成要素 | 配置 | 役割 |
|---|---|---|
| `fragments/pulldown.html` | `templates/fragments/` | 選択肢リストと選択中値を受け取り `<select>` を描画するフラグメント |
| `PulldownItem`（名称は仮） | `common.util` | 選択肢 1 件を表す record（`value` / `label`） |

**選択肢の取得元は 2 系統**。いずれも `CommandOutput` が `List<PulldownItem>` に変換して Model 経由でテンプレートに渡す。

| 取得元 | 取得方法 |
|---|---|
| DB（マスタテーブル） | Command が Mapper（`DbCall` 経由）で取得し、CommandOutput で `PulldownItem` に変換 |
| Enum | CommandOutput で `Enum.values()` から `PulldownItem` に変換 |

```
【DB 由来】  Command ─ DbCall ─ Mapper → List<Entity> ─ CommandOutput → List<PulldownItem> ─┐
【Enum 由来】                     Enum.values() ─ CommandOutput → List<PulldownItem> ─────────┤
                                                                                              ↓
                                                          fragments/pulldown.html（<select> 描画）
```

## 4. 例外ハンドリング機構

### クラス構成

| クラス | パッケージ | 役割 |
|---|---|---|
| `SystemException` | `common.exception` | システム例外。メッセージ ID を保持 |
| `BusinessException` | `common.exception` | 業務例外。メッセージ ID を保持 |
| `DbCall` | `common.db` | Mapper 呼び出しの共通ラッパー。DB 例外（`DataAccessException`）を `SystemException` に変換 |
| `GlobalExceptionHandler` | `config` | `@ControllerAdvice`。`SystemException` を一括キャッチして共通エラー画面へ |

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

// config.GlobalExceptionHandler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SystemException.class)
    public String handleSystem(SystemException ex, Model model) {
        model.addAttribute("errorMessage", /* messageSource.getMessage(ex.getMessageId()) */);
        return "error/system";
    }
}
```

## 5. メッセージ管理機構

- メッセージは `src/main/resources/messages.properties` で一元管理する。
- Java からは Spring の `MessageSource` を DI して `getMessage(messageId, args, locale)` で取得する。
- Thymeleaf テンプレートからは `#{メッセージID}` 構文で直接参照できる。
- Bean Validation のエラーメッセージも同プロパティに寄せる（アノテーションの `message` 属性にメッセージ ID を指定）。

メッセージ ID の体系・記載フォーマットは [coding-rules.md](./coding-rules.md) を参照。

## 6. ログ・トラッキング機構

### クラス構成

| クラス | パッケージ | 役割 |
|---|---|---|
| `MdcFilter` | `common.filter` | リクエスト開始時に MDC をセット、終了時にクリアする Servlet Filter |
| `LoggingAspect` | `common.aspect` | 各レイヤーの開始・終了ログを出力する Aspect |

### MDC に設定する項目

| MDC キー | 内容 | 取得元 |
|---|---|---|
| `trackingId` | リクエストごとに発行する UUID | `MdcFilter` で `UUID.randomUUID()` |
| `userId` | ログイン中のユーザー ID | セッション（SAML 認証で格納したログイン ID） |
| `clientIp` | クライアントの IP アドレス | `HttpServletRequest.getRemoteAddr()` |

### LoggingAspect の対象と出力

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

### ログ出力フォーマット

MDC の値をパターンに含めることで、トラッキング ID・ユーザー ID・IP を全ログ行に出力する。

```
[%X{trackingId}] [%X{userId}] [%X{clientIp}] %-5level %logger{36} - %msg%n
```

## 7. リクエスト処理の共通機構（フィルタ・セッション）

### 業務時間チェックフィルタ

| クラス | パッケージ | 役割 |
|---|---|---|
| `BusinessHoursFilter` | `common.filter` | リクエストごとに業務時間チェックを行う Servlet Filter |
| `BusinessHoursMapper` | `common.mapper` | 業務サービス状況テーブルを参照（単一テーブルのため common） |

動作:
1. リクエストを受け取る
2. `BusinessHoursMapper` で業務サービス状況テーブルを参照する（`DbCall` 経由）
3. 業務時間内 → そのまま次のフィルタ・処理へ
4. 業務時間外 → 業務時間外エラー画面（`error/business-hours.html`）へ遷移

SAML 認証エンドポイント（`/login/saml2/**`）等、認証フローに関わるパスはチェック対象外とする（除外パスは未確定）。

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

### セッションタイムアウト

- タイムアウト時間は `application.properties` の `server.servlet.session.timeout` で設定する（値は未確定）。
- Spring Security の `invalidSessionUrl` で、無効なセッション ID を持つリクエストを一括でタイムアウト画面へリダイレクトする。

```java
// config.SecurityConfig（抜粋）
http.sessionManagement(session -> session
    .invalidSessionUrl("/error/session-timeout")
);
```

- タイムアウト画面: `error/session-timeout.html`（認証不要 `permitAll`）

## 8. 認証アーキテクチャ（SAML）

| 項目 | 内容 |
|---|---|
| IdP | Azure Entra ID |
| プロトコル | SAML 2.0 |
| SP（このアプリ） | Spring Security SAML SP（`spring-security-saml2-service-provider`） |

- SAML 認証成功後、アサーションから**ログイン ID** と**ユーザー種別**を取り出しセッションに格納する。
- `MdcFilter` はセッションのログイン ID を MDC の `userId` にセットする（§6 参照）。
- CSRF: SAML の POST Binding でレスポンスが飛んでくるため、SAML エンドポイント（`/login/saml2/sso/**`）は CSRF 除外が必要。その他の画面は要検討。
- CORS: 画面のみのため現時点では不要。

## 9. インフラ接続（Key Vault / Oracle Wallet）

### Azure Key Vault

- `spring-cloud-azure-starter-keyvault-secrets` により、起動時に Key Vault のシークレットを Spring の `Environment` に注入する。
- `application.properties` にはシークレット名のみを書く:
  ```properties
  spring.datasource.password=${db-password}       # Key Vault のシークレット名
  spring.datasource.url=${db-url}                 # 接続 URL も Key Vault で管理（未確定）
  ```
- 認証は `DefaultAzureCredential` で環境ごとに自動切り替え:

| 環境 | 認証方式 | 必要な事前作業 |
|---|---|---|
| Azure（本番・ステージング等） | マネージド ID | アプリのマネージド ID に Key Vault アクセスポリシーを付与 |
| ローカル開発 | Azure CLI（`az login`） | 開発者アカウントに Key Vault アクセスポリシーを付与 |

### Oracle Wallet

- Wallet ファイル（`cwallet.sso` / `ewallet.p12`）を Key Vault で管理する。
- アプリ起動時に Key Vault から Wallet を取得し、一時ディレクトリに展開する（取得・展開方法は未確定）。
- JDBC 接続時に `oracle.net.wallet_location` で Wallet のパスを指定する。
- 接続 URL は TCPS プロトコルを使用する（例: `jdbc:oracle:thin:@tcps://...`）。

## 10. 日付変換機構（MyBatis TypeHandler）

DB のユーザー入力日付は `String(yyyyMMdd)`、Java 側は `LocalDate` で扱う（[要件 §5](./requirements/overview.md)）。
変換は MyBatis の **TypeHandler** に集約し、各 Mapper で個別の変換処理を書かない。

- 配置: `common.mapper.typehandler.LocalDateTypeHandler`
- `BaseTypeHandler<LocalDate>` を継承。グローバル登録して `LocalDate` 型カラムに自動適用する。

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
