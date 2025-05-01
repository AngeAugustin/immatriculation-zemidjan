package com.zemidjan.repository;

import com.zemidjan.model.Cotisation;
import com.zemidjan.model.Zemidjan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, Long> {
    List<Cotisation> findByZemidjanOrderByDatePaiementDesc(Zemidjan zemidjan);
    
    @Query("SELECT c FROM Cotisation c WHERE c.zemidjan = :zemidjan AND c.moisConcerne = :mois AND c.anneeConcerne = :annee")
    List<Cotisation> findByZemidjanAndPeriode(
        @Param("zemidjan") Zemidjan zemidjan, 
        @Param("mois") String mois, 
        @Param("annee") Integer annee
    );
    
    @Query("SELECT c FROM Cotisation c WHERE c.datePaiement BETWEEN :debut AND :fin")
    List<Cotisation> findByPeriode(
        @Param("debut") LocalDate debut, 
        @Param("fin") LocalDate fin
    );
    
    @Query("SELECT COUNT(c) FROM Cotisation c WHERE c.zemidjan = :zemidjan AND c.anneeConcerne = :annee")
    Integer countCotisationsByZemidjanAndAnnee(
        @Param("zemidjan") Zemidjan zemidjan, 
        @Param("annee") Integer annee
    );

    @Query("SELECT c FROM Cotisation c WHERE c.datePaiement BETWEEN :debut AND :fin AND c.zemidjan.commune = :commune")
List<Cotisation> findByPeriodeAndCommune(
    @Param("debut") LocalDate debut, 
    @Param("fin") LocalDate fin,
    @Param("commune") String commune
);
}