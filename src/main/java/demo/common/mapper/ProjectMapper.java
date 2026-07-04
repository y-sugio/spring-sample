package demo.common.mapper;

import java.util.List;

/**
 * 案件テーブル（単一テーブル）の Mapper。
 * 単一テーブルを扱うため common.mapper に配置する（architecture.md §2）。
 * MyBatis 導入時に @Mapper インターフェース + SQL に置き換える。
 * 呼び出しは必ず DbCall 経由で行う。
 */
public interface ProjectMapper {

    /** 案件名の部分一致検索（大文字小文字を区別しない）。更新日時降順 */
    List<ProjectEntity> search(String q, int offset, int limit);

    /** 主キー検索。該当なしは null */
    ProjectEntity findById(Long projectId);

    /** 同名の案件が存在するか */
    boolean existsByName(String name);

    /** 登録。projectId・version・登録/更新日時はサーバ側で採番・設定する（DB では sysdate） */
    void insert(ProjectEntity entity);
}
