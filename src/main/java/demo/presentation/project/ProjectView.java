package demo.presentation.project;

import java.time.OffsetDateTime;

public class ProjectView {
    private Long projectId;
    private String name;
    private Integer version;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

    public static ProjectView from(ProjectDto dto) {
        ProjectView v = new ProjectView();
        v.projectId = dto.projectId();
        v.name = dto.name();
        v.version = dto.version();
        v.createDate = dto.createDate();
        v.updateDate = dto.updateDate();
        return v;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public Integer getVersion() {
        return version;
    }

    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    public OffsetDateTime getUpdateDate() {
        return updateDate;
    }
}

