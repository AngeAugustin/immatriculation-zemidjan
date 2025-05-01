package com.zemidjan.repository;

import com.zemidjan.model.Zemidjan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ZemidjanRepository extends JpaRepository<Zemidjan, Long> {
    Optional<Zemidjan> findByNumeroImmatriculation(String numeroImmatriculation);
    
    Boolean existsByNumeroImmatriculation(String numeroImmatriculation);
    
    List<Zemidjan> findByCommune(String commune);
    
    Page<Zemidjan> findByCommune(String commune, Pageable pageable);
    
    @Query("SELECT z FROM Zemidjan z WHERE z.cotisationAJour = false")
    List<Zemidjan> findAllWithCotisationNonAJour();
    
    @Query("SELECT z FROM Zemidjan z WHERE z.commune = :commune AND z.cotisationAJour = false")
    List<Zemidjan> findByCommuneAndCotisationNonAJour(@Param("commune") String commune);
    
    @Query("SELECT z FROM Zemidjan z WHERE z.nom LIKE %:keyword% OR z.prenom LIKE %:keyword% OR z.numeroImmatriculation LIKE %:keyword%")
    List<Zemidjan> searchZemidjans(@Param("keyword") String keyword);
}