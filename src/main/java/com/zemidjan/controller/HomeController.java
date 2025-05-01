package com.zemidjan.controller;

import com.zemidjan.service.ZemidjanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private ZemidjanService zemidjanService;

    @GetMapping("/")
    public String home() {
        return "pages/index";
    }

    @GetMapping("/verifier-numero")
    public String showVerificationPage() {
        return "pages/verifier-numero";
    }

    @PostMapping("/verifier-numero")
    public String verifierNumero(@RequestParam("numero") String numero, 
                                Model model,
                                RedirectAttributes redirectAttributes) {
        boolean estAuthentique = zemidjanService.verifierAuthenticitéNumero(numero);
        
        if (estAuthentique) {
            zemidjanService.getZemidjanByNumero(numero).ifPresent(z -> {
                model.addAttribute("zemidjan", z);
                model.addAttribute("estAuthentique", true);
            });
        } else {
            model.addAttribute("estAuthentique", false);
            model.addAttribute("message", "Le numéro d'immatriculation n'est pas authentique ou n'existe pas.");
        }
        
        model.addAttribute("numero", numero);
        return "pages/verifier-numero";
    }

    

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "pages/error/access-denied";
    }
}