package demo.presentation.project;

import demo.domain.project.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/page/projects")
public class ProjectPageController {

    private final ProjectService service;

    public ProjectPageController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        List<ProjectDto> list = service.list(q, page, size, "updateDate", "desc");
        List<ProjectView> projects = list.stream().map(ProjectView::from).toList();
        model.addAttribute("projects", projects);
        model.addAttribute("q", q);
        return "projects/list";
    }

    @GetMapping("/new")
    public String newForm() {
        return "projects/new";
    }

    @PostMapping
    public String create(@RequestParam("name") String name, RedirectAttributes ra) {
        ProjectDto created = service.create(new CreateProjectRequest(name));
        ra.addFlashAttribute("message", "案件を作成しました");
        return "redirect:/page/projects/" + created.projectId();
    }

    @GetMapping("/{projectId}")
    public String detail(@PathVariable Long projectId, Model model) {
        ProjectDto project = service.get(projectId);
        model.addAttribute("project", ProjectView.from(project));
        return "projects/detail";
    }
}
