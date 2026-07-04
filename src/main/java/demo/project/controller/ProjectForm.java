package demo.project.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 案件登録フォーム。
 * 単項目チェックは Bean Validation アノテーションで行い、
 * message 属性にはメッセージ ID を指定する（coding-rules.md §3, §6）。
 */
public record ProjectForm(
        @NotBlank(message = "{VAL001}")
        @Size(max = 100, message = "{VAL002}")
        String name
) {
}
