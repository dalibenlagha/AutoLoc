package tn.esprit.autoloc.web.dto;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;

/**
 * Réponse : véhicule avec la liste des équipements qui lui sont affectés.
 */
public record VehiculeDTO(
        Long idVehicule,
        String immatriculation,
        String marque,
        String modele,
        CategorieVehicule categorie,
        BigDecimal tarifJournalier,
        StatutVehicule statut,
        List<String> equipements) {

    public static VehiculeDTO fromEntity(Vehicule v) {
        List<String> libelles = v.getEquipements().stream()
                .sorted(Comparator.comparing(Equipement::getIdEquipement))
                .map(Equipement::getLibelle)
                .toList();
        return new VehiculeDTO(
                v.getIdVehicule(),
                v.getImmatriculation(),
                v.getMarque(),
                v.getModele(),
                v.getCategorie(),
                v.getTarifJournalier(),
                v.getStatut(),
                libelles);
    }
}
