package com.formation.taskops.repository;

import com.formation.taskops.model.Task;
import com.formation.taskops.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de TRANCHE PERSISTANCE : Spring demarre JPA + une base H2 en memoire,
 * sans la couche web. Chaque test s'execute dans une transaction
 * automatiquement annulee a la fin.
 */
@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Test
    @DisplayName("findByStatus ne renvoie que les taches du statut demande")
    void findByStatus_filtreCorrectement() {
        // GIVEN : trois taches, deux TODO et une DONE
        Task a = new Task("A", null);
        Task b = new Task("B", null);
        Task c = new Task("C", null);
        c.setStatus(TaskStatus.DONE);
        repository.saveAll(List.of(a, b, c));

        // WHEN
        List<Task> todo = repository.findByStatus(TaskStatus.TODO);
        List<Task> done = repository.findByStatus(TaskStatus.DONE);

        // THEN
        assertThat(todo).hasSize(2).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("A", "B");
        assertThat(done).hasSize(1).extracting(Task::getTitle)
                .containsExactly("C");
    }

    @Test
    @DisplayName("save attribue un identifiant genere par la base")
    void save_genereUnIdentifiant() {
        Task enregistree = repository.save(new Task("Nouvelle", "description"));

        assertThat(enregistree.getId()).isNotNull().isPositive();
        assertThat(repository.findById(enregistree.getId())).isPresent();
    }
}
