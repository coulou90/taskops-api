package com.formation.taskops.service;

/**
 * Exception metier levee quand une tache demandee n'existe pas.
 * Une exception dediee (plutot qu'un retour null) rend l'intention explicite
 * et permet de la traduire en code HTTP 404 a un seul endroit.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Tache introuvable : " + id);
    }
}
