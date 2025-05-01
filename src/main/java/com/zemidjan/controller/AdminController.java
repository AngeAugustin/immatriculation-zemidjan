package com.zemidjan.controller;

import com.zemidjan.dto.UserDTO;
import com.zemidjan.model.ERole;
import com.zemidjan.model.User;
import com.zemidjan.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "pages/admin/users";
    }

    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "pages/admin/user-create";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
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
            return "pages/admin/user-create";
        }

        // Créer le nouvel utilisateur
        User user = new User();
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // Le service encodera le mot de passe
        user.setCommune(userDTO.getCommune());
        user.setActive(userDTO.isActive());

        User savedUser = userService.createUser(user);
        userService.addRoleToUser(savedUser, ERole.ROLE_USER);

        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur créé avec succès.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDTO userDTO = new UserDTO();
            
            userDTO.setId(user.getId());
            userDTO.setNom(user.getNom());
            userDTO.setPrenom(user.getPrenom());
            userDTO.setEmail(user.getEmail());
            userDTO.setCommune(user.getCommune());
            userDTO.setActive(user.isActive());
            
            model.addAttribute("userDTO", userDTO);
            model.addAttribute("userId", id);
            return "pages/admin/user-edit";
        } else {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute("userDTO") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        
        Optional<User> userOpt = userService.getUserById(id);
        
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
            return "redirect:/admin/users";
        }
        
        User existingUser = userOpt.get();
        
        // Vérifier si l'email existe déjà pour un autre utilisateur
        Optional<User> userWithEmail = userService.getUserByEmail(userDTO.getEmail());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            result.rejectValue("email", "error.userDTO", "Cet email est déjà utilisé par un autre utilisateur");
        }
        
        if (result.hasErrors()) {
            return "pages/admin/user-edit";
        }
        
        // Mettre à jour les informations de l'utilisateur
        existingUser.setNom(userDTO.getNom());
        existingUser.setPrenom(userDTO.getPrenom());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setCommune(userDTO.getCommune());
        existingUser.setActive(userDTO.isActive());
        
        // Mettre à jour le mot de passe uniquement s'il est fourni
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.userDTO", "Les mots de passe ne correspondent pas");
                return "pages/admin/user-edit";
            }
            existingUser.setPassword(userDTO.getPassword()); // Le service encodera le mot de passe
        }
        
        userService.updateUser(existingUser);
        
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur mis à jour avec succès.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-role")
    public String toggleUserRole(@PathVariable Long id,
                               @RequestParam("role") String role,
                               RedirectAttributes redirectAttributes) {
        
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            try {
                ERole eRole = ERole.valueOf(role);
                userService.addRoleToUser(user, eRole);
                redirectAttributes.addFlashAttribute("successMessage", "Rôle ajouté avec succès.");
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Rôle invalide.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
        }
        
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur activé avec succès.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
        }
        
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur désactivé avec succès.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
        }
        
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur supprimé avec succès.");
        return "redirect:/admin/users";
    }
}