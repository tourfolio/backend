package com.tourfolio.app.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    // tourfolio.kr 메인 접속 시 자동으로 스웨거 페이지로 이동
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}