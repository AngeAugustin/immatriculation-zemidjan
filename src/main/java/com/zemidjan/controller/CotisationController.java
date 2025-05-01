package com.zemidjan.controller;

import com.zemidjan.dto.CotisationDTO;
import com.zemidjan.model.Cotisation;
import com.zemidjan.model.User;
import com.zemidjan.model.Zemidjan;
import com.zemidjan.security.UserDetailsImpl;
import com.zemidjan.service.CotisationService;
import com.zemidjan.service.UserService;
import com.zemidjan.service.ZemidjanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.zemidjan.security.UserDetailsImpl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/cotisations")
public class CotisationController {

    @Autowired
    private CotisationService cotisationService;

    @Autowired
    private ZemidjanService zemidjanService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String getAllCotisations(Model model) {
        try {
            List<Cotisation> cotisations = cotisationService.getAllCotisations();
            model.addAttribute("cotisations", cotisations);
            return "pages/cotisation/list";
        } catch (Exception e) {
            // Log l'exception
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur est survenue lors de la récupération des cotisations: " + e.getMessage());
            // Retourner une vue par défaut en cas d'erreur
            return "pages/error/general-error";
        }
    }

    @GetMapping("/zemidjan/{zemidjanId}")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR', 'ROLE_USER')")
public String getCotisationsParZemidjan(@PathVariable Long zemidjanId, Model model) {
    Optional<Zemidjan> zemidjanOpt = zemidjanService.getZemidjanById(zemidjanId);
    
    if (zemidjanOpt.isPresent()) {
        Zemidjan zemidjan = zemidjanOpt.get();
        List<Cotisation> cotisations = cotisationService.getCotisationsParZemidjan(zemidjan);
        
        // Récupérer les données pour le résumé annuel
        Map<Integer, Map<String, Object>> resumeAnnuel = new LinkedHashMap<>();
        
        // Année courante
        int currentYear = LocalDate.now().getYear();
        
        // Récupérer les données pour les années (au moins l'année courante et précédente)
        for (int year = currentYear; year >= currentYear - 1; year--) {
            int nombreMoisPayes = cotisationService.getNombreCotisationsAnnee(zemidjan, year);
            int montantTotal = nombreMoisPayes * 1000; // 1000 FCFA par mois
            int couverture = (nombreMoisPayes * 100) / (year == currentYear ? LocalDate.now().getMonthValue() : 12);
            
            Map<String, Object> anneeData = new HashMap<>();
            anneeData.put("cotisationsPayees", nombreMoisPayes + " mois");
            anneeData.put("total", montantTotal + " FCFA");
            anneeData.put("couverture", couverture);
            
            resumeAnnuel.put(year, anneeData);
        }
        
        // Récupérer les statuts des cotisations pour chaque mois de l'année en cours
        Map<String, String> statutsCotisations = new LinkedHashMap<>();
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        
        int currentMonth = LocalDate.now().getMonthValue();
        
        for (int i = 0; i < mois.length; i++) {
            int monthNumber = i + 1; // Mois commencent à 1 (janvier)
            
            // Vérifier si la cotisation a été payée pour ce mois
            boolean cotisationPayee = false;
            for (Cotisation cotisation : cotisations) {
                YearMonth cotisationMonth = YearMonth.of(
                    cotisation.getAnneeConcerne(),
                    getMonthNumber(cotisation.getMoisConcerne())
                );
                
                if (cotisationMonth.getYear() == currentYear && cotisationMonth.getMonthValue() == monthNumber) {
                    cotisationPayee = true;
                    break;
                }
            }
            
            // Déterminer le statut pour ce mois
            String statut;
            if (monthNumber < currentMonth) {
                // Mois passés
                statut = cotisationPayee ? "payé" : "retard";
            } else if (monthNumber == currentMonth) {
                // Mois courant
                statut = cotisationPayee ? "payé" : "retard";
            } else {
                // Mois futurs
                statut = "avenir";
            }
            
            statutsCotisations.put(mois[i], statut);
        }
        
        model.addAttribute("zemidjan", zemidjan);
        model.addAttribute("cotisations", cotisations);
        model.addAttribute("resumeAnnuel", resumeAnnuel);
        model.addAttribute("statutsCotisations", statutsCotisations);
        model.addAttribute("anneeActuelle", currentYear);
        
        return "pages/cotisation/list-par-zemidjan";
    } else {
        return "redirect:/zemidjans";
    }
}

// Méthode utilitaire pour convertir le nom du mois en numéro
private int getMonthNumber(String moisNom) {
    switch (moisNom.toLowerCase()) {
        case "janvier": return 1;
        case "février": return 2;
        case "mars": return 3;
        case "avril": return 4;
        case "mai": return 5;
        case "juin": return 6;
        case "juillet": return 7;
        case "août": return 8;
        case "septembre": return 9;
        case "octobre": return 10;
        case "novembre": return 11;
        case "décembre": return 12;
        default: return 0;
    }
}

    @GetMapping("/zemidjan/{zemidjanId}/create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String showCreateForm(@PathVariable Long zemidjanId, Model model) {
        Optional<Zemidjan> zemidjanOpt = zemidjanService.getZemidjanById(zemidjanId);
        
        if (zemidjanOpt.isPresent()) {
            Zemidjan zemidjan = zemidjanOpt.get();
            
            CotisationDTO cotisationDTO = new CotisationDTO();
            cotisationDTO.setZemidjanId(zemidjan.getId());
            cotisationDTO.setNumeroImmatriculation(zemidjan.getNumeroImmatriculation());
            cotisationDTO.setNomCompletZemidjan(zemidjan.getPrenom() + " " + zemidjan.getNom());
            
            // Mois et année courants
            YearMonth currentMonth = YearMonth.now();
            cotisationDTO.setMoisConcerne(currentMonth.format(DateTimeFormatter.ofPattern("MMMM")));
            cotisationDTO.setAnneeConcerne(currentMonth.getYear());
            
            // Montant par défaut
            cotisationDTO.setMontant(1000);
            
            model.addAttribute("cotisationDTO", cotisationDTO);
            model.addAttribute("zemidjan", zemidjan);
            
            return "pages/cotisation/create";
        } else {
            return "redirect:/zemidjans";
        }
    }

    @PostMapping("/zemidjan/{zemidjanId}/create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String createCotisation(@PathVariable Long zemidjanId,
                                 @Valid @ModelAttribute("cotisationDTO") CotisationDTO cotisationDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        if (result.hasErrors()) {
            return "pages/cotisation/create";
        }
        
        Optional<Zemidjan> zemidjanOpt = zemidjanService.getZemidjanById(zemidjanId);
        Optional<User> userOpt = userService.getUserById(userDetails.getId());
        
        if (zemidjanOpt.isPresent() && userOpt.isPresent()) {
            Zemidjan zemidjan = zemidjanOpt.get();
            User user = userOpt.get();
            
            try {
                cotisationService.enregistrerCotisation(zemidjan, 
                                                    cotisationDTO.getMoisConcerne(), 
                                                    cotisationDTO.getAnneeConcerne(), 
                                                    user);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Cotisation enregistrée avec succès pour " + zemidjan.getPrenom() + " " + zemidjan.getNom());
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Zemidjan ou utilisateur non trouvé.");
        }
        
        return "redirect:/cotisations/zemidjan/" + zemidjanId;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteCotisation(@PathVariable Long id, 
                                 @RequestParam Long zemidjanId,
                                 RedirectAttributes redirectAttributes) {
        try {
            cotisationService.supprimerCotisation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cotisation supprimée avec succès.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/cotisations/zemidjan/" + zemidjanId;
    }

    @GetMapping("/rapport")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
    public String showRapportForm() {
        return "pages/cotisation/rapport-form";
    }

    @GetMapping("/rapport/generer")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATEUR')")
public String genererRapport(
    @RequestParam("dateDebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
    @RequestParam("dateFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
    @RequestParam(value = "parCommune", required = false) Boolean parCommune,
    Model model,
    @AuthenticationPrincipal UserDetailsImpl userDetails) {
    
    // Récupérer la commune de l'utilisateur et vérifier s'il est admin
    String commune = userDetails.getCommune();
    boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    
    // Récupérer toutes les cotisations de la période
    List<Cotisation> cotisations;
    if (isAdmin) {
        cotisations = cotisationService.getCotisationsPeriode(dateDebut, dateFin);
    } else {
        cotisations = cotisationService.getCotisationsPeriodeParCommune(dateDebut, dateFin, commune);
    }
    
    // Statistiques de base
    int totalCotisations = cotisations.size();
    int montantTotal = cotisations.stream().mapToInt(Cotisation::getMontant).sum();
    
    // 1. Répartition par commune
    Map<String, List<Cotisation>> cotisationsParCommune = cotisations.stream()
            .collect(Collectors.groupingBy(c -> c.getZemidjan().getCommune()));
    
    List<Map<String, Object>> statsParCommune = new ArrayList<>();
    
    cotisationsParCommune.forEach((nomCommune, cotisationsCommune) -> {
        int nombreCotisationsCommune = cotisationsCommune.size();
        int montantTotalCommune = cotisationsCommune.stream()
                .mapToInt(Cotisation::getMontant).sum();
        double pourcentage = (double) nombreCotisationsCommune / totalCotisations * 100;
        
        Map<String, Object> communeStats = new HashMap<>();
        communeStats.put("commune", nomCommune);
        communeStats.put("nombreCotisations", nombreCotisationsCommune);
        communeStats.put("montantTotal", montantTotalCommune);
        communeStats.put("pourcentage", Math.round(pourcentage));
        
        statsParCommune.add(communeStats);
    });
    
    // Trier par nombre de cotisations décroissant
    statsParCommune.sort((a, b) -> 
        ((Integer) b.get("nombreCotisations")).compareTo((Integer) a.get("nombreCotisations")));
    
    // 2. Répartition par jour
    Map<LocalDate, List<Cotisation>> cotisationsParJour = cotisations.stream()
            .collect(Collectors.groupingBy(Cotisation::getDatePaiement));
    
    List<Map<String, Object>> statsParJour = new ArrayList<>();
    
    cotisationsParJour.forEach((date, cotisationsJour) -> {
        int nombreCotisationsJour = cotisationsJour.size();
        int montantTotalJour = cotisationsJour.stream()
                .mapToInt(Cotisation::getMontant).sum();
        
        Map<String, Object> jourStats = new HashMap<>();
        jourStats.put("date", date);
        jourStats.put("nombreCotisations", nombreCotisationsJour);
        jourStats.put("montantTotal", montantTotalJour);
        
        statsParJour.add(jourStats);
    });
    
    // Trier par date croissante
    statsParJour.sort((a, b) -> 
        ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));
    
    // Trouver le jour avec le plus de cotisations
    Map<String, Object> jourMax = statsParJour.stream()
            .max((a, b) -> ((Integer) a.get("nombreCotisations")).compareTo((Integer) b.get("nombreCotisations")))
            .orElse(null);
    
    // Ajouter toutes les données au modèle
    model.addAttribute("cotisations", cotisations);
    model.addAttribute("dateDebut", dateDebut);
    model.addAttribute("dateFin", dateFin);
    model.addAttribute("totalCotisations", totalCotisations);
    model.addAttribute("montantTotal", montantTotal);
    model.addAttribute("statsParCommune", statsParCommune);
    model.addAttribute("statsParJour", statsParJour);
    model.addAttribute("jourMax", jourMax);
    
    return "pages/cotisation/rapport-resultats";
}
}