package ru.lessons.my.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RootController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
