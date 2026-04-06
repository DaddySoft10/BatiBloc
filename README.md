# BatiBloc - TP4

## Description
BatiBloc est une application permettant d'importer un plan en format PDF,
de gérer différentes vues du plan et de simuler le placement de blocs
de construction sur des zones définies.

Cette version correspond au **livrable 4** du projet.

## Prérequis
- Java 25 ou plus récent
- Un fichier PDF contenant un plan (plusieurs pages possibles)

## Lancer l'application
1. Télécharger ou cloner le projet.
2. Se placer dans le dossier du projet.
3. Exécuter le fichier jar :
## Fonctionnalités disponibles

### 1. Importation d'un plan PDF
L'utilisateur peut importer un fichier PDF contenant un ou plusieurs plans.
Chaque page du PDF correspond à une vue indépendante.

### 2. Gestion des vues
- Navigation entre les différentes vues
- Suppression d'une vue
- Rognage d'une vue pour ne conserver qu'une partie spécifique
- Création d'une nouvelle vue à partir d'un rognage

### 3. Gestion des zones
- Création de zones (Rectangle, Triangle, Triangle tronqué)
- Types de zones : Armature en blocs, Armature classique, Ouverture
- Sélection d'une zone avec contour rouge et 8 poignées
- Déplacement d'une zone avec la souris
- Modification des dimensions via le panneau d'édition (format impérial)
- Suppression d'une zone (touche DELETE ou bouton)
- Chaque vue possède ses propres zones indépendantes

### 4. Mesures impériales
Les dimensions sont saisies et affichées en pouces impériaux.
Formats acceptés : `3' 6"`, `3'`, `6"`, `42` (pouces bruts)

### 5. Simulation du placement des blocs
- Lancement de la simulation sur toutes les zones de type Bloc
- Respect des contraintes de taille minimale (6 pouces)
- Affichage du nombre total de blocs dans l'interface
- Résultat affiché dans le panneau (sans popup)

### 6. Zoom
- Zoom avec la molette de la souris
- Le zoom est centré autour de la position de la souris
- Boutons Zoom+ et Zoom- disponibles dans la barre d'outils
- Bouton Recentrer pour réinitialiser la vue

## Structure du projet
Le projet suit une architecture MVC :
- **Domaine** : logique métier (zones, simulation, façades)
- **Vue** : interface graphique Swing (MainWindow, DrawingPanel)
- **DTO** : transfert de données entre domaine et vue

Le contrôleur :
- Ne retourne pas d'objets complexes du domaine
- Ne prend pas d'objets complexes du domaine comme paramètres
- Communique avec la vue via des DTOs

## Dépôt Git
Le projet est remis via le dépôt Git — branche `remise_4`.

## Équipe
Équipe 05 — Projet BatiBloc — Hiver 2026