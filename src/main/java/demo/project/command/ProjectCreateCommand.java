package demo.project.command;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import demo.common.db.DbCall;
import demo.common.exception.BusinessException;
import demo.common.mapper.ProjectEntity;
import demo.common.mapper.ProjectMapper;
import demo.project.task.ProjectDuplicateCheckTask;

/** 案件登録ユースケース。業務ロジック（重複チェック）は Task に委譲する */
@Component
public class ProjectCreateCommand {

    private final ProjectDuplicateCheckTask duplicateCheckTask;
    private final ProjectMapper projectMapper;
    private final DbCall dbCall;

    public ProjectCreateCommand(ProjectDuplicateCheckTask duplicateCheckTask,
                                ProjectMapper projectMapper, DbCall dbCall) {
        this.duplicateCheckTask = duplicateCheckTask;
        this.projectMapper = projectMapper;
        this.dbCall = dbCall;
    }

    @Transactional
    public ProjectCreateCommandOutput execute(ProjectCreateCommandInput input) {
        if (duplicateCheckTask.execute(input.name())) {
            // 既に登録済み: 登録できないため業務例外を throw（Controller でキャッチ）
            throw new BusinessException("BIZ002");
        }
        ProjectEntity entity = new ProjectEntity();
        entity.setName(input.name());
        dbCall.execute("SYS001", () -> projectMapper.insert(entity));
        return new ProjectCreateCommandOutput(entity.getProjectId());
    }
}
