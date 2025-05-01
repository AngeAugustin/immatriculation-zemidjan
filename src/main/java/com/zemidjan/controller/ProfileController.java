package com.zemidjan.controller;

import com.zemidjan.model.User;
import com.zemidjan.security.UserDetailsImpl;
import com.zemidjan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Optional<User> userOpt = userService.getUserById(userDetails.getId());
        
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "pages/profile";
        } else {
            return "redirect:/dashboard";
        }
    }
}