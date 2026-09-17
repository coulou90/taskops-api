package com.formation.taskops.service;

import com.formation.taskops.model.Task;
import com.formation.taskops.model.TaskStatus;
import com.formation.taskops.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Couche metier. Elle isole les regles de gestion du controleur (HTTP)
 * et du repository (base de donnees).
 * Interet DevOps : cette couche est testable SANS demarrer de serveur
 * ni de base -> les tests sont rapides, donc la CI est rapide (Module 3).
 */
@Service
public class TaskService {

    private final TaskRepository repository;

    /**
     * Injection par constructeur : la dependance est obligatoire, immuable,
     * et remplacable par un mock dans les tests. A preferer a @Autowired sur un champ.
     */
    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Compte les tâches par statut.
     * Renvoie une Map ordonnée : TODO, IN_PROGRESS, DONE.
     */
    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> countByStatus() {
        Map<TaskStatus, Long> resultat = new EnumMap<>(TaskStatus.class);
        for (TaskStatus statut : TaskStatus.values()) {
            resultat.put(statut, (long) repository.findByStatus(statut).size());
        }
        return resultat;
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> findByStatus(TaskStatus status) {
        return repository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public Task create(Task task) {
        task.setId(null); // securite : on ne laisse pas le client imposer l'identifiant
        return repository.save(task);
    }

    @Transactional
    public Task update(Long id, Task data) {
        Task existing = findById(id);          // leve 404 si absente
        existing.setTitle(data.getTitle());
        existing.setDescription(data.getDescription());
        if (data.getStatus() != null) {
            existing.setStatus(data.getStatus());
        }
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        repository.deleteById(id);
    }
}