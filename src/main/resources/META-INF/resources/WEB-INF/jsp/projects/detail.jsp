<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>案件詳細</title>
    <style>
        table{border-collapse:collapse}
        th,td{border:1px solid #ddd;padding:6px 8px}
        thead th{text-align:left;background:#f6f7f8}
    </style>
</head>
<body>
<div>
    <a href="/page/projects">一覧へ</a>
    <a href="/page/projects/new">新規作成</a>
    <span>ID: <c:out value='${project.projectId}'/></span>
    </div>

<h1><c:out value='${project.name}'/></h1>

<table>
    <tbody>
    <tr>
        <th>案件ID</th>
        <td><c:out value='${project.projectId}'/></td>
    </tr>
    <tr>
        <th>バージョン</th>
        <td><c:out value='${project.version}'/></td>
    </tr>
    <tr>
        <th>作成日時</th>
        <td><c:out value='${project.createDate}'/></td>
    </tr>
    <tr>
        <th>更新日時</th>
        <td><c:out value='${project.updateDate}'/></td>
    </tr>
    </tbody>
    </table>

</body>
</html>
