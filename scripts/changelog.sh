#!/usr/bin/env bash
# Genere un extrait de CHANGELOG a partir des commits conventionnels
# situes entre deux references Git.
# Usage : ./scripts/changelog.sh <tag-depart> <tag-arrivee>
set -euo pipefail

DEPUIS="${1:?Usage: $0 <tag-depart> <tag-arrivee>}"
JUSQUA="${2:-HEAD}"

# %s = sujet du commit (la premiere ligne)
COMMITS=$(git log --no-merges --pretty=format:'%s' "${DEPUIS}..${JUSQUA}")

# Petite fonction : filtre les commits d'un type et les met en forme
section() {
    local type="$1" titre="$2"
    local lignes
    lignes=$(echo "$COMMITS" | grep -E "^${type}(\(.+\))?!?: " || true)
    if [ -n "$lignes" ]; then
        echo "### ${titre}"
        echo ""
        # On retire le prefixe de type pour ne garder que la description
        echo "$lignes" | sed -E "s/^${type}(\(.+\))?!?: /- /"
        echo ""
    fi
}

echo "## ${JUSQUA} — $(date +%Y-%m-%d)"
echo ""
section "feat" "✨ Nouvelles fonctionnalités"
section "fix" "🐛 Corrections"
section "perf" "⚡ Performances"
section "refactor" "♻️ Refactorisations"
section "docs" "📚 Documentation"
section "ci" "⚙️ Intégration continue"
section "build" "📦 Build et dépendances"
