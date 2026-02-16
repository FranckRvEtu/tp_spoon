# Scanner - Analyseur de métriques Java

Ce projet est une application Spring Boot utilisant la librairie Spoon pour analyser le code source de projets Java. Il permet de calculer diverses métriques logicielles, de générer des graphes d'appel et de couplage, et d'effectuer un clustering hiérarchique.

## Prérequis

* Java JDK 21
* Maven (ou utiliser le wrapper fourni)
* Un navigateur web récent

## Installation

1.  Clonez ce dépôt.
2.  Placez-vous dans le dossier du projet (là où se trouve le fichier `pom.xml`) :
    ```bash
    cd scanner
    ```
3.  Compilez le projet à l'aide du wrapper Maven :
    * Sous Linux/macOS :
        ```bash
        ./mvnw clean install
        ```
    * Sous Windows :
        ```cmd
        mvnw.cmd clean install
        ```

## Lancement de l'application

Vous pouvez démarrer l'application directement avec le plugin Spring Boot via Maven :

* Sous Linux/macOS :
    ```bash
    ./mvnw spring-boot:run
    ```
* Sous Windows :
    ```cmd
    mvnw.cmd spring-boot:run
    ```

Une fois l'application démarrée, le serveur est accessible par défaut sur le port 8080.

## Utilisation

1.  Ouvrez votre navigateur et accédez à l'adresse : `http://localhost:8080`
2.  Remplissez le formulaire avec les informations suivantes :
    * **Chemin du projet** : Le chemin absolu vers la racine du projet Java à analyser sur votre machine locale.
    * **Valeur de X** : Un nombre entier (utilisé pour filtrer les classes ayant plus de X méthodes).
    * **Paramètre CP** : Une valeur décimale (ex: 0.1) servant de seuil pour l'identification des modules dans le clustering.
3.  Cliquez sur "Lancer l'analyse".

Les résultats (métriques, matrices, graphes et modules) s'afficheront sur la même page.
