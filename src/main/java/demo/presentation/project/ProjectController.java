// src/main/java/demo/presentation/project/ProjectController.java
package demo.presentation.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import demo.domain.project.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@Tag(name = "Projects", description = "案件API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    // ====== 案件一覧（配列レスポンス／ページングはクエリ引数だけ先に固定） ======
    @Operation(
            summary = "案件一覧（ページングあり：配列レスポンス）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProjectDto[].class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "default",
                                                    externalValue = "classpath:openapi/examples/project-list.json"
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping
    public List<ProjectDto> list(
            @Parameter(description = "名前の部分一致（例: 刷新）")
            @RequestParam(required = false) String q,
            @Parameter(description = "ページ番号（0開始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "ページサイズ", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ソートキー（例: updateDate）", example = "updateDate")
            @RequestParam(defaultValue = "updateDate") String sortKey,
            @Parameter(description = "ソート順（asc|desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return service.list(q, page, size, sortKey, sortDir);
    }

    // ====== 案件詳細 ======
    @Operation(
            summary = "案件詳細取得",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProjectDto.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            }
    )
    @GetMapping("/{projectId}")
    public ProjectDto get(@PathVariable Long projectId) {
        return service.get(projectId);
    }

    // ====== 案件作成 ======
    @Operation(
            summary = "案件作成",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateProjectRequest.class),
                            examples = @ExampleObject(externalValue = "classpath:openapi/examples/create-project.json"))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProjectDto.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Error")
            }
    )
    @PostMapping
    public ResponseEntity<ProjectDto> create(@Valid @RequestBody CreateProjectRequest req) {
        ProjectDto created = service.create(req);
        return ResponseEntity.created(URI.create("/api/projects/" + created.projectId())).body(created);
    }
}
