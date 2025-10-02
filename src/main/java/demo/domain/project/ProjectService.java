package demo.domain.project;

import demo.presentation.project.CreateProjectRequest;
import demo.presentation.project.ProjectDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProjectService {

    private final Map<Long, ProjectDto> store = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public ProjectService() {
        putSeed(1L, "2025年度 園児管理システム刷新", 1,
                "2025-01-10T18:00:00+09:00", "2025-03-02T00:30:00+09:00");
        putSeed(2L, "Webサイトリニューアル", 3,
                "2025-02-05T19:15:00+09:00", "2025-03-12T23:20:00+09:00");
        putSeed(3L, "モバイルアプリ開発", 2,
                "2025-01-20T20:45:00+09:00", "2025-03-01T01:50:00+09:00");
        seq.set(store.keySet().stream().mapToLong(Long::longValue).max().orElse(0L));
    }

    private void putSeed(Long id, String name, int version, String created, String updated) {
        store.put(id, new ProjectDto(
                id,
                name,
                version,
                OffsetDateTime.parse(created),
                OffsetDateTime.parse(updated)
        ));
    }

    public List<ProjectDto> list(String q, int page, int size, String sortKey, String sortDir) {
        List<ProjectDto> all = new ArrayList<>(store.values());
        if (q != null && !q.isBlank()) {
            final String needle = q.toLowerCase();
            all = all.stream()
                    .filter(p -> p.name() != null && p.name().toLowerCase().contains(needle))
                    .toList();
        }

        Comparator<ProjectDto> comp = switch (sortKey) {
            case "projectId" -> Comparator.comparing(ProjectDto::projectId, Comparator.nullsLast(Long::compareTo));
            case "name"      -> Comparator.comparing(ProjectDto::name, Comparator.nullsLast(String::compareToIgnoreCase));
            case "version"   -> Comparator.comparing(ProjectDto::version, Comparator.nullsLast(Integer::compareTo));
            case "createDate"-> Comparator.comparing(ProjectDto::createDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "updateDate"-> Comparator.comparing(ProjectDto::updateDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default          -> Comparator.comparing(ProjectDto::updateDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("desc".equalsIgnoreCase(sortDir)) comp = comp.reversed();
        all.sort(comp);

        int from = Math.max(0, page * Math.max(1, size));
        int to   = Math.min(all.size(), from + Math.max(1, size));
        return from >= all.size() ? List.of() : all.subList(from, to);
    }

    public ProjectDto get(Long projectId) {
        ProjectDto dto = store.get(projectId);
        if (dto == null) throw new NoSuchElementException("Project not found: " + projectId);
        return dto;
    }

    public ProjectDto create(CreateProjectRequest req) {
        long id = seq.incrementAndGet();
        OffsetDateTime nowJst = OffsetDateTime.now(ZoneOffset.ofHours(9));
        ProjectDto created = new ProjectDto(
                id,
                req.name(),
                1,
                nowJst,
                nowJst
        );
        store.put(id, created);
        return created;
    }
}

