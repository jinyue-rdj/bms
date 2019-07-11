package com.bc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexMainPage {
    @RequestMapping("/")
    public String IndexMainPage() throws Exception{
        return "redirect:/book/list";
    }
}
