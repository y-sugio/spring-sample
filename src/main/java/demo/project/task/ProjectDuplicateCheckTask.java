package demo.project.task;

import org.springframework.stereotype.Component;

import demo.common.db.DbCall;
import demo.common.mapper.ProjectMapper;

/**
 * 案件名の重複チェック（業務ロジック）。
 * Task は原則 1 項目を返す。入力値が 6 つ未満のため TaskInput は作らず引数で渡す
 * （coding-rules.md §2）。
 */
@Component
public class ProjectDuplicateCheckTask {

    private final ProjectMapper projectMapper;
    private final DbCall dbCall;

    public ProjectDuplicateCheckTask(ProjectMapper projectMapper, DbCall dbCall) {
        this.projectMapper = projectMapper;
        this.dbCall = dbCall;
    }

    /** @return 同名の案件が既に存在する場合 true */
    public boolean execute(String name) {
        return dbCall.execute("SYS001", () -> projectMapper.existsByName(name));
    }
}
