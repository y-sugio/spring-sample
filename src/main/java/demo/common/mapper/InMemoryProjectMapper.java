package demo.common.mapper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

/**
 * ProjectMapper のインメモリ仮実装。
 * Oracle + MyBatis 導入までのつなぎ（アプリ再起動でデータは消える）。
 * DB の sysdate に相当する日時設定もここで代替している。
 */
@Repository
public class InMemoryProjectMapper implements ProjectMapper {

    private final Map<Long, ProjectEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public InMemoryProjectMapper() {
        seed("2025年度 園児管理システム刷新");
        seed("Webサイトリニューアル");
        seed("モバイルアプリ開発");
    }

    private void seed(String name) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(name);
        insert(entity);
    }

    @Override
    public List<ProjectEntity> search(String q, int offset, int limit) {
        return store.values().stream()
                .filter(p -> q == null || q.isBlank()
                        || p.getName().toLowerCase().contains(q.toLowerCase()))
                .sorted(Comparator.comparing(ProjectEntity::getUpdateDate).reversed())
                .skip(Math.max(0, offset))
                .limit(Math.max(1, limit))
                .toList();
    }

    @Override
    public ProjectEntity findById(Long projectId) {
        return store.get(projectId);
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream().anyMatch(p -> p.getName().equals(name));
    }

    @Override
    public void insert(ProjectEntity entity) {
        long id = seq.incrementAndGet();
        LocalDateTime now = LocalDateTime.now(); // DB 実装では sysdate
        entity.setProjectId(id);
        entity.setVersion(1);
        entity.setCreateDate(now);
        entity.setUpdateDate(now);
        store.put(id, entity);
    }
}
