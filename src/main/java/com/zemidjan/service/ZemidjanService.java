package com.zemidjan.service;

import com.zemidjan.model.Cotisation;
import com.zemidjan.model.Zemidjan;
import com.zemidjan.repository.CotisationRepository;
import com.zemidjan.repository.UserRepository;
import com.zemidjan.repository.ZemidjanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import com.zemidjan.model.Cotisation;
import com.zemidjan.model.User;
import com.zemidjan.repository.CotisationRepository;
import com.zemidjan.repository.UserRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ZemidjanService {

    @Autowired
    private ZemidjanRepository zemidjanRepository;

    @Autowired
private CotisationRepository cotisationRepository;

@Autowired
private UserRepository userRepository;

    public List<Zemidjan> getAllZemidjans() {
        return zemidjanRepository.findAll();
    }

    public Page<Zemidjan> getAllZemidjans(Pageable pageable) {
        return zemidjanRepository.findAll(pageable);
    }

    public Optional<Zemidjan> getZemidjanById(Long id) {
        return zemidjanRepository.findById(id);
    }

    public Optional<Zemidjan> getZemidjanByNumero(String numero) {
        return zemidjanRepository.findByNumeroImmatriculation(numero);
    }

    @Transactional
public Zemidjan createZemidjan(Zemidjan zemidjan) {
    // Générer un numéro d'immatriculation unique s'il n'est pas fourni
    if (zemidjan.getNumeroImmatriculation() == null || zemidjan.getNumeroImmatriculation().isEmpty()) {
        zemidjan.setNumeroImmatriculation(genererNumeroImmatriculation(zemidjan.getCommune()));
    }
    
    // Définir la date d'immatriculation à aujourd'hui si elle n'est pas fournie
    if (zemidjan.getDateImmatriculation() == null) {
        zemidjan.setDateImmatriculation(LocalDate.now());
    }
    
    // Définir que le Zemidjan est à jour de ses cotisations par défaut
    zemidjan.setCotisationAJour(true);
    
    // Définir la date de dernière cotisation à la date d'immatriculation
    zemidjan.setDateDerniereCotisation(zemidjan.getDateImmatriculation());
    
    // Enregistrer également une cotisation pour le mois en cours
    Zemidjan zemidjanEnregistre = zemidjanRepository.save(zemidjan);
    
    // Créer automatiquement une cotisation pour le mois en cours
    try {
        YearMonth currentMonth = YearMonth.now();
        String moisCourant = currentMonth.format(DateTimeFormatter.ofPattern("MMMM"));
        int anneeCourante = currentMonth.getYear();
        
        // Trouver un utilisateur administrateur pour l'enregistrement de la cotisation
        // Note: Il faut injecter UserRepository dans le service
        Optional<User> adminUser = userRepository.findByEmail("admin@zemidjan.com");
        if (adminUser.isPresent()) {
            Cotisation cotisation = new Cotisation();
            cotisation.setZemidjan(zemidjanEnregistre);
            cotisation.setMoisConcerne(moisCourant);
            cotisation.setAnneeConcerne(anneeCourante);
            cotisation.setMontant(1000); // Montant fixe de 1000 FCFA
            cotisation.setDatePaiement(LocalDate.now());
            cotisation.setEnregistrePar(adminUser.get());
            
            cotisationRepository.save(cotisation);
        }
    } catch (Exception e) {
        // Logger l'erreur mais continuer (l'essentiel est que le Zemidjan soit enregistré)
        System.err.println("Erreur lors de la création de la cotisation initiale: " + e.getMessage());
    }
    
    return zemidjanEnregistre;
}

    @Transactional
    public Zemidjan updateZemidjan(Zemidjan zemidjan) {
        return zemidjanRepository.save(zemidjan);
    }

    @Transactional
    public void deleteZemidjan(Long id) {
        zemidjanRepository.deleteById(id);
    }

    public List<Zemidjan> getZemidjansByCommune(String commune) {
        return zemidjanRepository.findByCommune(commune);
    }

    public Page<Zemidjan> getZemidjansByCommune(String commune, Pageable pageable) {
        return zemidjanRepository.findByCommune(commune, pageable);
    }

    public List<Zemidjan> getZemidjansNonAJour() {
        return zemidjanRepository.findAllWithCotisationNonAJour();
    }

    public List<Zemidjan> getZemidjansNonAJourParCommune(String commune) {
        return zemidjanRepository.findByCommuneAndCotisationNonAJour(commune);
    }

    public List<Zemidjan> searchZemidjans(String keyword) {
        return zemidjanRepository.searchZemidjans(keyword);
    }

    @Transactional
    public boolean verifierAuthenticitéNumero(String numero) {
        Optional<Zemidjan> zemidjan = zemidjanRepository.findByNumeroImmatriculation(numero);
        return zemidjan.isPresent();
    }

    @Transactional
    public void mettreAJourStatutCotisation(Long zemidjanId, boolean status) {
        Zemidjan zemidjan = zemidjanRepository.findById(zemidjanId)
                .orElseThrow(() -> new RuntimeException("Zemidjan non trouvé avec ID: " + zemidjanId));
        
        zemidjan.setCotisationAJour(status);
        if (status) {
            zemidjan.setDateDerniereCotisation(LocalDate.now());
        }
        
        zemidjanRepository.save(zemidjan);
    }

    /**
     * Génère un numéro d'immatriculation unique pour un Zemidjan en fonction de sa commune.
     * Format: [CODE_COMMUNE]-[ANNÉE]-[NUMÉRO_SÉQUENTIEL]-[LETTRE_CONTRÔLE]
     */
    private String genererNumeroImmatriculation(String commune) {
        String codeCommune = getCodeCommune(commune);
        String annee = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
        
        // Générer un numéro séquentiel aléatoire (dans un vrai système, ce serait incrémental)
        Random random = new Random();
        int numeroSequentiel = 10000 + random.nextInt(90000); // Numéro à 5 chiffres
        
        // Générer une lettre de contrôle aléatoire
        char lettreControle = (char) ('A' + random.nextInt(26));
        
        String numeroPropose = String.format("%s-%s-%d-%c", codeCommune, annee, numeroSequentiel, lettreControle);
        
        // Vérifier l'unicité du numéro généré
        while (zemidjanRepository.existsByNumeroImmatriculation(numeroPropose)) {
            // Si le numéro existe déjà, en générer un nouveau
            numeroSequentiel = 10000 + random.nextInt(90000);
            lettreControle = (char) ('A' + random.nextInt(26));
            numeroPropose = String.format("%s-%s-%d-%c", codeCommune, annee, numeroSequentiel, lettreControle);
        }
        
        return numeroPropose;
    }

    /**
     * Obtient le code de la commune (pour simplifier, on utilise les premières lettres)
     */
    private String getCodeCommune(String commune) {
        if (commune == null || commune.isEmpty()) {
            return "ZZZ"; // Code par défaut
        }
        
        // Simple exemple: prendre les 3 premières lettres en majuscule comme code
        return commune.substring(0, Math.min(3, commune.length())).toUpperCase();
    }
}