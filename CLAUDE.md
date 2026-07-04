# spring-sample

Spring Boot 3.5 / Java 21 のサンプル実装。案件で必要になる実装パターンを、要件・アーキテクチャ・コーディングルールのドキュメントとセットで蓄積するリポジトリ。

## 最重要ルール: ドキュメント駆動

会話でインプットされた情報は消えていく前提で運用する。

1. **コードを書く前に必ず `docs/` を読む。** ドキュメントは3つに分かれている:
   - **要件**（何を満たすか）: `docs/requirements/overview.md`、機能別は `docs/requirements/features/<機能名>.md`
   - **アーキテクチャ**（どういう構造・仕組みか）: `docs/architecture.md`
   - **コーディングルール**（どう書くか）: `docs/coding-rules.md`
2. **ユーザーから新しいインプットを受け取ったら、コードより先に該当ドキュメントへ反映する。**
   - 業務・画面・データの要件 → `docs/requirements/`（新機能は `features/_template.md` をコピー）
   - パッケージ構成・共通機構・技術選定 → `docs/architecture.md`
   - 命名・書き方・使い分けのルール → `docs/coding-rules.md`
   - 1つのインプットが複数に関わる場合は分割してそれぞれに反映する（重複記載しない）。
3. **未確定の要件を勝手に確定させない。** 不明点は `docs/requirements/overview.md` §7（未確定リスト）に残す。
4. 実装とドキュメントが食い違ったままコミットしない。

## ビルド・実行

```bash
./gradlew build        # ビルド + テスト
./gradlew bootRun      # 起動（http://localhost:8080/ → 案件一覧へリダイレクト）
```

## 要点（詳細は docs/ 参照）

- パッケージは業務単位 × レイヤー（controller / command / task / mapper）。共通機構は `common`。
- Controller は `@Controller`。REST API は作らない。ビューは Thymeleaf。
- Mapper は必ず `DbCall` 経由。`@Transactional` は Command のみ。
- 例外: `SystemException` → GlobalExceptionHandler、`BusinessException` → Controller でキャッチ。
- メッセージは `messages.properties` で ID 管理（`ID=ID 本文` 形式）。
