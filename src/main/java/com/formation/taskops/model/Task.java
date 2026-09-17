package com.formation.taskops.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Entite JPA representant une tache.
 * "Entite" = une classe Java dont chaque instance correspond a une ligne en base.
 */
@Entity                    // Hibernate cree une table a partir de cette classe
@Table(name = "tasks")     // Nom explicite de la table (sinon : "task")
public class Task {

    @Id                                                  // Cle primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Valeur generee par la base
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")       // Refuse null, "" et "   "
    @Size(max = 120, message = "Le titre ne peut depasser 120 caracteres")
    private String title;

    @Size(max = 1000, message = "La description ne peut depasser 1000 caracteres")
    private String description;

    @Enumerated(EnumType.STRING)  // Stocke "TODO" et non 0 : lisible et resistant aux refactorings
    private TaskStatus status = TaskStatus.TODO;

    private Instant createdAt = Instant.now();

    /**
     * Constructeur sans argument : EXIGE par JPA pour instancier l'objet
     * lors de la lecture en base. Ne pas le supprimer.
     */
    public Task() {
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // --- Getters / Setters : Jackson (JSON) et Hibernate en ont besoin ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
