# TPlanner - Training Planner

Une application Android pour planifier et suivre vos entraînements de musculation avec analyse de progression.

## Fonctionnalités

### ✅ Importation de programme CSV
- Importez votre programme d'entraînement au format CSV
- Format simple : Jour, Exercice, Séries, Reps, Poids, RPE, Catégorie, Commentaire
- Validation automatique des données importées

### ✅ Suivi de séance
- Interface épurée pour enregistrer chaque série
- Auto-remplissage avec les performances précédentes
- Suivi du poids, RPE, répétitions et temps de repos
- Comparaison automatique du volume avec la séance précédente
- Message de fin personnalisé avec statistiques

### ✅ Analyse de progression
- **Exercices** : Courbes de progression, PRs, estimation 1RM
- **Groupes musculaires** : Volume par catégorie
- **Statistiques** : Volume total, RPE moyen, fréquence hebdomadaire

### ✅ Export des données
- Exportez votre programme au format CSV
- Exportez tout l'historique de performances
- Conservez vos données en toute sécurité

### 🎨 Interface
- Thème sombre avec accents bleu acier
- Design minimaliste inspiré de Notion
- Navigation intuitive

## Format du fichier CSV

Le fichier CSV doit contenir les colonnes suivantes :

```csv
Jour,Exercice,Séries,Reps,Poids,RPE,Catégorie,Commentaire
Mercredi,Tirage vertical prise neutre,4,8-10,59,8,Dos,
Samedi,Développé décliné haltères,4,8-10,55,7,Pecs,Bas des pecs
```

### Colonnes
- **Jour** : Jour de la semaine pour cet exercice
- **Exercice** : Nom de l'exercice
- **Séries** : Nombre de séries
- **Reps** : Répétitions (peut être une fourchette comme "8-10")
- **Poids** : Poids cible en kg
- **RPE** : Rate of Perceived Exertion (échelle 1-10)
- **Catégorie** : Groupe musculaire (Dos, Pecs, Jambes, etc.)
- **Commentaire** : Notes optionnelles

Un fichier d'exemple `exemple_programme.csv` est fourni dans le projet.

## Technologies utilisées

- **Kotlin** - Langage de programmation
- **Jetpack Compose** - UI moderne et déclarative
- **Room Database** - Persistance locale des données
- **Apache Commons CSV** - Parsing CSV
- **Vico Charts** - Graphiques de progression
- **Material Design 3** - Design system

## Architecture

L'application suit une architecture MVVM (Model-View-ViewModel) :

```
app/
├── data/
│   ├── models (ProgramExercise, PerformanceRecord, SessionHistory)
│   ├── dao (Data Access Objects)
│   └── repository (WorkoutRepository)
├── ui/
│   ├── screens (HomeScreen, WorkoutScreen, ProgressScreen, etc.)
│   ├── viewmodel (ViewModels pour chaque écran)
│   └── theme (Configuration du thème)
└── utils/
    ├── CsvImporter
    ├── CsvExporter
    └── ProgressCalculator
```

## Calculs de progression

### Indice de surcharge progressive
```
Indice = (Volume semaine n / Volume semaine n-1) - 1
```

### Volume
```
Volume = Poids × Répétitions × Séries
```

### Estimation 1RM (Formule Epley)
```
1RM = Poids × (1 + Répétitions / 30)
```

### Détection de stagnation
L'application détecte une stagnation si la progression moyenne sur 3 séances consécutives est inférieure à 3%.

## Comment utiliser

1. **Importer un programme**
   - Préparez un fichier CSV avec votre programme
   - Appuyez sur le bouton "+" sur l'écran d'accueil
   - Sélectionnez votre fichier CSV
   - Vérifiez les exercices importés
   - Confirmez l'importation

2. **Suivre une séance**
   - Sur l'écran d'accueil, appuyez sur une séance
   - Pour chaque exercice :
     - Les valeurs de la dernière séance sont pré-remplies
     - Modifiez le poids, RPE, reps si nécessaire
     - Enregistrez le temps de repos (optionnel)
     - Appuyez sur "Valider série" après chaque série
   - Terminez la séance pour voir le résumé

3. **Analyser vos progrès**
   - Allez dans l'onglet "Progression"
   - Consultez les courbes par exercice
   - Visualisez le volume par groupe musculaire
   - Vérifiez vos statistiques globales

4. **Exporter vos données**
   - Sur l'écran d'accueil, cliquez sur "Exporter les données"
   - Choisissez entre programme ou historique
   - Sélectionnez l'emplacement de sauvegarde

## Développement

### Prérequis
- Android Studio Arctic Fox ou plus récent
- JDK 8 ou supérieur
- Android SDK 26 (Android 8.0) minimum

### Build
```bash
./gradlew build
```

### Tests
```bash
./gradlew test
```

## Licence

Ce projet est open source et disponible sous licence MIT.

## Auteur

Développé avec ❤️ pour tous les adeptes de la progression continue.
