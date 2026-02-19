Galaxy BBS — プロジェクト概要

Galaxy BBS は、Java Servlets、JSP、Oracle Database を用いて構築された
Reddit風の掲示板システムです。
投稿、ネストコメント、投票、アンケート、メディアアップロード、
ユーザープロフィール、通知機能、ライト／ダークモードをサポートしています。

------------------------------------------------------------
1. プロジェクト概要
------------------------------------------------------------

主な機能:
- ユーザー登録・ログイン（bcrypt によるパスワードハッシュ化）
- 投稿作成（テキスト／画像／動画／アンケート）
- 投稿の賛成／反対投票
- ネスト形式のコメント
- 動的更新されるアンケート投票
- 通知機能（コメント、メンション）
- プロフィール・設定管理
- ライト／ダークテーマ切り替え

アーキテクチャ:
- MVCスタイル構成
  - Servlets = コントローラ
  - JSP = ビュー
  - DAO = データアクセス層
  - Oracle DB = データ永続化

------------------------------------------------------------
2. リクエスト処理の流れ
------------------------------------------------------------

全体の流れ:
User → Tomcat → Filters → Servlet → DAO → Oracle DB
                          ↓
                        JSP
                          ↓
                      HTMLレスポンス

認証処理:
- AuthFilter が /posts/* を保護
- ログイン:
  - AuthServlet → UserDAO.checkLogin()
  - jBCrypt によるパスワード検証
  - Session に "user" を保存
- 新規登録:
  - バリデーション → bcrypt ハッシュ化 → DB登録
  - Jakarta Mail によるウェルカムメール送信
- パスワードリセット:
  - SHA-256 トークンを password_reset_tokens に保存
  - リセットリンクをメール送信

投稿とフィード:
- /posts/feed → PostDAO.findAll() または search()
- 投稿作成:
  - multipart/form-data によるメディアアップロード
  - メディアはディスク保存
  - アンケートと選択肢をDBに登録
- 投稿詳細:
  - 投稿とコメントを読み込み
  - buildCommentTree() によるネスト構造生成

投票とアンケート（AJAX）:
- 投稿投票:
  - fetch('/vote')
  - VoteServlet → VoteDAO
  - JSON を返却（success, newCount, userVote）
- アンケート投票:
  - XHR → /polls/vote
  - プログレスバーを動的更新

コメント:
- CommentServlet → CommentDAO.insert()
- 投稿者へ通知を作成

通知:
- NotificationFilter が unreadCount を追加
- ヘッダーが /notifications/count を30秒ごとにポーリング
- NotificationServlet が一覧表示および既読処理

プロフィール・設定:
- /profile → プロフィール画面
- プロフィール画像アップロード（multipart）
- /settings → ユーザー設定更新

------------------------------------------------------------
3. 技術スタック
------------------------------------------------------------

実行環境: Java (JDK 11以上)
サーバー: Apache Tomcat 10
データベース: Oracle Database
Web層: Jakarta Servlets
ビュー: JSP + JSTL
セキュリティ: jBCrypt (bcrypt)
メール: Jakarta Mail
JDBC: Oracle ojdbc8 / ojdbc11
コネクションプール: Tomcat JDBC Pool (JNDI)
フロントエンド: HTML, CSS, Vanilla JavaScript
ビルド: javac + バッチスクリプト

------------------------------------------------------------
4. 各技術の使用箇所
------------------------------------------------------------

バックエンド構成:
controller/ → サーブレット
dao/ → データベースアクセス（UserDAO, PostDAO など）
model/ → エンティティクラス
filter/ → AuthFilter, NotificationFilter
util/ → DBUtil, PasswordUtil, EmailUtil, ValidationUtil など

データベーステーブル:
- users
- password_reset_tokens
- posts
- comments（parent_id によるネスト）
- votes
- polls, poll_options, poll_votes
- notifications
- tags

データベース接続:
- META-INF/context.xml → JNDI DataSource (jdbc/GalaxyBBS)
- DBUtil は JNDI（Tomcat）または DriverManager（環境変数）を使用

フロントエンド資産:
CSS:
- reset.css, theme.css, main.css, feed.css, post.css, auth.css

JavaScript:
- theme.js（ダークモード切替）
- vote.js（AJAX投票）
- poll.js（アンケート投票）
- auth.js, profile.js

ビルドスクリプト:
- download_deps.bat（依存関係取得）
- compile_classes.bat（Javaコンパイル）
- run.bat（メインメニュー）
- set_smtp_env.bat（SMTP環境変数設定）

------------------------------------------------------------
5. 設計パターン
------------------------------------------------------------

- MVCスタイルアーキテクチャ
- DAOパターン（エンティティごとにDAO）
- フィルターチェーン（AuthFilter → NotificationFilter）
- セッションベース認証
- JNDIコネクションプーリング + フォールバック構成

------------------------------------------------------------
6. ディレクトリ構成
------------------------------------------------------------

Galaxy_bbs/
|-- index.jsp
|-- auth/
|-- common/
|-- assets/
|   |-- css/
|   |-- js/
|   |-- img/
|-- META-INF/
|   `-- context.xml
|-- WEB-INF/
|   |-- web.xml
|   |-- classes/
|   |-- lib/
|   |-- src/
|   |   |-- controller/
|   |   |-- dao/
|   |   |-- filter/
|   |   |-- model/
|   |   `-- util/
|   `-- views/
|-- SQL/
|   `-- schema.sql
|-- run.bat
|-- download_deps.bat
|-- compile_classes.bat
|-- set_smtp_env.bat
|-- SETUP.md
`-- README.md

------------------------------------------------------------
7. クイックセットアップ
------------------------------------------------------------

1. リポジトリをクローン
2. download_deps.bat を実行
3. ojdbc8.jar または ojdbc11.jar を WEB-INF/lib に追加
4. compile_classes.bat check を実行
5. compile_classes.bat を実行
6. SQL/schema.sql を Oracle で実行
7. DB設定（context.xml または環境変数）
8. （任意）SMTP環境変数を設定
9. Tomcat にデプロイ
10. http://localhost:8080/Galaxy_bbs/ を開く

詳細およびトラブルシューティングは SETUP.md を参照。

------------------------------------------------------------
プレゼンテーションガイド
------------------------------------------------------------

1. プロジェクト概要
2. アーキテクチャ図
3. 技術スタック
4. 機能のリクエストフロー例（ログインまたは投稿作成）
5. 技術の使用箇所
6. ディレクトリ構成
7. デモ
8. セットアップ概要
