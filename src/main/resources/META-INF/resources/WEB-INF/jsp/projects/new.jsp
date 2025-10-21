<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>案件作成</title>

  <!-- Bootstrap (WebJars経由) -->
  <link href="${pageContext.request.contextPath}/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>

  <style>
    .form-container {
      max-width: 600px;
      margin: 2rem auto;
    }
  </style>
</head>

<body class="bg-light">
  <!-- ヘッダー -->
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <a class="navbar-brand" href="${pageContext.request.contextPath}/page/projects">案件管理</a>
      <div class="ms-auto">
        <a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/page/projects">
          <i class="bi bi-arrow-left"></i> 一覧へ戻る
        </a>
      </div>
    </div>
  </nav>

  <main class="container form-container">
    <div class="card shadow-sm">
      <div class="card-header bg-white">
        <h1 class="h4 m-0">案件作成</h1>
      </div>

      <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/page/projects">
          <!-- 案件名 -->
          <div class="mb-3">
            <label for="name" class="form-label fw-semibold">案件名</label>
            <input id="name" name="name" type="text"
                   maxlength="100" required
                   class="form-control"
                   placeholder="例：新規システム開発プロジェクト" />
            <div class="form-text text-muted">100文字以内で入力してください。</div>
          </div>

          <!-- ボタン -->
          <div class="d-flex justify-content-end gap-2 mt-4">
            <a href="${pageContext.request.contextPath}/page/projects"
               class="btn btn-outline-secondary">キャンセル</a>
            <button type="submit" class="btn btn-primary">作成</button>
          </div>
        </form>
      </div>
    </div>
  </main>

  <script src="${pageContext.request.contextPath}/webjars/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
