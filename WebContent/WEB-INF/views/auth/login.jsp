<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>掲示板ログイン</title>
  <link rel="stylesheet" href="login.css">
  <style>
    .logo-header {
      position: absolute;
      top: 20px;
      left: 30px;
      z-index: 10;
    }
    .logo-header img {
      width: 80px;
      height: auto;
    }
  </style>
</head>
<body>

  <div class="logo-header">
    <img src="img/LOGO.png" alt="LOGO">
  </div>

  <!-- ここから下はログインフォームの部分 -->
  <div class="container">
    <h1>ユーザー登録</h1>

    <% if (request.getAttribute("errorMsg") != null) { %>
      <p style="color: #d32f2f; margin: 15px 0;"> <%= request.getAttribute("errorMsg") %> </p>
    <% } %>

    <form action="login" method="post">
      <label>メールアドレス</label>
      <input type="text" name="username" placeholder="" required autofocus>

      <label>パスワード</label>
      <input type="password" name="password" placeholder="" required>

      <div class="buttons">
        <button type="submit" class="login-btn">ログイン</button>
        <button type="button" class="signup-btn" onclick="location.href='signup.jsp'">サインアップ</button>
      </div>
    </form>
  </div>

</body>
</html>