package com.zemidjan.controller;

import com.zemidjan.dto.ZemidjanDTO;
import com.zemidjan.model.Zemidjan;
import com.zemidjan.security.UserDetailsImpl;
import com.zemidjan.service.ZemidjanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/zemidjans")
public class ZemidjanController {

    @Autowired
    private ZemidjanService zemidjanService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR', 'ROLE_USER')")
    public String getAllZemidjans(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // Récupérer la commune de l'utilisateur connecté
        String commune = userDetails.getCommune();
        
        // Si l'utilisateur est admin, afficher tous les zémidjans, sinon filtrer par commune
        Page<Zemidjan> zemidjanPage;
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            zemidjanPage = zemidjanService.getAllZemidjans(PageRequest.of(page, size, Sort.by("dateImmatriculation").descending()));
        } else {
            zemidjanPage = zemidjanService.getZemidjansByCommune(commune, PageRequest.of(page, size, Sort.by("dateImmatriculation").descending()));
        }
        
        model.addAttribute("zemidjans", zemidjanPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", zemidjanPage.getTotalPages());
        model.addAttribute("totalItems", zemidjanPage.getTotalElements());
        
        return "pages/zemidjan/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String showCreateForm(Model model) {
        model.addAttribute("zemidjanDTO", new ZemidjanDTO());
        return "pages/zemidjan/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String createZemidjan(@Valid @ModelAttribute("zemidjanDTO") ZemidjanDTO zemidjanDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        if (result.hasErrors()) {
            return "pages/zemidjan/create";
        }
        
        Zemidjan zemidjan = new Zemidjan();
        zemidjan.setNom(zemidjanDTO.getNom());
        zemidjan.setPrenom(zemidjanDTO.getPrenom());
        zemidjan.setAdresse(zemidjanDTO.getAdresse());
        zemidjan.setTelephone(zemidjanDTO.getTelephone());
        zemidjan.setCommune(zemidjanDTO.getCommune());
        zemidjan.setNumeroIdentificationNational(zemidjanDTO.getNumeroIdentificationNational());
        zemidjan.setDateImmatriculation(LocalDate.now());
        
        Zemidjan savedZemidjan = zemidjanService.createZemidjan(zemidjan);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "Le Zémidjan a été enregistré avec succès avec le numéro d'immatriculation: " 
            + savedZemidjan.getNumeroImmatriculation());
        
        return "redirect:/zemidjans";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR', 'ROLE_USER')")
    public String getZemidjanDetails(@PathVariable Long id, Model model) {
        Optional<Zemidjan> zemidjanOpt = zemidjanService.getZemidjanById(id);
        
        if (zemidjanOpt.isPresent()) {
            model.addAttribute("zemidjan", zemidjanOpt.get());
            return "pages/zemidjan/details";
        } else {
            return "redirect:/zemidjans";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Zemidjan> zemidjanOpt = zemidjanService.getZemidjanById(id);
        
        if (zemidjanOpt.isPresent()) {
            Zemidjan zemidjan = zemidjanOpt.get();
            ZemidjanDTO zemidjanDTO = new ZemidjanDTO();
            
            zemidjanDTO.setId(zemidjan.getId());
            zemidjanDTO.setNumeroImmatriculation(zemidjan.getNumeroImmatriculation());
            zemidjanDTO.setNom(zemidjan.getNom());
            zemidjanDTO.setPrenom(zemidjan.getPrenom());
            zemidjanDTO.setAdresse(zemidjan.getAdresse());
            zemidjanDTO.setTelephone(zemidjan.getTelephone());
            zemidjanDTO.setCommune(zemidjan.getCommune());
            zemidjanDTO.setDateImmatriculation(zemidjan.getDateImmatriculation());
            zemidjanDTO.setCotisationAJour(zemidjan.isCotisationAJour());
            zemidjanDTO.setDateDerniereCotisation(zemidjan.getDateDerniereCotisation());
            zemidjanDTO.setNumeroIdentificationNational(zemidjan.getNumeroIdentificationNational());
            
            model.addAttribute("zemidjanDTO", zemidjanDTO);
            return "pages/zemidjan/edit";
        } else {
            return "redirect:/zemidjans";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String updateZemidjan(@PathVariable Long id,
                                @Valid @ModelAttribute("zemidjanDTO") ZemidjanDTO zemidjanDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "pages/zemidjan/edit";
        }
        
        Optional<Zemidjan> zemidjanOpt = zemidjanService.getZemidjanById(id);
        
        if (zemidjanOpt.isPresent()) {
            Zemidjan zemidjan = zemidjanOpt.get();
            
            zemidjan.setNom(zemidjanDTO.getNom());
            zemidjan.setPrenom(zemidjanDTO.getPrenom());
            zemidjan.setAdresse(zemidjanDTO.getAdresse());
            zemidjan.setTelephone(zemidjanDTO.getTelephone());
            zemidjan.setCommune(zemidjanDTO.getCommune());
            zemidjan.setNumeroIdentificationNational(zemidjanDTO.getNumeroIdentificationNational());
            
            zemidjanService.updateZemidjan(zemidjan);
            
            redirectAttributes.addFlashAttribute("successMessage", "Les informations du Zémidjan ont été mises à jour avec succès.");
        }
        
        return "redirect:/zemidjans/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteZemidjan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        zemidjanService.deleteZemidjan(id);
        redirectAttributes.addFlashAttribute("successMessage", "Le Zémidjan a été supprimé avec succès.");
        return "redirect:/zemidjans";
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR', 'ROLE_USER')")
    public String searchZemidjans(@RequestParam String keyword, Model model) {
        List<Zemidjan> zemidjans = zemidjanService.searchZemidjans(keyword);
        model.addAttribute("zemidjans", zemidjans);
        model.addAttribute("keyword", keyword);
        return "pages/zemidjan/search-results";
    }

    // Dans la classe ZemidjanController.java
@GetMapping("/non-a-jour")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
public String getZemidjansNonAJour(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
    try {
        List<Zemidjan> zemidjansNonAJour;
        
        // Si l'utilisateur est admin, afficher tous les zémidjans non à jour, sinon filtrer par commune
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            zemidjansNonAJour = zemidjanService.getZemidjansNonAJour();
        } else {
            zemidjansNonAJour = zemidjanService.getZemidjansNonAJourParCommune(userDetails.getCommune());
        }
        
        model.addAttribute("zemidjans", zemidjansNonAJour);
        return "pages/zemidjan/non-a-jour";
    } catch (Exception e) {
        // Journaliser l'erreur
        e.printStackTrace(); // À remplacer par un vrai logging dans un environnement de production
        
        // Ajouter un message d'erreur pour l'utilisateur
        model.addAttribute("errorMessage", "Une erreur est survenue lors de la récupération des Zémidjans en retard de paiement. Veuillez réessayer ultérieurement.");
        return "pages/zemidjan/list"; // Rediriger vers la liste générale des Zémidjans
    }
}
}