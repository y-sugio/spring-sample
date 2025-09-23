package demo.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // JSPのindex.jspを使う場合は "index" を返す
        // ここでは案件一覧にリダイレクト
        return "redirect:/page/projects";
    }
}
