package tn.esprit.autoloc.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de la requête d'affectation d'un véhicule à un équipement.
 */
public record AffectationEquipementRequest(

        @Schema(description = "Identifiant du véhicule", example = "1")
        @NotNull(message = "L'identifiant du véhicule est obligatoire")
        @Positive(message = "L'identifiant du véhicule doit être positif")
        Long idVehicule,

        @Schema(description = "Identifiant de l'équipement", example = "1")
        @NotNull(message = "L'identifiant de l'équipement est obligatoire")
        @Positive(message = "L'identifiant de l'équipement doit être positif")
        Long idEquipement) {
}
