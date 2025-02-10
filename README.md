# Projet Java - Application Desktop en JavaFX : URCAR

## Réalisé par :  
- **Nom :** BOUDINAR  
- **Prénom :** LOUNES  
- **Classe :** Master 1 MIAGE - FA, Groupe 01  

## Instructions pour compiler et exécuter le projet

1. Avant toute chose, modifiez les informations de connexion à la base de données dans les fichiers `hibernate.cfg.xml` et `persistence.xml` situés dans le dossier `ressources`.  
   - Assurez-vous de remplacer les identifiants (login et mot de passe) par ceux qui vous permettront de vous connecter à votre base de données MySQL (via MySQL Workbench par exemple).

2. Une fois les informations de connexion mises à jour, ouvrez un terminal et exécutez les commandes suivantes pour compiler et exécuter le projet :  
   ```bash
   .\gradlew clean build run
