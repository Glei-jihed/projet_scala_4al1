# 📖 Guide d'utilisation - Simulateur de Panneau LED

## 🚀 Installation et Configuration

### Prérequis

- **Java 11+** installé sur votre système
- **Scala CLI** ou **SBT** pour la compilation et l'exécution

### Installation de Scala CLI (recommandé)

```bash
# Windows (avec Chocolatey)
choco install scala-cli

# Linux/macOS
curl -sSLf https://virtuslab.github.io/scala-cli-packages/scala-setup.sh | sh
```

## 🎮 Modes d'exécution

### Mode Interface Graphique (GUI) - Recommandé ⭐

Le mode le plus convivial avec interface visuelle interactive.

```bash
scala-cli run src/ --dep com.github.pathikrit::better-files:3.9.2 -- gui
```

**Fonctionnalités :**

- ✅ Sélection de fichier avec explorateur
- ✅ Visualisation en temps réel du panneau LED
- ✅ Affichage des couleurs réelles (Rouge, Vert, Bleu, Blanc, Noir)
- ✅ Statistiques détaillées
- ✅ Interface intuitive et moderne

**Utilisation :**

1. Cliquez sur **"Sélectionner fichier..."**
2. Choisissez votre fichier d'instructions `.txt`
3. Cliquez sur **"Exécuter simulation"**
4. Observez le résultat dans la grille LED et les statistiques
5. Utilisez **"Reset"** pour recommencer

### Mode Console

Pour une utilisation en ligne de commande.

```bash
# Avec fichier spécifique
scala-cli run src/ --dep com.github.pathikrit::better-files:3.9.2 -- console example_input.txt

# Avec fichier par défaut
scala-cli run src/ --dep com.github.pathikrit::better-files:3.9.2
```

### Mode Tiling (Pavage)

Pour calculer les possibilités de pavage.

```bash
scala-cli run src/ --dep com.github.pathikrit::better-files:3.9.2 -- tiling 4 4
```

## 📁 Format des fichiers d'instructions

### Structure du fichier

```
<largeur> x <hauteur>
<temps> <action> <position> <couleur>
<temps> <action> <position> <couleur>
...
```

### Exemple concret (`example_input.txt`)

```
6 x 5
0 + (2,1) rouge
1 + (3,2) vert
2 % (2,1) blanc
3 - (3,2)
4 + (1,1) bleu
```

### Actions disponibles

- **`+`** : Allumer une LED
- **`-`** : Éteindre une LED
- **`%`** : Basculer l'état d'une LED (allumée ↔ éteinte)

### Couleurs supportées

- `rouge` / `red`
- `vert` / `green`
- `bleu` / `blue`
- `blanc` / `white`
- `noir` / `black`

### Format des positions

- `(x,y)` où x et y sont des coordonnées entières
- Origine `(0,0)` en haut à gauche
- x augmente vers la droite, y augmente vers le bas

## 💡 Exemples d'utilisation

### Exemple 1 : Animation simple

```
3 x 3
0 + (1,1) rouge
1 + (0,1) vert
1 + (2,1) vert
2 + (1,0) bleu
2 + (1,2) bleu
3 % (1,1) blanc
```

### Exemple 2 : Motif complexe

```
5 x 5
0 + (2,2) blanc
1 + (1,2) rouge
1 + (3,2) rouge
2 + (2,1) vert
2 + (2,3) vert
3 + (1,1) bleu
3 + (3,1) bleu
3 + (1,3) bleu
3 + (3,3) bleu
```

## 🔧 Résolution de problèmes

### Erreurs communes

**"Fichier non trouvé"**

```bash
# Vérifiez le chemin du fichier
ls example_input.txt
# Utilisez le chemin absolu si nécessaire
scala-cli run src/ --dep com.github.pathikrit::better-files:3.9.2 -- console /chemin/absolu/vers/fichier.txt
```

**"Position invalide"**

- Vérifiez que les coordonnées sont dans les limites du panneau
- Rappelez-vous : (0,0) à (largeur-1, hauteur-1)

**"Impossible de changer couleur et intensité simultanément"**

- Utilisez `%` seulement pour basculer l'état
- Pour changer de couleur : éteignez d'abord avec `-`, puis rallumez avec `+`

### Problèmes de compilation

**"Not found: better"**

```bash
# Assurez-vous d'inclure la dépendance
scala-cli run src/ --dep com.github.pathikrit::better-files:3.9.2 -- gui
```

**Interface graphique ne s'affiche pas**

- Vérifiez que votre système supporte l'affichage graphique
- Utilisez le mode console en alternative

## 🎯 Conseils d'utilisation

### Performance

- Les gros panneaux (>50x50) peuvent être lents à afficher
- Utilisez le mode console pour de très gros panneaux
- L'interface graphique est optimale pour des panneaux ≤ 20x20

### Création de fichiers d'instructions

1. Commencez toujours par définir les dimensions
2. Organisez les instructions par temps croissant
3. Utilisez des commentaires pour documenter vos animations
4. Testez avec de petits panneaux d'abord

### Débogage

1. Utilisez le mode console pour voir les détails
2. Vérifiez les messages d'erreur dans l'interface graphique
3. Commencez par des instructions simples et ajoutez progressivement

## 📊 Interprétation des résultats

### Statistiques affichées

- **Instructions exécutées** : Nombre total d'instructions traitées
- **Panneau final** : Dimensions du panneau
- **LEDs allumées** : Nombre de LEDs actuellement allumées
- **LEDs éteintes** : Nombre de LEDs actuellement éteintes
- **Détail par couleur** : Répartition des LEDs allumées par couleur

### État du panneau

- **`.`** : LED éteinte (noire)
- **`R`** : LED rouge allumée
- **`G`** : LED verte allumée
- **`B`** : LED bleue allumée
- **`W`** : LED blanche allumée

## 🔄 Workflows recommandés

### Pour l'apprentissage

1. Commencez par l'interface graphique
2. Utilisez `example_input.txt` pour comprendre
3. Créez vos propres petits exemples
4. Expérimentez avec les couleurs et actions

### Pour le développement

1. Prototypez avec l'interface graphique
2. Validez avec le mode console
3. Automatisez avec des scripts
4. Testez la performance avec le mode tiling

### Pour la production

1. Utilisez le mode console pour l'intégration
2. Créez des tests automatisés
3. Documentez vos formats de fichiers
4. Surveillez les performances

- **Instructions temporisées** avec validation des conflits
- **Zones et positions** : single position ou zone rectangulaire
- **Résumé d'activité** : LEDs allumées, couleurs, temps cumulé

### Partie 2 : Calcul de pavage

- **Algorithme de pavage** avec blocs 2×1
- **Calcul des combinaisons** possibles pour panneau n×m
- **Optimisation** avec programmation dynamique

## 🏗️ Architecture du code

```
src/main/scala/fr/esgi/al/funprog/
├── model/
│   ├── Led.scala              # Modèles Color, Led, Position, Zone
│   ├── Instruction.scala      # Actions et instructions
│   └── Panel.scala            # Panneau LED et statistiques
├── parser/
│   └── InstructionParser.scala # Parsing fichiers d'instructions
├── simulator/
│   └── LedPanelSimulator.scala # Simulation du panneau
├── tiling/
│   └── TilingCalculator.scala  # Calcul combinaisons pavage
└── Main.scala                 # Point d'entrée principal
```

## 🚀 Utilisation

### Mode Simulation (Partie 1)

```bash
# Avec fichier spécifique
sbt "run /chemin/vers/fichier_instructions.txt"

# Avec fichier par défaut
sbt run

# Exemple
sbt "run example_input.txt"
```

### Mode Pavage (Partie 2)

```bash
sbt "run tiling <largeur> <hauteur>"

# Exemples
sbt "run tiling 4 4"    # Résultat: 36 combinaisons
sbt "run tiling 3 2"    # Résultat: 3 combinaisons
```

## 📋 Format du fichier d'instructions

```
5 x 5
1 | + | 1,0,0 | 0,0 - 1,0
1 | + | 0,1,0 | 1,1 - 2,1
2 | + | 0,0,1 | 2,2 - 3,4
3 | % | 1,1,1 | 4,4
```

**Format** : `temps | action | couleur | cible`

- **Temps** : instant d'exécution (entier ≥ 0)
- **Action** : `+` (increment), `-` (decrement), `%` (switch)
- **Couleur** : `r,g,b` avec r,g,b ∈ {0,1}
- **Cible** : `x,y` (position) ou `x1,y1 - x2,y2` (zone)

## ✅ Validation et contraintes

### Contraintes respectées

- ❌ **Pas de `return`**
- ❌ **Pas de `while`**
- ❌ **Pas de `null`**
- ❌ **Pas d'expressions régulières**
- ✅ **Tous les `if` sont exhaustifs** (avec `else`)
- ✅ **Pattern matching exhaustif**
- ❌ **Pas de mutabilité** (`var`, structures mutables)
- ✅ **Gestion d'erreurs avec `Try`** (pas de `throw`)
- ✅ **Effets de bord limités** aux frontières

### Validations métier

- **Couleurs valides** : seules les 5 couleurs autorisées
- **Positions valides** : dans les limites du panneau
- **Pas de conflits temporels** : une seule instruction par LED par instant
- **Changement de couleur** : seulement si LED éteinte
- **Intensité** : entre 0.0 et 1.0

## 🧪 Tests

```bash
# Lancer tous les tests
sbt test

# Tests par module
sbt "testOnly fr.esgi.al.funprog.model.*"
sbt "testOnly fr.esgi.al.funprog.parser.*"
sbt "testOnly fr.esgi.al.funprog.simulator.*"
sbt "testOnly fr.esgi.al.funprog.tiling.*"
```

### Couverture de tests

- **Modèles** : validation des couleurs, LEDs, positions, zones
- **Parser** : parsing complet, détection d'erreurs, conflits
- **Simulateur** : actions, zones, cas d'erreur
- **Pavage** : cas simples et complexes, symétrie

## 📊 Exemple de sortie

### Simulation

```
=== Simulateur de Panneau LED FunProg ===
Mode simulation - Fichier: example_input.txt

=== Résumé d'activité ===
- allumées: 15
- couleurs:
  - rouge: 1
  - vert: 4
  - bleu: 7
  - blanc: 3
- cumul: 74
```

### Pavage

```
=== Simulateur de Panneau LED FunProg ===
Mode pavage - Panneau: 4x4
Possibilités d'affichage pour le panneau 4 x 4: 36
```

## 🔧 Résolution de problèmes

### Erreur de compilation Java/SBT

Si vous rencontrez des erreurs Java :

1. Vérifiez Java 8+ : `java -version`
2. Vérifiez JAVA_HOME
3. Réinstallez SBT si nécessaire

### Erreurs de validation

- **"Position invalide"** : coordonnées hors panneau
- **"Conflits temporels"** : plusieurs instructions même LED même instant
- **"Couleur invalide"** : triplet RGB non autorisé
- **"Impossible de changer couleur"** : LED allumée, il faut l'éteindre d'abord

## 📚 Concepts Scala utilisés

- **Case classes** et **enums** (Scala 3)
- **Pattern matching** exhaustif
- **Immutabilité** complète
- **Gestion d'erreurs** avec `Try`/`Success`/`Failure`
- **Programmation fonctionnelle** pure
- **Higher-order functions** (`map`, `fold`, `filter`)
- **For-comprehensions** pour la composition
- **Collections immutables** (`List`, `Map`, `Set`)

## 🎓 Objectifs pédagogiques atteints

1. ✅ **Programmation fonctionnelle** sans mutabilité
2. ✅ **Gestion d'erreurs** fonctionnelle
3. ✅ **Architecture modulaire** et testable
4. ✅ **Validation métier** rigoureuse
5. ✅ **Tests unitaires** complets
6. ✅ **Algorithmes** (simulation, pavage)+
7. ✅ **Parsing** et manipulation de données
8. ✅ **Types sûrs** avec le système de types Scala
