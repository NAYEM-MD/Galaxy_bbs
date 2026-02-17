# Galaxy BBS – セットアップガイド（GitHub からクローンした場合）

GitHub からプロジェクトをクローンしてローカルで実行する際の手順です。

---

## 1. 必要環境

- **Java**（JDK 11 以上）
- **Apache Tomcat** 10.x（9.x でも微調整で動作可能）
- **Oracle Database**（Express Edition またはフル版）
- **Oracle JDBC ドライバ**（`ojdbc8.jar` または `ojdbc11.jar`）を `WEB-INF/lib/` に配置

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

プロジェクトルートで実行：

```batch
compile_classes.bat
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
| DB 接続失敗 | `DB_URL`、`DB_USER`、`DB_PASSWORD` と Oracle の起動を確認 |
| Tomcat lib not found | `compile_classes.bat` 内で `CATALINA_HOME`（例：`C:\tomcat10`）を設定 |
