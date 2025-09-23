<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>案件一覧</title>
    <style>
        table{border-collapse:collapse}
        th,td{border:1px solid #ddd;padding:6px 8px}
        thead th{text-align:left;background:#f6f7f8}
    </style>
</head>
<body>
<h1>案件一覧</h1>
<div>
    <form method="get" action="/page/projects">
        <input type="text" name="q" placeholder="検索（案件名）" value="<c:out value='${q}'/>" />
        <button type="submit">検索</button>
    </form>
    <button type="button" onclick="location.href='/page/projects/new'">案件作成</button>
    <button type="button" onclick="location.href='/'">Home</button>
    <span>（最大20件まで表示）</span>
    <span><c:out value='${pageContext.request.requestedSessionId}'/></span>
    </div>

<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>案件名</th>
        <th>バージョン</th>
        <th>作成日時</th>
        <th>更新日時</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${projects}" var="p">
        <tr>
            <td><a href="/page/projects/<c:out value='${p.projectId}'/>"><c:out value='${p.projectId}'/></a></td>
            <td><c:out value='${p.name}'/></td>
            <td><c:out value='${p.version}'/></td>
            <td><c:out value='${p.createDate}'/></td>
            <td><c:out value='${p.updateDate}'/></td>
        </tr>
    </c:forEach>
    <c:if test="${empty projects}">
        <tr><td colspan="5">データがありません</td></tr>
    </c:if>
    </tbody>
    </table>
</body>
</html>
