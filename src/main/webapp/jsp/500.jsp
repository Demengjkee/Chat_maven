<%@ page isErrorPage="true" import="java.io.*" contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>500 Internal Server Error</title>
</head>
<body>
<h1>Internal Server Error</h1>
Message:
<%=exception.getMessage()%>

StackTrace:
<%
  StringWriter stringWriter = new StringWriter();
  PrintWriter printWriter = new PrintWriter(stringWriter);
  exception.printStackTrace(printWriter);
  out.println(stringWriter);
  printWriter.close();
  stringWriter.close();
%>

</body>
</html>
