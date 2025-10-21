<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>案件一覧</title>

  <!-- Bootstrap (WebJars経由) -->
  <link href="${pageContext.request.contextPath}/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>

  <style>
    .table thead th {
      background-color: #f8f9fa;
    }
    .search-form input {
      max-width: 250px;
    }
  </style>
</head>

<body class="bg-light">
  <!-- ヘッダー -->
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <a class="navbar-brand" href="${pageContext.request.contextPath}/">案件管理</a>
      <div class="ms-auto">
        <a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/page/projects/new">
          <i class="bi bi-plus-lg"></i> 新規作成
        </a>
        <a class="btn btn-outline-light btn-sm ms-2" href="${pageContext.request.contextPath}/">
          <i class="bi bi-house"></i> Home
        </a>
      </div>
    </div>
  </nav>

  <main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="h3 mb-0">案件一覧</h1>
      <span class="text-muted small">（最大20件まで表示）</span>
    </div>

    <!-- 検索フォーム -->
    <form class="row g-2 align-items-center mb-4 search-form" method="get" action="${pageContext.request.contextPath}/page/projects">
      <div class="col-auto">
        <input type="text" class="form-control form-control-sm"
               name="q" placeholder="検索（案件名）"
               value="<c:out value='${q}'/>" />
      </div>
      <div class="col-auto">
        <button type="submit" class="btn btn-primary btn-sm">検索</button>
      </div>
      <div class="col-auto">
        <span class="text-muted small">
          セッションID: <c:out value='${pageContext.request.requestedSessionId}'/>
        </span>
      </div>
    </form>

    <!-- テーブル -->
    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm align-middle mb-0">
            <thead>
              <tr>
                <th scope="col" style="width:5%">ID</th>
                <th scope="col">案件名</th>
                <th scope="col" style="width:10%">バージョン</th>
                <th scope="col" style="width:20%">作成日時</th>
                <th scope="col" style="width:20%">更新日時</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${projects}" var="p">
                <tr>
                  <td>
                    <a href="${pageContext.request.contextPath}/page/projects/<c:out value='${p.projectId}'/>"
                       class="text-decoration-none">
                       <c:out value='${p.projectId}'/>
                    </a>
                  </td>
                  <td><c:out value='${p.name}'/></td>
                  <td><c:out value='${p.version}'/></td>
                  <td><c:out value='${p.createDate}'/></td>
                  <td><c:out value='${p.updateDate}'/></td>
                </tr>
              </c:forEach>

              <c:if test="${empty projects}">
                <tr>
                  <td colspan="5" class="text-center text-muted py-3">データがありません</td>
                </tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </main>

  <script src="${pageContext.request.contextPath}/webjars/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
