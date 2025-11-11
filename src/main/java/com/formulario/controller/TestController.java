package com.formulario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {
    
    static {
        System.out.println("=== TEST CONTROLLER CARGADO ===");
    }
    
    @GetMapping("/ping")
    public String ping(Model model) {
        model.addAttribute("mensaje", "TEST PING - Funcionando correctamente");
        return "error";
    }
    
    @GetMapping("/simple")
    public String simple(Model model) {
        model.addAttribute("mensaje", "TEST SIMPLE - Funcionando correctamente");
        return "error";
    }
} 