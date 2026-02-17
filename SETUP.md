# Galaxy BBS – セットアップガイド（GitHub からクローンした場合）

---

## クイックスタート（GitHub クローン後）

**メニュー方式:** `run.bat` を実行すると、依存取得・コンパイル・Tomcat 起動などを選べます。

```batch
REM 1. リポジトリをクローン
git clone https://github.com/NAYEM-MD/Galaxy_bbs.git
cd Galaxy_bbs

REM 2. 依存 JAR を取得（ojdbc は手動追加が必要）
download_deps.bat

REM 3. Oracle JDBC を WEB-INF\lib に配置
REM    ojdbc8.jar または ojdbc11.jar を Oracle / Maven から入手してコピー

REM 4. 依存関係の確認
compile_classes.bat check

REM 5. コンパイル
compile_classes.bat

REM 6. DB セットアップ（Oracle が起動していること）
REM    SQL/schema.sql を実行

REM 7. Tomcat の webapps に配置し、startup.bat で起動
REM    http://localhost:8080/Galaxy_bbs/
```

---

## 1. 必要環境

- **Java**（JDK 11 以上）
- **Apache Tomcat** 10.x（9.x でも微調整で動作可能）
- **Oracle Database**（Express Edition またはフル版）
- **Oracle JDBC ドライバ**（`ojdbc8.jar` または `ojdbc11.jar`）を `WEB-INF/lib/` に配置（手動）
- **jBCrypt** と **Jakarta Mail** は `download_deps.bat` で自動取得可能

---

## 2. データベースのセットアップ

### 2.1 スキーマ／ユーザの作成（必要な場合）

Oracle ユーザ `Galaxy_bbs` が存在しない場合、特権ユーザ（例：SYS）で以下を実行してください。

```sql
CREATE USER Galaxy_bbs IDENTIFIED BY pro;
GRANT CONNECT, RESOURCE TO Galaxy_bbs;
ALTER USER Galaxy_bbs QUOTA UNLIMITED ON USERS;
```

### 2.2 フルスキーマの実行

`Galaxy_bbs` / `pro`（またはアプリ用ユーザ）で接続し、次を実行します。

```
SQL/schema.sql
```

これで `password_reset_tokens` を含む全テーブルが作成されます。

### 2.3 古いスキーマを既に実行済みの場合（ORA-00942 エラー）

`password_reset_tokens` で **ORA-00942: 表またはビューが存在しません** が出る場合は、`SQL/schema.sql` 末尾の「Migration」セクション内の `password_reset_tokens` 用の CREATE 文だけをコピーして実行してください。既存データを残したまま、不足しているテーブルのみ追加します。

---

## 3. データベース接続の設定

デフォルト接続情報：

- **URL:** `jdbc:oracle:thin:@localhost:1521:orcl`
- **ユーザ:** `Galaxy_bbs`
- **パスワード:** `pro`

上書きする場合は、**Tomcat 起動前に** 環境変数を設定してください。

```batch
set DB_URL=jdbc:oracle:thin:@//YOUR_HOST:1521/YOUR_SERVICE
set DB_USER=Galaxy_bbs
set DB_PASSWORD=pro
```

Windows の例：

```batch
set DB_URL=jdbc:oracle:thin:@//192.168.1.50:1521/orclpdb1
set DB_USER=Galaxy_bbs
set DB_PASSWORD=pro
```

---

## 4. プロジェクトのビルド

```batch
compile_classes.bat check    REM 必要な JAR があるか確認
compile_classes.bat         REM コンパイル
```

依存 JAR をまとめて取得する場合：

```batch
download_deps.bat
REM ojdbc8.jar は Oracle から手動で入手し WEB-INF\lib に配置
```

DB 接続テスト（任意）：

```batch
compile_classes.bat dbtest "jdbc:oracle:thin:@localhost:1521:orcl" Galaxy_bbs pro
```

---

## 5. Tomcat へのデプロイ

- プロジェクトフォルダ（または WAR）を `%CATALINA_HOME%\webapps\` にコピー
- フォルダ名が `Galaxy_bbs` の場合: `http://localhost:8080/Galaxy_bbs/`
- `ttc-bbs` としてデプロイしている場合: `http://localhost:8080/ttc-bbs/`

---

## 6. Tomcat の起動

Tomcat を起動してください（例：`startup.bat`）。`DB_URL`、`DB_USER`、`DB_PASSWORD` は、Tomcat を起動する環境で設定されている必要があります。

---

## トラブルシューティング

| エラー | 対処 |
|--------|------|
| `password_reset_tokens` で ORA-00942 | `SQL/schema.sql` 末尾の Migration セクション内の CREATE 文を実行（2.3 参照） |
| Oracle JDBC Driver not found | `ojdbc8.jar` を `WEB-INF/lib/` に追加 |
| ClassNotFoundException: org.mindrot.jbcrypt.BCrypt | `jbcrypt-0.4.jar` を `WEB-INF/lib/` に追加 |
| DB 接続失敗 | `DB_URL`、`DB_USER`、`DB_PASSWORD` と Oracle の起動を確認 |
| Tomcat lib not found | `CATALINA_HOME` を設定（例：`set CATALINA_HOME=C:\tomcat10`）または `compile_classes.bat` を編集 |
| 依存 JAR が足りない | `download_deps.bat` 実行 → `ojdbc8.jar` を手動で `WEB-INF\lib` に追加 |
