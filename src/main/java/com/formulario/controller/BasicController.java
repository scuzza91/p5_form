package com.formulario.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasicController {

    private static final Logger logger = LoggerFactory.getLogger(BasicController.class);

    @GetMapping("/basic")
    public String basic(Model model) {
        logger.debug("=== BASIC ENDPOINT ACCEDIDO ===");
        model.addAttribute("mensaje", "Basic endpoint funcionando correctamente");
        return "error";
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        logger.debug("=== HELLO ENDPOINT ACCEDIDO ===");
        model.addAttribute("mensaje", "Hello World - Spring Boot funcionando");
        return "error";
    }
} 