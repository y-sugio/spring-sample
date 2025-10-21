package demo.presentation.project;

import java.time.OffsetDateTime;

public record ProjectDto(
        Long projectId,
        String name,
        Integer version,
        OffsetDateTime createDate,
        OffsetDateTime updateDate
) {}
