# Atelier 5 — Note sur l'API REST

Préfixe commun : `server.servlet.context-path=/api` (les contrôleurs n'écrivent pas `/api`).
Documentation : `http://localhost:8081/api/swagger-ui.html` et `http://localhost:8081/api/v3/api-docs`.
Collection Postman : `docs/postman/AutoLoc.postman_collection.json` (environnement `local`, variable `baseUrl`).

## A. Endpoints

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| `ContratController` | `GET /api/contrats` | `findAll()` | 200 | — |
| `ContratController` | `GET /api/contrats/{id}` | `findById(id)` | 200 | 404 |
| `ContratController` | `POST /api/contrats` | `create(contrat)` | 201 | 400 |
| `ContratController` | `PUT /api/contrats/{id}` | `update(id, contrat)` | 200 | 404, 400 |
| `ContratController` | `DELETE /api/contrats/{id}` | `deleteById(id)` | 204 | 404 |
| `PaiementController` | `GET /api/paiements` | `findAll()` | 200 | — |
| `PaiementController` | `GET /api/paiements/{id}` | `findById(id)` | 200 | 404 |
| `PaiementController` | `POST /api/paiements` (non exposé) | — | — | 405 |
| `AgenceController` | `GET /api/agences` | `findAll()` | 200 | — |
| `AgenceController` | `GET /api/agences/{id}` | `findById(id)` | 200 | 404 |
| `AgenceController` | `POST /api/agences` | `create(agence)` | 201 | 400 |
| `AgenceController` | `PUT /api/agences/{id}` | `update(id, agence)` | 200 | 404, 400 |
| `AgenceController` | `DELETE /api/agences/{id}` | `deleteById(id)` | 204 | 404 |
| `EmployeController` | `GET /api/employes` | `findAll()` | 200 | — |
| `EmployeController` | `GET /api/employes/{id}` | `findById(id)` | 200 | 404 |
| `EmployeController` | `POST /api/employes` | `create(employe)` | 201 | 400 |
| `EmployeController` | `PUT /api/employes/{id}` | `update(id, employe)` | 200 | 404, 400 |
| `EmployeController` | `DELETE /api/employes/{id}` | `deleteById(id)` | 204 | 404 |
| `VehiculeController` | `GET /api/vehicules` | `findAll()` | 200 | — |
| `VehiculeController` | `GET /api/vehicules/{id}` | `findById(id)` | 200 | 404 |
| `VehiculeController` | `POST /api/vehicules` | `create(vehicule)` | 201 | 400 |
| `VehiculeController` | `PUT /api/vehicules/{id}` | `update(id, vehicule)` | 200 | 404, 400 |
| `VehiculeController` | `DELETE /api/vehicules/{id}` | `deleteById(id)` | 204 | 404 |
| `EquipementController` | `GET /api/equipements` | `findAll()` | 200 | — |
| `EquipementController` | `GET /api/equipements/{id}` | `findById(id)` | 200 | 404 |
| `EquipementController` | `POST /api/equipements` | `create(equipement)` | 201 | 400 |
| `EquipementController` | `PUT /api/equipements/{id}` | `update(id, equipement)` | 200 | 404, 400 |
| `EquipementController` | `DELETE /api/equipements/{id}` | `deleteById(id)` | 204 | 404 |
| `ClientController` | `GET /api/clients` | `findAll()` | 200 | — |
| `ClientController` | `GET /api/clients/{id}` | `findById(id)` | 200 | 404 |
| `ClientController` | `POST /api/clients` | `create(client)` | 201 | 400 |
| `ClientController` | `PUT /api/clients/{id}` | `update(id, client)` | 200 | 404, 400 |
| `ClientController` | `DELETE /api/clients/{id}` | `deleteById(id)` | 204 | 404 |
| `ReservationController` | `GET /api/reservations` | `findAll()` | 200 | — |
| `ReservationController` | `GET /api/reservations/{id}` | `findById(id)` | 200 | 404 |
| `ReservationController` | `POST /api/reservations` | `create(reservation)` | 201 | 400 |
| `ReservationController` | `PUT /api/reservations/{id}` | `update(id, reservation)` | 200 | 404, 400 |
| `ReservationController` | `DELETE /api/reservations/{id}` | `deleteById(id)` | 204 | 404 |
| `MaintenanceController` | `GET /api/maintenances` | `findAll()` | 200 | — |
| `MaintenanceController` | `GET /api/maintenances/{id}` | `findById(id)` | 200 | 404 |
| `MaintenanceController` | `POST /api/maintenances` | `create(maintenance)` | 201 | 400 |
| `MaintenanceController` | `PUT /api/maintenances/{id}` | `update(id, maintenance)` | 200 | 404, 400 |
| `MaintenanceController` | `DELETE /api/maintenances/{id}` | `deleteById(id)` | 204 | 404 |

## B. Annotations Jackson

| Association | Annotation retenue | Justification |
|---|---|---|
| `Contrat.paiements` / `Paiement.contrat` | `@JsonManagedReference("contrat-paiements")` / `@JsonBackReference("contrat-paiements")` | Composition (cascade ALL) : les paiements sont créés dans le JSON du contrat ; Jackson renseigne `paiement.contrat` à la lecture, la clé étrangère est donc remplie. |
| `Agence.vehicules`, `Agence.employes` | `@JsonIgnore` | Côté inverse : un véhicule ou un employé est créé seul et désigne son agence par `{"agence": {"idAgence": 1}}`. |
| `Client.reservations` | `@JsonIgnore` | Côté inverse : la réservation désigne son client par identifiant. Pas de `@JsonBackReference` sur `Reservation.client` (le POST perdrait le client). |
| `Vehicule.maintenances` | `@JsonIgnore` | Côté inverse : la maintenance désigne son véhicule par identifiant. |
| `Equipement.vehicules` | `@JsonIgnore` | Côté inverse du `@ManyToMany` porté par `Vehicule.equipements`. |
| `Reservation.contrat` | `@JsonIgnore` | Côté inverse du `@OneToOne` porté par `Contrat.reservation` ; supprime la boucle Réservation → Contrat → Réservation. |
| `Agence`, `Client`, `Vehicule`, `Reservation` (cibles d'un `@ManyToOne`/`@OneToOne` LAZY) | `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` | Le proxy Hibernate ne doit pas exposer ses champs internes (`"hibernateLazyInitializer": {}` observé dans `GET /api/employes`). |

**`@JsonBackReference` ou `@JsonIgnore` ?** Les deux coupent la boucle à l'écriture. `@JsonBackReference` fonctionne
en paire avec `@JsonManagedReference` : à la lecture, Jackson rattache automatiquement chaque enfant à son parent
(utile quand l'enfant est créé dans le JSON du parent). `@JsonIgnore` ignore simplement la propriété dans les deux sens.
On le place côté inverse, quand l'enfant est créé seul et désigne son parent par identifiant.

Erreur rencontrée : avant ces annotations, `GET /api/reservations` produisait
`Document nesting depth (501) exceeds the maximum allowed (500)` (Client → réservations → client → …).

## C. Postman et Swagger

| Outil | Apport | Exemple rencontré dans l'atelier |
|---|---|---|
| Postman | Tester : requêtes enregistrées et rejouables, assertions (`pm.test`), variables (`{{baseUrl}}`, `{{contratId}}`), exécution de toute la collection (Runner). | Le premier passage du Runner (85 tests) a donné 4 échecs : « Créer un véhicule à supprimer » et « Créer un client à supprimer » renvoyaient 500 (immatriculation, e-mail et permis déjà utilisés : contraintes `unique`), donc les DELETE suivants visaient `/null` (400). Corrigé avec des valeurs distinctes : 85/85 PASS. |
| Swagger UI | Documenter et explorer : description OpenAPI générée depuis les contrôleurs, toujours synchronisée avec le code, endpoints essayables (Try it out, commande curl). | `GET /api/contrats/{id}` avec 9999 dans Swagger : 404 documenté (`@ApiResponse`) et corps d'erreur JSON visible. |

## D. Anomalies SonarQube for IDE

| Règle | Explication | Correction apportée |
|---|---|---|
| **java:S1710** — Annotation repetitions should not be wrapped | `@ApiResponses({ @ApiResponse(...), ... })` : `@ApiResponse` est répétable depuis Java 8, le conteneur est inutile. | Les annotations `@ApiResponse` sont écrites directement l'une sous l'autre dans les 9 contrôleurs ; import `ApiResponses` supprimé. |
| « Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method » (`GlobalExceptionHandler`) | `LocalDateTime.now()` dans `GlobalExceptionHandler` dépend implicitement du fuseau de la JVM. | `LocalDateTime.now(ZoneId.systemDefault())` : le fuseau utilisé est explicite. |
| **java:S4684** — Persistent entities should not be used as arguments of `@RequestMapping` methods | `@RequestBody Contrat` expose l'entité JPA (mass assignment : un client peut renseigner n'importe quel champ). | Non corrigée volontairement : l'Atelier 5 expose les entités de façon provisoire ; les DTO du Chapitre 10 la corrigeront. |

Résultat : 51 anomalies avant correction, 17 après (les S4684 restantes et le package `web.dto` encore vide).

## E. Questions

1. **Composant qui reçoit les requêtes.** Le `DispatcherServlet`. Il consulte le `HandlerMapping`, qui associe
   l'URL et le verbe HTTP à une méthode de contrôleur à partir des annotations `@RequestMapping` et `@GetMapping`/`@PostMapping`.
   Il appelle ensuite cette méthode, et Jackson convertit la valeur retournée en JSON.
2. **Pourquoi `IContratService` ?** Le contrôleur dépend d'une abstraction (couplage faible, injection par
   constructeur). Passer par le repository contournerait les règles métier du service (montant négatif, identifiant à la
   création, 404). Dépendre de `ContratServiceImpl` lierait le contrôleur à une implémentation précise.
3. **Codes HTTP.** POST renvoie 201 Created : une ressource a été créée, et le corps contient son identifiant. DELETE renvoie
   204 No Content : l'opération a réussi, et il n'y a rien à renvoyer. Une ressource absente est une erreur du client (mauvais
   identifiant), donc 404. Le code 500 signale une panne du serveur. `ResourceNotFoundException` est traduite en 404 par le `GlobalExceptionHandler`.
4. **Paiement en lecture seule.** Le paiement est une composante du contrat (cascade ALL + orphanRemoval) : on le crée,
   le modifie et le supprime à travers le contrat. `IPaiementService` n'expose que `findById` et `findAll`, donc
   `PaiementController` n'a que des GET. Un `POST /api/paiements` reçoit **405 Method Not Allowed**.
5. **Exposer directement les entités.** Cela couple l'API au schéma de la base : tout changement d'entité change le contrat REST. Les champs
   internes deviennent visibles (proxy Hibernate, collections), avec des risques de boucles de sérialisation. L'API permet aussi l'affectation de masse :
   le client peut envoyer un identifiant ou des associations (S4684). `Contrat.paiements` utilise `@JsonManagedReference` parce que les
   paiements sont créés **dans** le JSON du contrat : Jackson doit rattacher chaque paiement à son contrat à la lecture.
   `Agence.vehicules` utilise `@JsonIgnore` parce qu'un véhicule est créé seul et référence son agence par identifiant :
   la liste n'a pas besoin d'être lue ni écrite.
