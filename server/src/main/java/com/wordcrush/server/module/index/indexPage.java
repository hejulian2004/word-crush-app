package com.wordcrush.server.module.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class indexPage {

    @GetMapping("/")
    public String index() {
        return "redirect:/resume.html";
    }
}
