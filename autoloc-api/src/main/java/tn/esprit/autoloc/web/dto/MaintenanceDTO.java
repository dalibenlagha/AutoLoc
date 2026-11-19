package tn.esprit.autoloc.web.dto;

import java.time.LocalDate;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Vehicule;

/**
 * Réponse : maintenance avec le véhicule auquel elle est affectée.
 */
public record MaintenanceDTO(
        Long idMaintenance,
        LocalDate dateDebut,
        LocalDate dateFin,
        String description,
        Long idVehicule,
        String immatriculation) {

    public static MaintenanceDTO fromEntity(Maintenance m) {
        Vehicule v = m.getVehicule();
        return new MaintenanceDTO(
                m.getIdMaintenance(),
                m.getDateDebut(),
                m.getDateFin(),
                m.getDescription(),
                v != null ? v.getIdVehicule() : null,
                v != null ? v.getImmatriculation() : null);
    }
}
