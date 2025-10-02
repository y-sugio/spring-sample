<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>JSP Sample</title>
    </head>
<body>
<h1>Spring MVC + JSP</h1>
<p>JSP is wired. Render time: <code><c:out value='${pageContext.request.requestedSessionId}'/></code></p>
<p>Return view name <code>"index"</code> from a controller to render this page.</p>
</body>
</html>
