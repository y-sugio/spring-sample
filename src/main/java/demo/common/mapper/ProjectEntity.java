package demo.common.mapper;

import java.time.LocalDateTime;

/**
 * 案件テーブルのエンティティ。
 * 区分値を持つ場合はコード値（String）のまま保持する（Enum 変換は CommandOutput / Dto で行う）。
 */
public class ProjectEntity {

    private Long projectId;
    private String name;
    private Integer version;
    /** 登録日時。DB では sysdate で登録する（アプリから値を渡さない） */
    private LocalDateTime createDate;
    /** 更新日時。DB では sysdate で登録する（アプリから値を渡さない） */
    private LocalDateTime updateDate;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "ProjectEntity{projectId=" + projectId + ", name=" + name + ", version=" + version + "}";
    }
}
