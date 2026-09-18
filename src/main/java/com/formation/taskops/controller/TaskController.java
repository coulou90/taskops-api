package com.formation.taskops.controller;

import com.formation.taskops.model.Task;
import com.formation.taskops.model.TaskStatus;
import com.formation.taskops.service.CanaryService;
import com.formation.taskops.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Couche d'exposition HTTP. Elle ne contient AUCUNE regle metier :
 * elle traduit du HTTP en appels de service, et des objets Java en JSON.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;
    private final CanaryService canary;

    public TaskController(TaskService service, CanaryService canary) {
        this.service = service;
        this.canary = canary;
    }

    /** GET /api/tasks/stats -> repartition des taches par statut */
    @GetMapping("/stats")
    public Map<TaskStatus, Long> stats() {
        return service.countByStatus();
    }

    /**
     * GET /api/tasks/tri
     * Deux implementations du tri cohabitent dans le MEME artefact.
     * Le canari decide, appel par appel, laquelle sert la reponse.
     */
    @GetMapping("/tri")
    public Map<String, Object> trier() {
        boolean nouvelle = canary.utiliserNouvelleVersion();
        List<Task> taches = service.findAll();

        List<Task> triees = nouvelle
                // v2 : tri par statut puis par titre
                ? taches.stream()
                        .sorted(Comparator.comparing(Task::getStatus)
                                .thenComparing(Task::getTitle))
                        .toList()
                // v1 : tri historique, par identifiant
                : taches.stream()
                        .sorted(Comparator.comparing(Task::getId))
                        .toList();

        return Map.of(
                "implementation", nouvelle ? "v2-tri-par-statut" : "v1-tri-par-id",
                "canaryPourcentage", canary.getPourcentage(),
                "taches", triees);
    }

    /** GET /api/tasks           -> toutes les taches
     *  GET /api/tasks?status=TODO -> filtrees par statut */
    @GetMapping
    public List<Task> list(@RequestParam(required = false) TaskStatus status) {
        return (status == null) ? service.findAll() : service.findByStatus(status);
    }

    /** GET /api/tasks/{id} -> 200 + la tache, ou 404 si absente */
    @GetMapping("/{id}")
    public Task getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    /** POST /api/tasks -> 201 Created + en-tete Location. */
    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody Task task) {
        Task created = service.create(task);
        return ResponseEntity
                .created(URI.create("/api/tasks/" + created.getId()))
                .body(created);
    }

    /** PUT /api/tasks/{id} -> 200 + la tache mise a jour */
    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @Valid @RequestBody Task task) {
        return service.update(id, task);
    }

    /** DELETE /api/tasks/{id} -> 204 No Content */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
