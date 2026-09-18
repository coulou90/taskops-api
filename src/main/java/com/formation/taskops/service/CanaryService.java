package com.formation.taskops.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Aiguillage canari applicatif.
 * Le pourcentage vient de la configuration (variable d'environnement
 * CANARY_POURCENTAGE) : on peut donc passer de 0 % a 100 % SANS redeployer
 * un nouvel artefact - seulement en redemarrant avec une autre valeur.
 */
@Service
public class CanaryService {

    private final int pourcentage;

    public CanaryService(@Value("${taskops.canary.pourcentage:0}") int pourcentage) {
        // Bornage defensif : une valeur de configuration erronee ne doit
        // jamais mettre l'application dans un etat incoherent.
        this.pourcentage = Math.max(0, Math.min(100, pourcentage));
    }

    /**
     * Renvoie true pour environ {pourcentage} % des appels.
     * A 0 : jamais. A 100 : toujours.
     */
    public boolean utiliserNouvelleVersion() {
        if (pourcentage <= 0) {
            return false;
        }
        if (pourcentage >= 100) {
            return true;
        }
        return ThreadLocalRandom.current().nextInt(100) < pourcentage;
    }

    public int getPourcentage() {
        return pourcentage;
    }
}
