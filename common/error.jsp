<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  if (exception != null) {
    java.util.logging.Logger.getLogger("GalaxyBBS").severe(
      "Error page: " + exception.getClass().getName() + ": " + exception.getMessage());
  }
%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Error - Galaxy BBS</title>
  </head>
  <body>
    <h1>Something went wrong</h1>
    <p>We're sorry. The request could not be completed. Please try again or return to the <a href="${pageContext.request.contextPath}/posts/feed">home page</a>.</p>
  </body>
</html>
