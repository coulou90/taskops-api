package com.formation.taskops.model;

/**
 * Les etats possibles d'une tache.
 * Un enum plutot qu'une String : le compilateur garantit
 * qu'aucune valeur invalide ne peut entrer dans le systeme.
 */
public enum TaskStatus {
    TODO,        // a faire
    IN_PROGRESS, // en cours
    DONE         // terminee
}
