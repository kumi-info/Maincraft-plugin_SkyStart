SkyStart v1.0.0
================

s2e-skyrunner のスタート地点（startLocation）へプレイヤーをテレポートする
コンパニオンプラグインです。

■ コマンド
  /skystart            自分をスタート地点へTP
  /skystart <player>   指定プレイヤーをスタート地点へTP
  /skystart all        オンライン全員をスタート地点へTP
  別名: /srtp, /srstart
  権限: skystart.use（既定: OP）

■ 仕組み
  実行のたびに plugins/s2e-skyrunner/config.yml の startLocation を読み取ります。
  そのため /skyrunner create でスタート位置を引き直しても常に最新に追従します。
  向き(yaw)は config.yml の directionX / directionZ から進行方向を向くよう自動計算します。

  現在の設定値（参考・config.yml より）:
    world=world  x=8941.88  y=300.0  z=-809.33  dir=(1,0)→+X方向

■ 導入
  SkyStart-1.0.0.jar を plugins/ に置いて /reload もしくはサーバー再起動。
  ※既に plugins/ へ配置済みです。

■ オプション設定（任意）
  plugins/SkyStart/config.yml に下記を書くとデバッグログを出せます。
    debug: true

■ ビルド（オフライン）
  JDK21 で javac → jar。Paper 1.21 の libraries/ をクラスパスに使用。
  ソース: 02_自作プラグイン/SkyStart/src/

■ 注意
  ・元プラグインの /skyrunner tp とは独立しています（こちらは状態に依存せず
    config の座標へ直接TPします）。
  ・startLocation が未作成（/skyrunner create 前）の場合はエラーメッセージを返します。
