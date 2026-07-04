# spring-sample

Spring Boot 3.5 / Java 21 のサンプル実装。案件で必要になる実装パターンを、要件ドキュメントとセットで蓄積するリポジトリ。

## 最重要ルール: 要件ドキュメント駆動

会話でインプットされた要件は消えていく前提で運用する。

1. **コードを書く前に必ず `docs/requirements/` を読む。**
   - 全体要件・共通規約: `docs/requirements/overview.md`
   - 機能別要件: `docs/requirements/features/<機能名>.md`
2. **ユーザーから新しい要件・案件情報を受け取ったら、コードより先に要件ドキュメントへ反映する。**
   - 新機能は `docs/requirements/features/_template.md` をコピーして起こす。
   - 既存機能の変更は該当ドキュメントの仕様を更新してから実装する。
3. **未確定の要件を勝手に確定させない。** 不明点は各ドキュメントの「未実装 / 未確定」セクションと `overview.md` §6 に残す。
4. 実装とドキュメントが食い違ったままコミットしない。

## ビルド・実行

```bash
./gradlew build        # ビルド + テスト
./gradlew bootRun      # 起動（http://localhost:8080/ → 案件一覧へリダイレクト）
```

## コード規約（詳細は overview.md）

- パッケージ: `demo.config` / `demo.domain.<feature>` / `demo.presentation.<feature>`
- Controller は `@Controller`。REST API は作らない。
- Service の返り値 DTO（record）をそのまま Model に渡す。`XxxView` クラスは不要。
- Thymeleaf テンプレートから record にアクセスするときはメソッド呼び出し構文 `${dto.fieldName()}` を使う。
- テンプレートは `src/main/resources/templates/` 配下。共通ナビゲーションは `fragments/nav.html`。
- 日時は `OffsetDateTime`（JST）。テンプレートでは `#temporals.format()` でフォーマットする。
- フォームのバリデーションエラーは `BindingResult` で受け取り `th:errors` で表示する。
