package demo.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 共通エラー画面のコントローラ。
 * 業務時間外（BusinessHoursFilter からフォワード）と
 * セッションタイムアウト（SecurityConfig の invalidSessionUrl からリダイレクト）を表示する。
 */
@Controller
@RequestMapping("/error")
public class ErrorPageController {

    /** 業務時間外エラー画面（POST 中のフォワードもあるため全メソッドを受ける） */
    @RequestMapping("/business-hours")
    public String businessHours() {
        return "error/business-hours";
    }

    /** セッションタイムアウト画面 */
    @GetMapping("/session-timeout")
    public String sessionTimeout() {
        return "error/session-timeout";
    }
}
