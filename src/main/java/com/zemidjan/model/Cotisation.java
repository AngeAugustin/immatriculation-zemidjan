package com.zemidjan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "cotisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cotisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zemidjan_id", nullable = false)
    private Zemidjan zemidjan;

    @NotNull
    @Column(nullable = false)
    private LocalDate datePaiement;

    @NotNull
    @Column(nullable = false)
    private Integer montant = 1000; // Montant par défaut: 1000 FCFA

    @Column(nullable = false)
    private String moisConcerne;

    @Column(nullable = false)
    private Integer anneeConcerne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enregistre_par", nullable = false)
    private User enregistrePar;
}