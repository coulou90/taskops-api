package com.formation.taskops.controller;

import tools.jackson.databind.json.JsonMapper;
import com.formation.taskops.model.Task;
import com.formation.taskops.model.TaskStatus;
import com.formation.taskops.service.TaskNotFoundException;
import com.formation.taskops.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private TaskService service;

    @Test
    @DisplayName("GET /api/tasks renvoie 200 et la liste au format JSON")
    void list_renvoie200EtLeJson() throws Exception {
        Task tache = new Task("Mettre en place la CI", "GitHub Actions");
        tache.setId(1L);
        when(service.findAll()).thenReturn(List.of(tache));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Mettre en place la CI"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} renvoie 404 quand la tache est absente")
    void getOne_renvoie404SiAbsente() throws Exception {
        when(service.findById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Tache introuvable : 99"));
    }

    @Test
    @DisplayName("POST /api/tasks renvoie 201 et l'en-tete Location")
    void create_renvoie201() throws Exception {
        Task envoyee = new Task("Ecrire un test", null);
        Task creee = new Task("Ecrire un test", null);
        creee.setId(7L);
        when(service.create(any(Task.class))).thenReturn(creee);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(envoyee)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("POST /api/tasks renvoie 400 quand le titre est vide")
    void create_renvoie400SiTitreVide() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    @DisplayName("GET /api/tasks?status=DONE delegue au filtrage par statut")
    void list_filtreParStatut() throws Exception {
        when(service.findByStatus(TaskStatus.DONE)).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} renvoie 204 quand la tache existe")
    void delete_renvoie204() throws Exception {
        mockMvc.perform(delete("/api/tasks/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} renvoie 404 quand la tache est absente")
    void delete_renvoie404SiAbsente() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
