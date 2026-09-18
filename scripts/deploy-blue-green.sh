#!/usr/bin/env bash
# Deploiement Blue/Green simule.
# BLUE = port 8081  GREEN = port 8082
# Le fichier .active contient le port qui recoit le trafic (le "routeur").
# Regle : on ne bascule QUE si le smoke test de la nouvelle instance passe.
# Usage : ./scripts/deploy-blue-green.sh <chemin-du-jar>
set -uo pipefail

JAR="${1:?Usage: $0 <chemin-du-jar>}"
FICHIER_ACTIF=".active"
PORT_BLUE=8081
PORT_GREEN=8082

# --- Determiner qui est actif et qui va recevoir la nouvelle version ---
PORT_ACTIF=$(cat "$FICHIER_ACTIF" 2>/dev/null || echo "$PORT_BLUE")
if [ "$PORT_ACTIF" = "$PORT_BLUE" ]; then
    PORT_CIBLE=$PORT_GREEN; NOM_ACTIF="BLUE"; NOM_CIBLE="GREEN"
else
    PORT_CIBLE=$PORT_BLUE; NOM_ACTIF="GREEN"; NOM_CIBLE="BLUE"
fi

echo "═══════════════════════════════════════════"
echo " Actif : ${NOM_ACTIF} (port ${PORT_ACTIF})"
echo " Cible : ${NOM_CIBLE} (port ${PORT_CIBLE})"
echo "═══════════════════════════════════════════"

# --- 1. Demarrer la nouvelle version A COTE de l'ancienne ---
echo "▶ Demarrage de ${NOM_CIBLE}..."
# On libere le port cible s'il traine une instance d'un essai precedent
# Sous Git Bash / Windows : pkill n'existe pas, on tente taskkill via patterns
if command -v pkill >/dev/null 2>&1; then
    pkill -f "server.port=${PORT_CIBLE}" 2>/dev/null || true
else
    # Fallback Windows : tuer tous les java qui matchent le jar
    taskkill //F //FI "IMAGENAME eq java.exe" //FI "WINDOWTITLE eq *taskops*" >/dev/null 2>&1 || true
fi
sleep 1

java -jar "$JAR" --server.port="${PORT_CIBLE}" > "/tmp/taskops-${PORT_CIBLE}.log" 2>&1 &
PID_CIBLE=$!

# --- 2. La PORTE : le smoke test decide ---
if ./scripts/smoke-test.sh "${PORT_CIBLE}"; then
    echo "▶ Bascule du trafic vers ${NOM_CIBLE}..."
    echo "${PORT_CIBLE}" > "$FICHIER_ACTIF"
    echo "✅ Deploiement reussi : le trafic va vers ${NOM_CIBLE} (${PORT_CIBLE})"
    echo "   ${NOM_ACTIF} (${PORT_ACTIF}) reste demarre : rollback possible en 1 seconde."
    exit 0
else
    # --- 3. ROLLBACK AUTOMATIQUE : on n'a jamais bascule, on nettoie ---
    echo "❌ Smoke test en echec sur ${NOM_CIBLE}."
    kill "$PID_CIBLE" 2>/dev/null || true
    echo "↩ Rollback : le trafic reste sur ${NOM_ACTIF} (${PORT_ACTIF})."
    echo "   AUCUN utilisateur n'a vu la version defectueuse."
    exit 1
fi
