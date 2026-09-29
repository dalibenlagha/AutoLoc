# AutoLoc — Plateforme de location de véhicules multi-agences

## Objectifs du projet
AutoLoc permet de gérer la location de véhicules à travers plusieurs agences :
gestion du parc automobile, réservations, contrats de location et suivi des agences.

## Stack technique
Java 17, Spring Boot, Spring Data JPA, MySQL, Maven, Lombok, Postman, Git.

## Acteurs et cas d'utilisation (v0)

### Client
- Rechercher un véhicule disponible (dates, agence, catégorie)
- Réserver / annuler une réservation
- Consulter l'historique de ses locations

### Agent d'agence
- Enregistrer la prise en charge et le retour d'un véhicule
- Créer et gérer les contrats de location
- Mettre à jour l'état d'un véhicule (disponible, loué, en maintenance)

### Responsable d'agence
- Gérer le parc de véhicules de son agence
- Gérer les agents de son agence

### Administrateur
- Gérer les agences
- Gérer les comptes utilisateurs