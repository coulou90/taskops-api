package com.formation.taskops.service;

import com.formation.taskops.model.Task;
import com.formation.taskops.model.TaskStatus;
import com.formation.taskops.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    @DisplayName("create() enregistre la tache avec le statut TODO par defaut")
    void create_assigneStatutTodoParDefaut() {
        Task nouvelle = new Task("Ecrire les tests", "JUnit 5 + Mockito");
        when(repository.save(any(Task.class))).thenAnswer(appel -> appel.getArgument(0));

        Task resultat = service.create(nouvelle);

        assertThat(resultat.getTitle()).isEqualTo("Ecrire les tests");
        assertThat(resultat.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(resultat.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById() leve TaskNotFoundException si la tache n'existe pas")
    void findById_leveExceptionSiAbsente() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("countByStatus() renvoie un compteur pour chacun des trois statuts")
    void countByStatus_couvreTousLesStatuts() {
        when(repository.findByStatus(TaskStatus.TODO))
            .thenReturn(List.of(new Task("A", null), new Task("B", null)));
        when(repository.findByStatus(TaskStatus.IN_PROGRESS)).thenReturn(List.of());
        when(repository.findByStatus(TaskStatus.DONE))
            .thenReturn(List.of(new Task("C", null)));

        Map<TaskStatus, Long> compteurs = service.countByStatus();

        assertThat(compteurs)
            .containsEntry(TaskStatus.TODO, 2L)
            .containsEntry(TaskStatus.IN_PROGRESS, 0L)
            .containsEntry(TaskStatus.DONE, 1L);
    }

    @Test
    @DisplayName("delete() leve TaskNotFoundException quand l'identifiant n'existe pas")
    void delete_leveExceptionSiAbsente() {
        when(repository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("42");

        verify(repository, never()).deleteById(42L);
    }

    @Test
    @DisplayName("delete() supprime la tache quand elle existe")
    void delete_supprimeQuandExiste() {
        when(repository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(repository).deleteById(7L);
    }

    @Test
    @DisplayName("update() remplace le titre et la description")
    void update_remplaceLesChamps() {
        Task existante = new Task("Ancien titre", "Ancienne description");
        existante.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(existante));
        when(repository.save(any(Task.class))).thenAnswer(call -> call.getArgument(0));

        Task nouvellesDonnees = new Task("Nouveau titre", "Nouvelle description");
        Task resultat = service.update(5L, nouvellesDonnees);

        assertThat(resultat.getTitle()).isEqualTo("Nouveau titre");
        assertThat(resultat.getDescription()).isEqualTo("Nouvelle description");
    }
}
