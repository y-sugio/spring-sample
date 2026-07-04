package demo.presentation.project;

import demo.domain.project.ProjectService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

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
        List<ProjectDto> projects = service.list(q, page, size, "updateDate", "desc");
        model.addAttribute("projects", projects);
        model.addAttribute("q", q);
        return "projects/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CreateProjectRequest(null));
        return "projects/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CreateProjectRequest form,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "projects/new";
        }
        ProjectDto created = service.create(form);
        ra.addFlashAttribute("message", "案件を作成しました");
        return "redirect:/page/projects/" + created.projectId();
    }

    @GetMapping("/{projectId}")
    public String detail(@PathVariable Long projectId, Model model) {
        ProjectDto project = service.get(projectId);
        model.addAttribute("project", project);
        return "projects/detail";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("message", "指定された案件は存在しません");
        return "error/404";
    }
}
