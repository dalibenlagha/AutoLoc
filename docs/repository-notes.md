# Atelier 3 — Note sur la couche Repository

## Choix d'interface

| Interface | Étend | Justification |
|---|---|---|
| `IContratRepository` | `JpaRepository<Contrat, Long>` | CRUD complet, `findAll` renvoie une `List`, `saveAndFlush` disponible. Les suppressions unitaires (`deleteById`, `delete`) appliquent la cascade ALL + orphanRemoval vers les paiements. |
| `IPaiementRepository` | `JpaRepository<Paiement, Long>` | Sert à lire les paiements. Dans la composition Contrat → Paiement, le code métier crée et retire un paiement à travers son contrat. |
| `IAgenceRepository` | `JpaRepository<Agence, Long>` | CRUD complet avec des `List`, tri et pagination disponibles. |
| `IEmployeRepository` | `JpaRepository<Employe, Long>` | CRUD complet avec des `List`, tri et pagination disponibles. |
| `IVehiculeRepository` | `JpaRepository<Vehicule, Long>` | CRUD complet avec des `List` ; tri et pagination utiles pour le catalogue de véhicules. |
| `IEquipementRepository` | `JpaRepository<Equipement, Long>` | CRUD complet avec des `List`, tri et pagination disponibles. |
| `IClientRepository` | `JpaRepository<Client, Long>` | CRUD complet avec des `List`, tri et pagination disponibles. |
| `IReservationRepository` | `JpaRepository<Reservation, Long>` | CRUD complet avec des `List` ; tri et pagination utiles pour l'historique des réservations. |
| `IMaintenanceRepository` | `JpaRepository<Maintenance, Long>` | CRUD complet avec des `List`, tri et pagination disponibles. |

Au démarrage, les logs affichent `Found 9 JPA repository interfaces`. Aucune classe d'implémentation n'est écrite et
aucune annotation `@Repository` n'est nécessaire : Spring Data génère un proxy par interface.

## Anomalies SonarQube for IDE

| Anomalie SonarQube for IDE | Règle / explication | Correction apportée |
|---|---|---|
| `import jakarta.persistence.*;` dans les neuf entités du package `domain` | **S2208** — Wildcard imports should not be used : l'import générique masque les classes réellement utilisées et peut créer des conflits de noms. | Remplacement par des imports explicites (`Entity`, `Table`, `Id`, `Column`, `OneToMany`...) dans chaque entité. |
| Méthode `contextLoads()` vide dans `AutolocApiApplicationTests` | **S1186** — Methods should not be empty : une méthode vide ne dit pas si elle est inachevée ou volontairement vide. | La méthode vérifie maintenant le contexte (voir ligne suivante). |
| Test `contextLoads()` sans assertion | **S2699** — Tests should include assertions : un test sans assertion ne vérifie rien. | Ajout de `assertThat(context.getBeansOfType(JpaRepository.class)).hasSize(9)` : le test vérifie que les neuf repositories sont créés. Le contexte est injecté par constructeur. |
