package demo.presentation.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "案件作成リクエスト")
public record CreateProjectRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(description = "案件名", example = "Webサイトリニューアル")
        String name
) {}
