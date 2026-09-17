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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test UNITAIRE : aucun serveur, aucune base de donnees.
 * Le repository est remplace par un mock -> le test s'execute en millisecondes.
 * C'est ce qui permettra a la CI du Module 3 de rendre un verdict en moins d'une minute.
 */
@ExtendWith(MockitoExtension.class)  // active Mockito dans JUnit 5
class TaskServiceTest {

    @Mock
    private TaskRepository repository;   // faux repository, pilote par le test

    @InjectMocks
    private TaskService service;         // le service reel, avec le mock injecte

    @Test
    @DisplayName("create() enregistre la tache avec le statut TODO par defaut")
    void create_assigneStatutTodoParDefaut() {
        // GIVEN : le repository renvoie l'objet qu'on lui donne
        Task nouvelle = new Task("Ecrire les tests", "JUnit 5 + Mockito");
        when(repository.save(any(Task.class))).thenAnswer(appel -> appel.getArgument(0));

        // WHEN
        Task resultat = service.create(nouvelle);

        // THEN
        assertThat(resultat.getTitle()).isEqualTo("Ecrire les tests");
        assertThat(resultat.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(resultat.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById() leve TaskNotFoundException si la tache n'existe pas")
    void findById_leveExceptionSiAbsente() {
        // GIVEN : la base ne contient rien pour l'id 42
        when(repository.findById(42L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("42");
    }
}
