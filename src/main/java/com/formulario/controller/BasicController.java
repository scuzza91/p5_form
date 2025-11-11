package com.formulario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasicController {
    
    static {
        System.out.println("=== BASIC CONTROLLER CARGADO ===");
    }
    
    @GetMapping("/basic")
    public String basic(Model model) {
        System.out.println("=== BASIC ENDPOINT ACCEDIDO ===");
        model.addAttribute("mensaje", "Basic endpoint funcionando correctamente");
        return "error";
    }
    
    @GetMapping("/hello")
    public String hello(Model model) {
        System.out.println("=== HELLO ENDPOINT ACCEDIDO ===");
        model.addAttribute("mensaje", "Hello World - Spring Boot funcionando");
        return "error";
    }
} 