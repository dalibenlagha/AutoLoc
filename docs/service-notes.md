# Atelier 4 — Couche Service, IoC et injection de dépendances

## A. Modes d'injection comparés

Expérience de l'étape 2 sur `ContratServiceImpl` : remplacement temporaire de l'injection par constructeur
(`@RequiredArgsConstructor` + champ `final`) par une injection par attribut (`@Autowired` sur le champ).
L'application démarre dans les deux cas (`Found 9 JPA repository interfaces`, puis `Started AutolocApiApplication`).

| Critère | Constructeur (`@RequiredArgsConstructor` + `final`) | Attribut (`@Autowired` sur le champ) |
|---|---|---|
| Champ `final` possible ? | Oui | Non : le champ est affecté par réflexion après la construction |
| Dépendance visible dans la signature ? | Oui, elle apparaît dans le constructeur | Non, elle est cachée dans un champ privé |
| Utilisable avec `new` hors conteneur, sans dépendance `null` ? | Oui : impossible de créer l'objet sans lui fournir son repository | Non : `new ContratServiceImpl()` compile, mais `contratRepository` vaut `null` → `NullPointerException` au premier appel |
| Signalé par SonarQube for IDE ? | Non | Oui, règle **S6813** (« Field dependency injection should be avoided ») |

Conclusion : l'injection par constructeur est la convention de tous les services AutoLoc ; l'objet ne peut
jamais exister dans un état incomplet. L'injection par attribut a été retirée après l'expérience.

## B. Services

Tous les services sont annotés `@Service` et `@RequiredArgsConstructor`, avec un unique champ `final`
typé par l'interface du repository. Aucun `new`, aucune injection par attribut, aucun attribut modifiable (sans état).

| Service | Dépendances injectées | Mode | Justification / règle de gestion |
|---|---|---|---|
| `ContratServiceImpl` | `IContratRepository` | Constructeur | CRUD. Refuse un montant total négatif ou absent. La suppression d'un contrat supprime ses paiements (cascade ALL + orphanRemoval, Atelier 2). |
| `AgenceServiceImpl` | `IAgenceRepository` | Constructeur | CRUD. Refuse une agence dont le nom, la ville ou l'adresse est vide. |
| `EmployeServiceImpl` | `IEmployeRepository` | Constructeur | CRUD. Refuse un employé dont le nom ou le prénom est vide, ou sans rôle. |
| `VehiculeServiceImpl` | `IVehiculeRepository` | Constructeur | CRUD. Refuse un tarif journalier négatif ou absent. |
| `EquipementServiceImpl` | `IEquipementRepository` | Constructeur | CRUD. Refuse un équipement sans libellé (le modèle n'a pas d'attribut quantité). |
| `ClientServiceImpl` | `IClientRepository` | Constructeur | CRUD. Refuse un client dont le nom, le prénom, l'e-mail ou le numéro de permis est vide. |
| `ReservationServiceImpl` | `IReservationRepository` | Constructeur | CRUD. Refuse des dates absentes ou une date de fin antérieure à la date de début. |
| `MaintenanceServiceImpl` | `IMaintenanceRepository` | Constructeur | CRUD. Refuse une date de début absente ou une date de fin antérieure à la date de début (le modèle n'a pas d'attribut coût). |
| `PaiementServiceImpl` | `IPaiementRepository` | Constructeur | Lecture seule (`findById`, `findAll`) : un paiement est créé, modifié et supprimé à travers son contrat. |

Règles communes à tous les services CRUD :

- `create` refuse un identifiant non `null` : `save` avec un id renseigné déclenche un `merge` et pourrait écraser une ligne existante.
- `findById` lève `ResourceNotFoundException` au lieu de propager un `Optional` vide ou `null`.
- `update` charge l'entité existante puis recopie uniquement les attributs modifiables (jamais l'id ni les collections) avant `save`.
- `deleteById` vérifie l'existence, car `deleteById` du repository ne lève aucune exception si l'id n'existe pas.
- Les règles de gestion lèvent `IllegalArgumentException` avec un message clair.

## C. Messages d'erreur

**Message 1** : `Parameter 0 of constructor in ContratController required a bean of type IContratService that could not be found.`

- Exception : `NoSuchBeanDefinitionException` (enveloppée dans `UnsatisfiedDependencyException`).
- Bean concerné : `ContratController`, qui demande un `IContratService`.
- Causes probables côté `ContratServiceImpl` :
  1. l'annotation `@Service` a été oubliée : la classe n'est pas un bean ;
  2. la classe est placée hors du package racine `tn.esprit.autoloc` (ou de ses sous-packages) : le scan ne la détecte pas.
- Correction : ajouter `@Service` sur `ContratServiceImpl` et la placer dans `tn.esprit.autoloc.service`.

**Message 2** : `required a single bean, but 2 were found: emailNotificateur, smsNotificateur`

- Exception : `NoUniqueBeanDefinitionException`.
- Cause : deux beans implémentent le même type demandé par `NotificationController`.
- Correction 1 : `@Primary` **sur la classe** de l'implémentation à utiliser par défaut (ex. `EmailNotificateur`).
- Correction 2 : `@Qualifier("smsNotificateur")` **sur le paramètre du constructeur** de `NotificationController`, pour choisir explicitement le bean (nom par défaut = nom de la classe avec la première lettre en minuscule).

**Message 3** : cycle `clientServiceImpl` → `reservationServiceImpl` → `clientServiceImpl`

- Exception : `BeanCurrentlyInCreationException` (dépendance circulaire).
- Pourquoi l'injection par constructeur rend le cycle impossible à contourner : pour construire `ClientServiceImpl`,
  Spring doit d'abord lui passer un `ReservationServiceImpl` déjà construit, qui exige lui-même un `ClientServiceImpl`
  déjà construit. Aucun des deux ne peut être créé en premier, et un champ `final` ne peut pas être rempli plus tard.
- Piste de conception : supprimer la dépendance mutuelle. Chaque service dépend des **repositories** dont il a besoin
  (ex. `ReservationServiceImpl` injecte `IClientRepository` plutôt que `IClientService`), ou bien la responsabilité
  commune est extraite dans un troisième service dont les deux dépendent.

## D. Anomalies SonarQube for IDE

| Règle | Explication | Correction apportée |
|---|---|---|
| **S6813** — Field dependency injection should be avoided | Déclenchée par l'injection par attribut de l'étape 2 (`@Autowired` sur `contratRepository`) : dépendance cachée, champ non `final`, `null` hors conteneur. | Retour à l'injection par constructeur (`@RequiredArgsConstructor` + `private final IContratRepository`). |
| **S112** — Generic exceptions should never be thrown | Déclenchée par l'expérience de l'étape 4 : `throw new RuntimeException(...)` dans `findById` ne dit pas quelle erreur s'est produite et empêche de la traiter spécifiquement. | Restauration de l'exception métier `ResourceNotFoundException`. |
| **S1128** — Unnecessary imports should be removed | Après le retour à l'injection par constructeur, l'import `org.springframework.beans.factory.annotation.Autowired` n'est plus utilisé. | Suppression de l'import (et retour de l'import `lombok.RequiredArgsConstructor`). |

## E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
   Il n'y en a pas dans notre code. C'est le conteneur Spring (`ApplicationContext`, créé par `SpringApplication.run`)
   qui détecte la classe grâce à `@Service` lors du scan, puis appelle lui-même son constructeur (par réflexion)
   en lui passant le proxy `IContratRepository` généré par Spring Data.

2. **Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
   Pour dépendre d'une abstraction et non d'une implémentation : on peut changer d'implémentation (ou en fournir
   une de remplacement pour les tests) sans modifier le contrôleur. Le couplage entre couches reste faible.

3. **Pourquoi un service singleton doit-il rester sans état ? Exemple de bug.**
   Une seule instance est partagée par toutes les requêtes, traitées en parallèle par plusieurs threads.
   Exemple : un attribut `private Contrat contratCourant;` rempli par `findById` puis lu par `update`.
   Si deux utilisateurs appellent le service en même temps, le second écrase `contratCourant` et le premier
   modifie le contrat de l'autre.

4. **Pourquoi `update` charge-t-il l'entité existante avant `save` au lieu de passer directement l'objet reçu ?**
   L'objet reçu peut être incomplet : `save` écraserait toutes les colonnes, y compris celles non fournies (mises à `null`).
   Pour `Contrat`, sa liste `paiements` reçue vide, combinée à `orphanRemoval = true`, supprimerait tous les paiements
   en base. En chargeant l'existant puis en recopiant les seuls attributs modifiables, l'id et les associations sont préservés,
   et l'absence de l'entité est détectée (`ResourceNotFoundException`).

5. **`@Component` vs `@Bean` ? `@Primary` vs `@Qualifier` ? Pourquoi `IPaiementService` n'expose-t-il ni création, ni modification, ni suppression ?**
   - `@Component` se place sur **une classe de notre projet** : le scan la détecte et Spring l'instancie.
     `@Bean` se place sur **une méthode** d'une classe `@Configuration` : on construit l'objet soi-même, utile pour une
     classe qu'on ne peut pas annoter (bibliothèque, JDK) ou qui demande une construction particulière.
   - `@Primary` se pose **sur la classe** : ce bean est choisi par défaut quand plusieurs candidats existent.
     `@Qualifier("nom")` se pose **au point d'injection** (paramètre du constructeur) : choix explicite d'un bean précis.
     `@Qualifier` l'emporte sur `@Primary`.
   - Un paiement est une composition du contrat (cascade ALL + orphanRemoval) : il n'existe que rattaché à son contrat.
     Sa création, sa modification et sa suppression passent donc par le contrat ; un service indépendant permettrait de
     créer des paiements orphelins ou de contourner les règles du contrat.
