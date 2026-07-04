# spring-sample
springのsample実装

案件からインプットした要件をドキュメントとして残しながら、対応するサンプルコードを蓄積していくリポジトリ。

## ドキュメント構成

- [docs/requirements/overview.md](docs/requirements/overview.md) — 全体要件・技術方針・共通規約
- [docs/requirements/features/](docs/requirements/features/) — 機能別の要件（1機能1ファイル）
  - [project.md](docs/requirements/features/project.md) — 案件（プロジェクト）管理
  - [_template.md](docs/requirements/features/_template.md) — 新機能用テンプレート

新しい要件をインプットするときは、先に該当の要件ドキュメントへ反映してからコードを書く（詳細は [CLAUDE.md](CLAUDE.md)）。

## 実行

```bash
./gradlew bootRun   # http://localhost:8080/
```
