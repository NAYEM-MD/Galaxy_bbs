<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Error - Galaxy BBS</title>
    <style>
      pre {
        background: #f5f5f5;
        padding: 1em;
        overflow: auto;
      }
    </style>
  </head>
  <body>
    <h1>Something went wrong</h1>
    <c:if test="${not empty pageContext.exception}">
    <p><strong>Exception:</strong> <c:out value="${pageContext.exception['class'].name}"/></p>
    <p><strong>Message:</strong> <c:out value="${pageContext.exception.message}"/></p>
    <c:if test="${not empty pageContext.exception.cause}">
    <p>
      <strong>Caused by:</strong> <c:out value="${pageContext.exception.cause['class'].name}"/>: <c:out value="${pageContext.exception.cause.message}"/>
    </p>
    </c:if>
    <pre><%
        if (exception != null) { exception.printStackTrace(new java.io.PrintWriter(out)); }
    %></pre>
    </c:if>
  </body>
</html>
