package demo.project.command;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import demo.common.db.DbCall;
import demo.common.exception.BusinessException;
import demo.common.mapper.ProjectEntity;
import demo.common.mapper.ProjectMapper;

/** 案件詳細表示ユースケース */
@Component
public class ProjectDetailCommand {

    private final ProjectMapper projectMapper;
    private final DbCall dbCall;

    public ProjectDetailCommand(ProjectMapper projectMapper, DbCall dbCall) {
        this.projectMapper = projectMapper;
        this.dbCall = dbCall;
    }

    @Transactional(readOnly = true)
    public ProjectDetailCommandOutput execute(ProjectDetailCommandInput input) {
        ProjectEntity entity = dbCall.execute("SYS001",
                () -> projectMapper.findById(input.projectId()));
        if (entity == null) {
            // 該当データなし: CommandOutput を返すルートがないため業務例外を throw
            throw new BusinessException("BIZ001");
        }
        return ProjectDetailCommandOutput.from(entity);
    }
}
