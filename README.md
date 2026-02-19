# Galaxy BBS

Java Servlets・JSP・Oracle Database で構築した **Reddit 風掲示板**です。投稿、投票、コメント、アンケート、プロフィール管理ができ、ライト/ダークテーマ対応のウェブインターフェースで利用できます。

---

## 主な機能

- **ユーザーアカウント** — 登録、ログイン、パスワード忘れ・リセット（メール認証）
- **投稿** — テキスト投稿、画像・動画の添付、アンケートの作成
- **投票** — 投稿のアップ/ダウンボート、アンケートの選択肢への投票
- **コメント** — 投稿へのネストされた返信
- **通知** — コメントやメンションの通知
- **プロフィール・設定** — プロフィール写真、表示名、ライト/ダークテーマ

---

## 技術スタック

| 層         | 技術                         |
|------------|------------------------------|
| バックエンド | Java 11+, Jakarta Servlets   |
| ビュー     | JSP, JSTL                    |
| データベース | Oracle Database             |
| サーバー   | Apache Tomcat 10.x           |
| セキュリティ | jBCrypt（パスワード）        |
| メール     | Jakarta Mail（ウェルカム・リセット用） |
| フロントエンド | HTML, CSS, JavaScript    |

---

## はじめに

### 必要環境

- **JDK 11 以上**
- **Apache Tomcat 10.x**
- **Oracle Database**（`SQL/schema.sql` でスキーマ作成）
- **Oracle JDBC ドライバ**（`ojdbc8.jar` または `ojdbc11.jar` を `WEB-INF/lib` に配置）

### クイックセットアップ

1. **クローン** — リポジトリをクローンし、プロジェクトフォルダを開く。
2. **依存関係** — `download_deps.bat` を実行し、jBCrypt と Jakarta Mail を `WEB-INF/lib` に取得。
3. **Oracle ドライバ** — `ojdbc8.jar` または `ojdbc11.jar` を `WEB-INF/lib` に追加（リポジトリには含まれません）。
4. **データベース** — Oracle で `SQL/schema.sql` を実行。
5. **設定** — `META-INF/context.xml`（JNDI）または環境変数 `DB_URL`・`DB_USER`・`DB_PASSWORD` を設定。
6. **ビルド** — `compile_classes.bat` を実行して Java ソースをコンパイル。
7. **デプロイ** — アプリを Tomcat の `webapps` にコピーし、Tomcat を起動。
8. **アクセス** — `http://localhost:8080/Galaxy_bbs/` を開く（コンテキストパスは環境に合わせて変更してください）。

オプション：ウェルカムメール・パスワードリセット用に `set_smtp_env.bat` を実行し、SMTP を設定してください。

詳細な手順とトラブルシューティングは **[SETUP.md](SETUP.md)** を参照してください。

---

## プロジェクト構成

```
├── auth/              # ログイン・登録・パスワード忘れ・リセットページ
├── common/            # 共通ヘッダー、エラーページ
├── assets/            # CSS, JS, 画像
├── META-INF/          # Tomcat コンテキスト（JNDI DataSource）
├── WEB-INF/
│   ├── web.xml       # Servlets, フィルタ, マッピング
│   ├── src/
│   │   ├── controller/  # サーブレット
│   │   ├── dao/         # データアクセス
│   │   ├── filter/      # 認証・通知
│   │   ├── model/       # ドメインモデル
│   │   └── util/        # DB・パスワード・メールユーティリティ
│   └── views/        # JSP（フィード、投稿詳細、プロフィール等）
├── SQL/
│   └── schema.sql    # Oracle スキーマ
├── run.bat           # メニュー：依存取得、コンパイル、SMTP、起動
├── download_deps.bat
├── compile_classes.bat
└── set_smtp_env.bat
```

---

## ライセンス

本プロジェクトは教育・デモ目的です。自由に利用・改変してください。
