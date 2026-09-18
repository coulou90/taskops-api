#!/usr/bin/env bash
# scripts/router.sh — envoie une requete a l'instance actuellement active.
PORT=$(cat .active 2>/dev/null || echo 8081)
echo "→ routage vers le port ${PORT}"
curl -s "http://localhost:${PORT}/actuator/info" | python -m json.tool
