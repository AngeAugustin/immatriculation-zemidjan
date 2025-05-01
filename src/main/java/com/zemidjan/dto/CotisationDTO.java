package com.zemidjan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CotisationDTO {

    private Long id;

    @NotNull(message = "L'ID du Zemidjan est obligatoire")
    private Long zemidjanId;

    private String numeroImmatriculation;

    private String nomCompletZemidjan;

    @NotBlank(message = "Le mois concerné est obligatoire")
    private String moisConcerne;

    @NotNull(message = "L'année concernée est obligatoire")
    @Min(value = 2000, message = "L'année doit être valide")
    private Integer anneeConcerne;

    @NotNull(message = "Le montant est obligatoire")
    @Min(value = 1000, message = "Le montant minimum est de 1000 FCFA")
    private Integer montant = 1000;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate datePaiement;

    private String enregistreParUtilisateur;
}