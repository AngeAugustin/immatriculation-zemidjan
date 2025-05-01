package com.zemidjan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "zemidjans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zemidjan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String numeroImmatriculation;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @NotBlank
    @Column(nullable = false)
    private String prenom;

    @NotBlank
    @Column(nullable = false)
    private String adresse;

    @NotBlank
    @Column(nullable = false)
    private String telephone;

    @NotBlank
    @Column(nullable = false)
    private String commune;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateImmatriculation;

    @Column(nullable = false)
    private boolean cotisationAJour = false;

    @Column(nullable = false)
    private LocalDate dateDerniereCotisation;

    @Column(nullable = false)
    private String numeroIdentificationNational;
}