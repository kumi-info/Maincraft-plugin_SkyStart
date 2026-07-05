# Maincraft-plugin_SkyStart

`s2e-skyrunner` のスタート地点（`startLocation`）へプレイヤーをテレポートする Paper 用コンパニオンプラグインです。

## コマンド

| コマンド | 動作 |
|---|---|
| `/skystart` | 自分をスタート地点へTP |
| `/skystart <player>` | 指定プレイヤーをTP |
| `/skystart all` | オンライン全員をTP |

- 別名: `/srtp`, `/srstart`
- 権限: `skystart.use`（既定: OP）

## 仕組み

実行のたびに `plugins/s2e-skyrunner/config.yml` の `startLocation` を読み取ります。
そのため `/skyrunner create` でスタート位置を引き直しても常に最新に追従します。
向き(yaw)は `directionX` / `directionZ` から進行方向を向くよう自動計算します。

## 導入

`SkyStart-x.y.z.jar` を `plugins/` に置いて `/reload` もしくはサーバー再起動。

## オプション設定（任意）

`plugins/SkyStart/config.yml`:

```yaml
debug: true   # startLocation 読込ログを出力
```

## ビルド（オフライン）

JDK21 で `javac` → `jar`。Paper 1.21 の `libraries/` をクラスパスに使用。
ソース: [`src/`](src/)

## ブランチ運用

- `main` … 最新版
- `vX.Y.Z` … 各リリースのスナップショット（例: `v1.0.0`）

## バージョン履歴

- **v1.0.0** — 初版。config の `startLocation` を直接読んでTP、`all`/対象指定、進行方向へ向き調整。
