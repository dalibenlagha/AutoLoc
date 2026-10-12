# Stratégie de fetch et de cascade — AutoLoc

Règles générales appliquées à tout le modèle :

- **Fetch** : toutes les associations sont en `LAZY`. Les `@ManyToOne` / `@OneToOne` (EAGER par défaut)
  sont forcés en `LAZY` pour éviter de charger toute la chaîne d'objets liés à chaque lecture ;
  les collections (`@OneToMany`, `@ManyToMany`) restent `LAZY` (défaut).
- **Cascade** : une cascade n'est posée que du côté parent, et seulement si le cycle de vie de l'enfant
  dépend de celui du parent. `REMOVE` (inclus dans `ALL`) est réservé aux vraies compositions.
- **Côté propriétaire** : toujours le côté `@ManyToOne` (ou le côté qui porte la FK pour `@OneToOne`) ;
  le côté inverse utilise `mappedBy`, il ne crée donc ni colonne ni table de jointure.
- **Lombok** : `@Getter` / `@Setter` ciblés, jamais `@Data`, pour éviter les boucles infinies
  `toString` / `hashCode` sur les associations bidirectionnelles.

| Association | Type | Côté propriétaire (FK générée) | Fetch | Cascade | Justification |
|---|---|---|---|---|---|
| Contrat → Paiement | `@OneToMany` / `@ManyToOne` | Paiement (`contrat_id_contrat`) | LAZY | ALL + orphanRemoval | Composition : un paiement n'existe que rattaché à son contrat. Supprimer le contrat supprime ses paiements ; retirer un paiement de la liste le supprime en base. |
| Agence → Vehicule | `@OneToMany` / `@ManyToOne` | Vehicule (`agence_id_agence`) | LAZY | Aucune | Un véhicule survit à son agence (il peut être réaffecté) : aucune propagation, surtout pas de suppression. |
| Agence → Employe | `@OneToMany` / `@ManyToOne` | Employe (`agence_id_agence`) | LAZY | Aucune | Un employé a son propre cycle de vie (mutation, départ) ; il est géré indépendamment de l'agence. |
| Vehicule ↔ Equipement | `@ManyToMany` | Vehicule (table `vehicule_equipement`) | LAZY | Aucune | Les équipements (GPS, siège bébé…) sont un catalogue partagé entre véhicules : supprimer un véhicule ne doit pas supprimer l'équipement. `Set` pour éviter les doublons et les suppressions/réinsertions massives d'Hibernate sur un `List`. |
| Client → Reservation | `@OneToMany` / `@ManyToOne` | Reservation (`client_id_client`) | LAZY | PERSIST | Enregistrer un nouveau client avec sa première réservation en une seule opération. Pas de REMOVE : l'historique des réservations est conservé. |
| Reservation → Vehicule | `@ManyToOne` (unidirectionnelle) | Reservation (`vehicule_id_vehicule`) | LAZY | Aucune | Le véhicule est une ressource indépendante ; inutile de naviguer de Vehicule vers toutes ses réservations dans le modèle. |
| Reservation ↔ Contrat | `@OneToOne` | Contrat (`reservation_id_reservation`, unique) | LAZY | ALL (côté Reservation) | Le contrat découle d'une réservation et n'a pas de sens sans elle : il suit son cycle de vie. |
| Vehicule → Maintenance | `@OneToMany` / `@ManyToOne` | Maintenance (`vehicule_id_vehicule`) | LAZY | PERSIST | Enregistrer un véhicule avec ses maintenances en une fois ; pas de REMOVE pour garder l'historique d'entretien. |

## Remarques

- **Nom des colonnes FK** : aucune `@JoinColumn` n'est utilisée ; Hibernate nomme les colonnes
  `<attribut>_<pk référencée>` (ex. `agence_id_agence`). La table de jointure est nommée explicitement
  via `@JoinTable(name = "vehicule_equipement")`, avec les colonnes `vehicules_id_vehicule` et
  `equipements_id_equipement`.
- **LAZY sur le côté inverse d'un `@OneToOne`** (`Reservation.contrat`) : Hibernate ne peut pas créer de proxy
  sans savoir si un contrat existe ; sans bytecode enhancement, ce côté est donc en pratique chargé
  immédiatement. Le côté propriétaire (`Contrat.reservation`) est, lui, réellement LAZY.
- **LazyInitializationException** : un accès à une association LAZY hors transaction lèvera cette exception ;
  elle sera traitée par des requêtes dédiées (`JOIN FETCH`, Atelier 7).
