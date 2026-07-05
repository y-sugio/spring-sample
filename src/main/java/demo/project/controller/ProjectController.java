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
import jakarta.servlet.http.HttpSession;
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
 * リクエストの受け取り・CommandInput の生成・Command の呼び出し・Model への設定・
 * セッション管理（検索条件の保持）を行う。
 * 業務例外（BusinessException）はここでキャッチして元の画面を再描画する。
 */
@Controller
@RequestMapping("/page/projects")
public class ProjectController {

    /**
     * 「戻る」時の検索条件・ページング復元用のセッションキー。
     * TODO: 状態保持の実現方式（セッション保持か hidden パラメータ引き回しか）が
     *       未確定のため、セッション保持の仮実装（requirements/overview.md §7）。
     */
    static final String SESSION_SEARCH_CONDITION = "projects.searchCondition";

    /** セッションに保持する検索条件（画面遷移パターン A/B の「戻る」用） */
    record SearchCondition(String q, int page, int size) {
    }

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

    /**
     * 一覧（検索）。
     * restore=1 のとき（詳細画面からの「戻る」）はセッションの検索条件・ページングを復元する。
     */
    @GetMapping
    public String list(@ModelAttribute("searchForm") ProjectSearchForm searchForm,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(defaultValue = "false") boolean restore,
                       HttpSession session, Model model) {
        SearchCondition condition = new SearchCondition(searchForm.q(), page, size);
        if (restore) {
            Object saved = session.getAttribute(SESSION_SEARCH_CONDITION);
            if (saved instanceof SearchCondition savedCondition) {
                condition = savedCondition;
                model.addAttribute("searchForm", new ProjectSearchForm(condition.q()));
            }
        }
        session.setAttribute(SESSION_SEARCH_CONDITION, condition);

        ProjectListCommandInput input =
                new ProjectListCommandInput(condition.q(), condition.page(), condition.size());
        model.addAttribute("output", listCommand.execute(input));
        return "projects/list";
    }

    /** 登録フォーム */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ProjectForm(null));
        return "projects/new";
    }

    /**
     * 検索・登録画面（1 画面で検索・登録の 2 アクションを持つ。画面遷移パターン B・C）。
     * 検索実行はこの画面の検索フォームから GET /page/projects（一覧）へ遷移する（パターン B）。
     * 一覧からの「戻る」時はセッションの検索条件を復元し、検索項目が入力された状態で表示する。
     */
    @GetMapping("/entry")
    public String entryForm(HttpSession session, Model model) {
        String q = null;
        if (session.getAttribute(SESSION_SEARCH_CONDITION) instanceof SearchCondition saved) {
            q = saved.q();
        }
        model.addAttribute("searchForm", new ProjectSearchForm(q));
        model.addAttribute("form", new ProjectForm(null));
        return "projects/entry";
    }

    /**
     * 検索・登録画面からの登録実行（パターン C: 自画面に遷移してメッセージ表示）。
     * 検索用（ProjectSearchForm）と登録用（ProjectForm）でバリデーションは別物
     * （coding-rules.md §3）。
     */
    @PostMapping("/entry")
    public String entryCreate(@Valid @ModelAttribute("form") ProjectForm form,
                              BindingResult bindingResult, HttpSession session,
                              Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            restoreEntrySearchForm(session, model);
            return "projects/entry";
        }
        try {
            createCommand.execute(new ProjectCreateCommandInput(form.name()));
        } catch (BusinessException e) {
            // 業務例外: メッセージを設定して元の画面（自画面）を再描画
            model.addAttribute("errorMessage", resolve(e.getMessageId()));
            restoreEntrySearchForm(session, model);
            return "projects/entry";
        }
        ra.addFlashAttribute("message", resolve("MSG001"));
        // パターン C: 自画面へリダイレクトしてメッセージを表示（PRG）
        return "redirect:/page/projects/entry";
    }

    /** 登録エラーで再描画するとき、検索フォーム側の入力状態も復元する */
    private void restoreEntrySearchForm(HttpSession session, Model model) {
        String q = null;
        if (session.getAttribute(SESSION_SEARCH_CONDITION) instanceof SearchCondition saved) {
            q = saved.q();
        }
        model.addAttribute("searchForm", new ProjectSearchForm(q));
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
