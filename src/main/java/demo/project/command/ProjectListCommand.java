package demo.project.command;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import demo.common.db.DbCall;
import demo.common.mapper.ProjectEntity;
import demo.common.mapper.ProjectMapper;

/**
 * 案件一覧検索ユースケース（ユースケースと 1:1）。
 * トランザクション境界はここ（@Transactional は Command のみ）。
 */
@Component
public class ProjectListCommand {

    private final ProjectMapper projectMapper;
    private final DbCall dbCall;

    public ProjectListCommand(ProjectMapper projectMapper, DbCall dbCall) {
        this.projectMapper = projectMapper;
        this.dbCall = dbCall;
    }

    @Transactional(readOnly = true)
    public ProjectListCommandOutput execute(ProjectListCommandInput input) {
        int offset = input.page() * input.size();
        List<ProjectEntity> entities = dbCall.execute("SYS001",
                () -> projectMapper.search(input.q(), offset, input.size()));
        return new ProjectListCommandOutput(entities.stream().map(ProjectDto::from).toList());
    }
}
