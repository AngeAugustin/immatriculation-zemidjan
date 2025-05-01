package com.zemidjan.controller;

import com.zemidjan.dto.UserDTO;
import com.zemidjan.model.ERole;
import com.zemidjan.model.User;
import com.zemidjan.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "pages/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "pages/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // Vérifier si l'email existe déjà
        if (userService.existsByEmail(userDTO.getEmail())) {
            result.rejectValue("email", "error.userDTO", "Cet email est déjà utilisé");
        }

        // Vérifier si les mots de passe correspondent
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.userDTO", "Les mots de passe ne correspondent pas");
        }

        if (result.hasErrors()) {
            return "pages/register";
        }

        // Créer le nouvel utilisateur
        User user = new User();
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // Le service encodera le mot de passe
        user.setCommune(userDTO.getCommune());
        user.setActive(true);

        userService.createUser(user);
        userService.addRoleToUser(user, ERole.ROLE_USER);

        redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie! Vous pouvez maintenant vous connecter.");
        return "redirect:/login";
    }
}