# BâtiBloc - Équipe 05

Application desktop Java (Swing) pour préparer et estimer des travaux de maçonnerie à partir de plans.

## Objectif

BâtiBloc permet de charger un plan PDF, de manipuler ses vues, puis de préparer une estimation liée aux zones de façade.

## Fonctionnalités réalisées

- Importer un fichier PDF contenant plusieurs pages.
- Afficher et naviguer entre les différentes vues/pages du PDF.
- Supprimer une ou plusieurs vues/pages importées.
- Rogner (crop) les vues pour conserver uniquement la zone utile.
- Créer des zones de travail (formes et types de zone).
- Simuler un placement de blocs et calculer une estimation de coût.

## Stack technique

- Java 17
- Maven
- Swing (interface graphique)

## Structure du projet

- `equipe05/src/main/java/domaine` : logique métier (bâtiment, façade, zones, contrôleur).
- `equipe05/src/main/java/vue` : interface utilisateur (fenêtre principale, panneau de dessin).
- `equipe05/src/main/java/vue/drawer` : rendu graphique des vues.

## Prérequis

- JDK 17 installé
- Maven installé

## Installation et lancement

Depuis la racine du dépôt :

```bash
cd equipe05
mvn clean package
mvn exec:java -Dexec.mainClass="vue.MainWindow"
```

Alternative :

```bash
cd equipe05
java -jar target/equipe05-1.0-SNAPSHOT.jar
```

## Flux d’utilisation

1. Importer un plan PDF multi-pages.
2. Parcourir les vues/pages disponibles.
3. Supprimer les vues non pertinentes.
4. Rogner les vues pour isoler les zones d’intérêt.
5. Créer/positionner les zones de façade.
6. Lancer le calcul d’estimation.

## Équipe

Projet réalisé par l’Équipe 05.
