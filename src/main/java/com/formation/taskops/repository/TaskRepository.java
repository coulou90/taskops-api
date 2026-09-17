package com.formation.taskops.repository;

import com.formation.taskops.model.Task;
import com.formation.taskops.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data genere AUTOMATIQUEMENT l'implementation de cette interface au demarrage.
 * En heritant de JpaRepository<Task, Long>, on obtient sans ecrire une ligne :
 * findAll(), findById(), save(), deleteById(), count()...
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Methode "derivee" : Spring Data lit le NOM de la methode et ecrit la requete
     * SQL correspondante (SELECT * FROM tasks WHERE status = ?).
     */
    List<Task> findByStatus(TaskStatus status);
}
