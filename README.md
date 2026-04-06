# BatiBloc - TP4

## Description
BatiBloc est une application permettant d'importer un plan en format PDF et de gerer differentes vues du plan.

Cette version correspond au **livrable 4** du projet. Elle permet au client de tester certaines fonctionnalites principales de l'application.

## Prerequis
- Java 21 ou plus recent
- Un fichier PDF contenant un plan (plusieurs pages possibles)

## Lancer l'application

1. Telecharger ou cloner le projet.
2. Se placer dans le dossier du projet.
3. Executer le fichier jar :

```bash
java -jar equipe05.jar
```

## Fonctionnalites disponibles

### 1. Importation d'un plan PDF
L'utilisateur peut importer un fichier PDF contenant un ou plusieurs plans.

Etapes :
1. Ouvrir l'application.
2. Cliquer sur **Importer un PDF**.
3. Selectionner un fichier PDF sur votre ordinateur.

### 2. Visualisation des vues
Apres l'importation du PDF, l'utilisateur peut naviguer entre les differentes vues correspondant aux pages du plan.

### 3. Suppression d'une vue
L'utilisateur peut supprimer une ou plusieurs vues du plan importe.

### 4. Rognage d'une vue
Il est possible de rogner une vue pour ne conserver qu'une partie specifique du plan.

## Structure du projet
Le projet utilise une architecture avec un **controleur** qui gere les interactions entre la vue et la logique de l'application.

Le controleur :
- ne retourne pas d'objets complexes du domaine
- ne prend pas d'objets complexes du domaine comme parametres
- communique avec la vue via des donnees simples (DTO si utilises)

## Depot Git
Le projet est remis via le depot Git.

Pour la remise :
1. Placer le fichier `equipe05.jar` a la racine du projet.
2. Verifier que la branche `main` contient tout le code.
3. Creer la branche de remise :

```bash
git checkout -b remise_3
git push origin remise_3
```

## Equipe
Equipe 05 - Projet BatiBloc
