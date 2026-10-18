package cl.duoc.atenas.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.atenas.core.Fechas

/*
 * COMPONENTES DE FORMULARIO REUTILIZABLES
 *
 * Los tres componentes de este archivo se usan en todos los formularios de la
 * aplicación. Centralizarlos acá asegura que todos los campos se vean y se
 * comporten igual, y que el manejo de errores sea idéntico en todas partes.
 *
 * Patrón común: cada uno recibe `error: String?`. Si viene null el campo se ve
 * normal; si trae texto, el campo se pinta en rojo y muestra el mensaje debajo
 * usando supportingText, que es lo que exige el checklist.
 */

/**
 * Campo de texto estándar.
 *
 * @param error mensaje a mostrar bajo el campo, o null si el valor es válido.
 * @param icono ícono que acompaña al campo y comunica de un vistazo qué se pide.
 * @param lineas cantidad de líneas visibles; más de 1 lo convierte en área de texto.
 * @param soloNumeros muestra el teclado numérico del teléfono.
 */
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    icono: ImageVector? = null,
    lineas: Int = 1,
    soloNumeros: Boolean = false,
    prefijo: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        // El ícono es opcional: cuando viene, se dibuja a la izquierda del
        // campo y refuerza visualmente qué dato se está pidiendo.
        leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
        isError = error != null,
        // Si hay error se muestra el error; si no, el texto de ayuda opcional.
        supportingText = when {
            error != null -> {
                { Text(error) }
            }
            ayuda != null -> {
                { Text(ayuda) }
            }
            else -> null
        },
        prefix = prefijo?.let { { Text(it) } },
        singleLine = lineas == 1,
        minLines = lineas,
        keyboardOptions = if (soloNumeros) {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        } else {
            KeyboardOptions.Default
        },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Lista desplegable genérica.
 *
 * Es genérica (<T>) para servir tanto a enums como a entidades de la base:
 * recibe las opciones y una función que dice cómo mostrar cada una como texto.
 *
 * El campo es readOnly para que el usuario no pueda escribir valores que no
 * existen en la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListaDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    textoDe: (T) -> String,
    alSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    icono: ImageVector? = null,
    textoVacio: String = "Selecciona una opción"
) {
    // Estado local: si el menú está abierto o cerrado. Es estado de UI puro,
    // no pertenece al ViewModel.
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionada?.let(textoDe) ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text(textoVacio) },
            leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            // menuAnchor conecta el campo con el menú para que se despliegue
            // justo debajo y con el mismo ancho.
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        alSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

/**
 * Selector de fecha.
 *
 * Muestra la fecha como texto en un campo de solo lectura; al tocarlo abre el
 * calendario de Material 3. Trabaja con milisegundos (Long), que es como se
 * guardan las fechas en la base de datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFecha(
    etiqueta: String,
    valor: Long?,
    alSeleccionar: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null
) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor?.let { Fechas.aTexto(it) } ?: "",
        onValueChange = { },
        readOnly = true,
        label = { Text(etiqueta) },
        placeholder = { Text("dd/mm/aaaa") },
        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
        isError = error != null,
        supportingText = when {
            error != null -> {
                { Text(error) }
            }
            ayuda != null -> {
                { Text(ayuda) }
            }
            else -> null
        },
        trailingIcon = {
            TextButton(onClick = { mostrarCalendario = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir fecha")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (mostrarCalendario) {
        val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = valor)
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoCalendario.selectedDateMillis?.let(alSeleccionar)
                        mostrarCalendario = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}

/**
 * Selector de hora con reloj de Material 3.
 *
 * Trabaja con minutos desde medianoche (Int), que es como la base de datos
 * guarda las horas. Evita que el usuario escriba horarios a mano y, por lo
 * tanto, evita tener que validar formatos como "10am", "10.00" o "25:70".
 *
 * El reloj se abre dentro de un AlertDialog: Material 3 entrega el TimePicker
 * suelto, sin un diálogo propio como sí tiene el calendario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorHora(
    etiqueta: String,
    minutosDelDia: Int,
    alSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var mostrarReloj by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = Fechas.horaATexto(minutosDelDia),
        onValueChange = { },
        readOnly = true,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon = {
            TextButton(onClick = { mostrarReloj = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "Elegir hora")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (mostrarReloj) {
        // El estado del reloj se crea con la hora actual del campo, para que
        // se abra justo donde está en vez de en medianoche.
        val estadoReloj = rememberTimePickerState(
            initialHour = minutosDelDia / 60,
            initialMinute = minutosDelDia % 60,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { mostrarReloj = false },
            title = { Text(etiqueta) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = estadoReloj)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alSeleccionar(estadoReloj.hour * 60 + estadoReloj.minute)
                        mostrarReloj = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarReloj = false }) { Text("Cancelar") }
            }
        )
    }
}
