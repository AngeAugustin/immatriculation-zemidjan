package com.zemidjan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZemidjanDTO {

    private Long id;

    private String numeroImmatriculation;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Format de téléphone invalide")
    private String telephone;

    @NotBlank(message = "La commune est obligatoire")
    private String commune;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateImmatriculation;

    private boolean cotisationAJour;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDerniereCotisation;

    @NotBlank(message = "Le numéro d'identification national est obligatoire")
    private String numeroIdentificationNational;
}