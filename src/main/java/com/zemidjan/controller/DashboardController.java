package com.zemidjan.controller;

import com.zemidjan.model.Cotisation;
import com.zemidjan.model.Zemidjan;
import com.zemidjan.security.UserDetailsImpl;
import com.zemidjan.service.CotisationService;
import com.zemidjan.service.ZemidjanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
public class DashboardController {
    
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private ZemidjanService zemidjanService;

    @Autowired
    private CotisationService cotisationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // Récupérer la commune de l'utilisateur connecté
            String commune = userDetails.getCommune();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            // 1. Récupération des statistiques globales
            List<Zemidjan> allZemidjans;
            if (isAdmin) {
                // Admin voit tous les Zémidjans
                allZemidjans = zemidjanService.getAllZemidjans();
            } else {
                // Les autres ne voient que les Zémidjans de leur commune
                allZemidjans = zemidjanService.getZemidjansByCommune(commune);
            }
            
            int totalZemidjans = allZemidjans.size();
            
            // 2. Comptage des Zémidjans à jour et non à jour
            long zemidjanAJour = allZemidjans.stream()
                    .filter(Zemidjan::isCotisationAJour)
                    .count();
            long zemidjanNonAJour = totalZemidjans - zemidjanAJour;
            
            // 3. Calcul du taux de recouvrement
            double tauxRecouvrement = totalZemidjans > 0 
                    ? (double) zemidjanAJour / totalZemidjans * 100 
                    : 0;
            
            // 4. Calcul du montant total collecté pour le mois courant
            YearMonth currentMonth = YearMonth.now();
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();
            
            List<Cotisation> cotisationsMois;
            if (isAdmin) {
                cotisationsMois = cotisationService.getCotisationsPeriode(startOfMonth, endOfMonth);
            } else {
                cotisationsMois = cotisationService.getCotisationsPeriodeParCommune(startOfMonth, endOfMonth, commune);
            }
            
            int montantMensuel = cotisationsMois.stream()
                    .mapToInt(Cotisation::getMontant)
                    .sum();
            
            // Ajouter les données au modèle pour affichage
            model.addAttribute("totalZemidjans", totalZemidjans);
            model.addAttribute("zemidjanAJour", zemidjanAJour);
            model.addAttribute("zemidjanNonAJour", zemidjanNonAJour);
            model.addAttribute("tauxRecouvrement", Math.round(tauxRecouvrement));
            model.addAttribute("montantMensuel", montantMensuel);
            
        } catch (Exception e) {
            // Journaliser l'erreur
            log.error("Erreur lors de la récupération des données pour le dashboard", e);
            
            // Ajouter un message d'erreur au modèle
            model.addAttribute("errorMessage", "Une erreur est survenue lors du chargement des données. Veuillez réessayer.");
            
            // Valeurs par défaut
            model.addAttribute("totalZemidjans", 0);
            model.addAttribute("zemidjanAJour", 0);
            model.addAttribute("zemidjanNonAJour", 0);
            model.addAttribute("tauxRecouvrement", 0);
            model.addAttribute("montantMensuel", 0);
        }
        
        return "pages/dashboard";
    }
}