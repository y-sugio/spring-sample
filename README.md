# spring-sample
springのsample実装

案件からインプットした情報をドキュメントとして残しながら、対応するサンプルコードを蓄積していくリポジトリ。

## ドキュメント構成

ドキュメントは「要件 / アーキテクチャ / コーディングルール」の3つに分割して管理する。

| ドキュメント | 内容 |
|---|---|
| [docs/requirements/overview.md](docs/requirements/overview.md) | **要件** — システムが何を満たすか（業務・画面・データ・運用） |
| [docs/requirements/features/](docs/requirements/features/) | 機能別の要件（1機能1ファイル。[_template.md](docs/requirements/features/_template.md) をコピーして作成） |
| [docs/architecture.md](docs/architecture.md) | **アーキテクチャ** — どういう構造・仕組みで実現するか（パッケージ構成・レイヤー・共通機構） |
| [docs/coding-rules.md](docs/coding-rules.md) | **コーディングルール** — コードをどう書くか（命名・作成基準・使い分け） |

新しいインプットを受けたときは、先に該当ドキュメントへ反映してからコードを書く（詳細は [CLAUDE.md](CLAUDE.md)）。

## 実行

```bash
./gradlew bootRun   # http://localhost:8080/
```
