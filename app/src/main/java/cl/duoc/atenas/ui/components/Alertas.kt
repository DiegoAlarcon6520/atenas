package cl.duoc.atenas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.ui.theme.Espaciado

/*
 * COMPONENTES DE ALERTAS (HU-17)
 *
 * Compartidos por el tablero del ejecutivo y el panel del supervisor, para
 * que una alerta se vea igual en ambos.
 */

/**
 * Tarjeta de una gestión atrasada. Tocarla abre la oportunidad para
 * registrar la gestión pendiente. El supervisor ve además el responsable.
 */
@Composable
fun TarjetaGestionAtrasada(
    gestion: GestionPendiente,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarResponsable: Boolean = false
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(gestion.oportunidadNombre, style = MaterialTheme.typography.titleSmall)
            Text(
                text = gestion.proximaAccion ?: "Sin próxima acción definida",
                style = MaterialTheme.typography.bodySmall
            )
            if (mostrarResponsable) {
                Text(
                    text = gestion.responsableNombre ?: "Sin responsable",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Vencía el ${Fechas.aTexto(gestion.fechaProximoSeguimiento)}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
