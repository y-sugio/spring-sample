<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>案件詳細</title>

  <!-- Bootstrap (WebJars) -->
  <link href="${pageContext.request.contextPath}/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>

  <style>
    .detail-card { max-width: 960px; }
  </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/page/projects">案件管理</a>
    <div class="ms-auto">
      <a class="btn btn-outline-light btn-sm me-2" href="${pageContext.request.contextPath}/page/projects">一覧へ</a>
      <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/page/projects/new">新規作成</a>
    </div>
  </div>
</nav>

<main class="container py-4">
  <nav aria-label="breadcrumb">
    <ol class="breadcrumb small">
      <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/page/projects">案件一覧</a></li>
      <li class="breadcrumb-item active" aria-current="page">
        ID: <c:out value='${project.projectId}'/>
      </li>
    </ol>
  </nav>

  <div class="d-flex align-items-center justify-content-between mb-3">
    <h1 class="h3 m-0"><c:out value='${project.name}'/></h1>
  </div>

  <div class="card shadow-sm detail-card">
    <div class="card-header bg-white"><strong>案件詳細</strong></div>
    <div class="card-body">
      <div class="table-responsive">
        <table class="table table-sm table-hover align-middle mb-0">
          <tbody>
          <tr>
            <th style="width: 180px;" class="text-muted fw-normal">案件ID</th>
            <td><span class="badge text-bg-secondary"><c:out value='${project.projectId}'/></span></td>
          </tr>
          <tr>
            <th class="text-muted fw-normal">バージョン</th>
            <td><c:out value='${project.version}'/></td>
          </tr>
          <tr>
            <th class="text-muted fw-normal">作成日時</th>
            <td><c:out value='${project.createDate}'/></td>
          </tr>
          <tr>
            <th class="text-muted fw-normal">更新日時</th>
            <td><c:out value='${project.updateDate}'/></td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div class="card-footer bg-white d-flex gap-2">
      <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/page/projects">一覧へ戻る</a>
      <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/page/projects/<c:out value='${project.projectId}'/>/edit">編集</a>
      <form method="post" action="${pageContext.request.contextPath}/page/projects/<c:out value='${project.projectId}'/>/delete" class="ms-auto">
        <button type="submit" class="btn btn-outline-danger" onclick="return confirm('削除してよろしいですか？');">削除</button>
      </form>
    </div>
  </div>
</main>

<script src="${pageContext.request.contextPath}/webjars/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
