<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>案件作成</title>
</head>
<body>
<h1>案件作成</h1>

<form method="post" action="/page/projects">
    <div>
        <label for="name">案件名</label>
        <input id="name" name="name" type="text" maxlength="100" required />
    </div>
    <div>
        <button type="submit">作成</button>
        <a href="/page/projects">キャンセル</a>
    </div>
</form>

</body>
</html>
