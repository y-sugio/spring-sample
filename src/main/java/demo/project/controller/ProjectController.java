package demo.project.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import demo.common.exception.BusinessException;
import demo.project.command.ProjectCreateCommand;
import demo.project.command.ProjectCreateCommandInput;
import demo.project.command.ProjectCreateCommandOutput;
import demo.project.command.ProjectDetailCommand;
import demo.project.command.ProjectDetailCommandInput;
import demo.project.command.ProjectListCommand;
import demo.project.command.ProjectListCommandInput;
import jakarta.validation.Valid;

/**
 * 案件管理の画面コントローラ。
 * リクエストの受け取り・CommandInput の生成・Command の呼び出し・Model への設定を行う。
 * 業務例外（BusinessException）はここでキャッチして元の画面を再描画する。
 */
@Controller
@RequestMapping("/page/projects")
public class ProjectController {

    private final ProjectListCommand listCommand;
    private final ProjectDetailCommand detailCommand;
    private final ProjectCreateCommand createCommand;
    private final MessageSource messageSource;

    public ProjectController(ProjectListCommand listCommand, ProjectDetailCommand detailCommand,
                             ProjectCreateCommand createCommand, MessageSource messageSource) {
        this.listCommand = listCommand;
        this.detailCommand = detailCommand;
        this.createCommand = createCommand;
        this.messageSource = messageSource;
    }

    /** 一覧（検索） */
    @GetMapping
    public String list(@ModelAttribute("searchForm") ProjectSearchForm searchForm,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        ProjectListCommandInput input = new ProjectListCommandInput(searchForm.q(), page, size);
        model.addAttribute("output", listCommand.execute(input));
        return "projects/list";
    }

    /** 登録フォーム */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ProjectForm(null));
        return "projects/new";
    }

    /** 登録実行 */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "projects/new";
        }
        ProjectCreateCommandOutput output;
        try {
            output = createCommand.execute(new ProjectCreateCommandInput(form.name()));
        } catch (BusinessException e) {
            // 業務例外: メッセージを設定して元の画面を再描画
            model.addAttribute("errorMessage", resolve(e.getMessageId()));
            return "projects/new";
        }
        ra.addFlashAttribute("message", resolve("MSG001"));
        return "redirect:/page/projects/" + output.projectId();
    }

    /** 詳細 */
    @GetMapping("/{projectId}")
    public String detail(@PathVariable Long projectId, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("project",
                    detailCommand.execute(new ProjectDetailCommandInput(projectId)));
        } catch (BusinessException e) {
            // 直接 GET のため再描画する元画面がない。一覧へ戻してメッセージを表示する
            ra.addFlashAttribute("errorMessage", resolve(e.getMessageId()));
            return "redirect:/page/projects";
        }
        return "projects/detail";
    }

    private String resolve(String messageId) {
        return messageSource.getMessage(messageId, null, Locale.getDefault());
    }
}
