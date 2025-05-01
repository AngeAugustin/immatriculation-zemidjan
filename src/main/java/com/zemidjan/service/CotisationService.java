package com.zemidjan.service;

import com.zemidjan.model.Cotisation;
import com.zemidjan.model.User;
import com.zemidjan.model.Zemidjan;
import com.zemidjan.repository.CotisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CotisationService {

    @Autowired
    private CotisationRepository cotisationRepository;

    @Autowired
    private ZemidjanService zemidjanService;

    public List<Cotisation> getAllCotisations() {
        return cotisationRepository.findAll();
    }

    public Optional<Cotisation> getCotisationById(Long id) {
        return cotisationRepository.findById(id);
    }

    public List<Cotisation> getCotisationsParZemidjan(Zemidjan zemidjan) {
        return cotisationRepository.findByZemidjanOrderByDatePaiementDesc(zemidjan);
    }

    @Transactional
    public Cotisation enregistrerCotisation(Zemidjan zemidjan, String moisConcerne, Integer anneeConcerne, User enregistrePar) {
        // Vérifier si une cotisation pour ce mois existe déjà
        List<Cotisation> cotisationsExistantes = cotisationRepository.findByZemidjanAndPeriode(zemidjan, moisConcerne, anneeConcerne);
        
        if (!cotisationsExistantes.isEmpty()) {
            throw new RuntimeException("Une cotisation existe déjà pour cette période");
        }
        
        Cotisation cotisation = new Cotisation();
        cotisation.setZemidjan(zemidjan);
        cotisation.setMoisConcerne(moisConcerne);
        cotisation.setAnneeConcerne(anneeConcerne);
        cotisation.setMontant(1000); // Montant fixe de 1000 FCFA
        cotisation.setDatePaiement(LocalDate.now());
        cotisation.setEnregistrePar(enregistrePar);
        
        Cotisation cotisationEnregistree = cotisationRepository.save(cotisation);
        
        // Vérifier si le Zemidjan est à jour de ses cotisations
        verifierEtMettreAJourStatutCotisation(zemidjan);
        
        return cotisationEnregistree;
    }

    @Transactional
    public void supprimerCotisation(Long id) {
        Optional<Cotisation> cotisationOpt = cotisationRepository.findById(id);
        
        if (cotisationOpt.isPresent()) {
            Cotisation cotisation = cotisationOpt.get();
            Zemidjan zemidjan = cotisation.getZemidjan();
            
            // Supprimer la cotisation
            cotisationRepository.deleteById(id);
            
            // Mettre à jour le statut de cotisation du Zemidjan
            verifierEtMettreAJourStatutCotisation(zemidjan);
        } else {
            throw new RuntimeException("Cotisation non trouvée avec ID: " + id);
        }
    }

    /**
     * Vérifie si un Zemidjan est à jour de ses cotisations et met à jour son statut
     */
    @Transactional
    public void verifierEtMettreAJourStatutCotisation(Zemidjan zemidjan) {
    LocalDate today = LocalDate.now();
    YearMonth currentMonth = YearMonth.from(today);
    
    // Vérifier si le Zemidjan a été enregistré ce mois-ci
    boolean enregistreCeMois = zemidjan.getDateImmatriculation().getMonth() == today.getMonth() 
                             && zemidjan.getDateImmatriculation().getYear() == today.getYear();
    
    // Si le Zemidjan a été enregistré ce mois-ci, il est automatiquement à jour
    if (enregistreCeMois) {
        zemidjan.setCotisationAJour(true);
        zemidjanService.updateZemidjan(zemidjan);
        return;
    }
    
    // Vérifier si le Zemidjan a été enregistré le mois dernier et si on est avant le 10 du mois
    YearMonth dernierMois = currentMonth.minusMonths(1);
    boolean enregistreMoisDernier = zemidjan.getDateImmatriculation().getMonth() == dernierMois.getMonth() 
                                 && zemidjan.getDateImmatriculation().getYear() == dernierMois.getYear();
    boolean avantLe10 = today.getDayOfMonth() < 10;
    
    // Si on est dans le mois suivant l'immatriculation et avant le 10, le Zemidjan est toujours à jour
    if (enregistreMoisDernier && avantLe10) {
        zemidjan.setCotisationAJour(true);
        zemidjanService.updateZemidjan(zemidjan);
        return;
    }
    
    // Cas standard: vérifier si la cotisation du mois courant a été payée
    String moisCourant = currentMonth.format(DateTimeFormatter.ofPattern("MMMM"));
    int anneeCourante = currentMonth.getYear();
    
    List<Cotisation> cotisationsRecentes = cotisationRepository.findByZemidjanAndPeriode(
            zemidjan, moisCourant, anneeCourante);
    
    // Si cotisation du mois courant payée, le Zemidjan est à jour
    boolean cotisationMoisCourantPayee = !cotisationsRecentes.isEmpty();
    
    // Période de grâce: Si on est avant le 10 du mois, vérifier aussi le mois précédent
    if (!cotisationMoisCourantPayee && avantLe10) {
        String moisPrecedent = currentMonth.minusMonths(1).format(DateTimeFormatter.ofPattern("MMMM"));
        int anneePrecedente = currentMonth.minusMonths(1).getYear();
        
        List<Cotisation> cotisationsMoisPrecedent = cotisationRepository.findByZemidjanAndPeriode(
                zemidjan, moisPrecedent, anneePrecedente);
        
        boolean cotisationMoisPrecedentPayee = !cotisationsMoisPrecedent.isEmpty();
        
        // À jour si cotisation du mois précédent payée pendant la période de grâce
        zemidjan.setCotisationAJour(cotisationMoisPrecedentPayee);
    } else {
        // Après le 10 du mois, seule la cotisation du mois courant compte
        zemidjan.setCotisationAJour(cotisationMoisCourantPayee);
    }
    
    // Mettre à jour la date de dernière cotisation si à jour
    if (zemidjan.isCotisationAJour() && !cotisationsRecentes.isEmpty()) {
        zemidjan.setDateDerniereCotisation(cotisationsRecentes.get(0).getDatePaiement());
    }
    
    zemidjanService.updateZemidjan(zemidjan);
}

    /**
     * Vérifie si un Zemidjan est à jour de ses cotisations sans mettre à jour son statut
     */
    public boolean estAJourDeCotisation(Zemidjan zemidjan) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        
        String moisCourant = currentMonth.format(DateTimeFormatter.ofPattern("MMMM"));
        int anneeCourante = currentMonth.getYear();
        
        List<Cotisation> cotisationsRecentes = cotisationRepository.findByZemidjanAndPeriode(
                zemidjan, moisCourant, anneeCourante);
        
        return !cotisationsRecentes.isEmpty();
    }

    /**
     * Retourne le nombre de cotisations payées dans l'année pour un Zemidjan donné
     */
    public int getNombreCotisationsAnnee(Zemidjan zemidjan, int annee) {
        return cotisationRepository.countCotisationsByZemidjanAndAnnee(zemidjan, annee);
    }

    /**
     * Obtient les cotisations pour une période donnée
     */
    public List<Cotisation> getCotisationsPeriode(LocalDate debut, LocalDate fin) {
        return cotisationRepository.findByPeriode(debut, fin);
    }

    /**
     * Récupère les cotisations d'une période et commune
     */
    public List<Cotisation> getCotisationsPeriodeParCommune(LocalDate debut, LocalDate fin, String commune) {
        return cotisationRepository.findByPeriodeAndCommune(debut, fin, commune);
    }

    
}