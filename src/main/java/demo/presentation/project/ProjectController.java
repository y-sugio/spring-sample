// src/main/java/demo/presentation/project/ProjectController.java
package demo.presentation.project;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import demo.domain.project.ProjectService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    // ====== 案件一覧（配列レスポンス／ページングはクエリ引数だけ先に固定） ======
    @GetMapping
    public List<ProjectDto> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updateDate") String sortKey,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return service.list(q, page, size, sortKey, sortDir);
    }

    // ====== 案件詳細 ======
    @GetMapping("/{projectId}")
    public ProjectDto get(@PathVariable Long projectId) {
        return service.get(projectId);
    }

    // ====== 案件作成 ======
    @PostMapping
    public ResponseEntity<ProjectDto> create(@Valid @RequestBody CreateProjectRequest req) {
        ProjectDto created = service.create(req);
        return ResponseEntity.created(URI.create("/api/projects/" + created.projectId())).body(created);
    }
}
