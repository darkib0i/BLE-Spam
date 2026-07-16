package com.blespam.app.ui.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.domain.util.HexUtils
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GlassTextField
import com.blespam.app.ui.components.GradientActionButton
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalGlass
import com.blespam.app.ui.theme.StatusError
import com.blespam.app.ui.theme.StatusOk

/**
 * The visual payload editor. Lets the user compose an advertisement field by
 * field and shows a live, color-coded hex preview of the resulting AD
 * structures plus a running size budget against the 31-byte legacy limit.
 */
@Composable
fun PayloadEditorScreen(
    onBack: () -> Unit,
    viewModel: ModesViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val glass = LocalGlass.current
    val snackbar = remember { SnackbarHostState() }
    var saveName by remember { mutableStateOf("") }

    // Local editable text mirrors of the byte-array fields.
    val manuHex = remember(config.manufacturerData) { HexUtils.toHex(config.manufacturerData, " ") }
    val serviceHex = remember(config.serviceData) { HexUtils.toHex(config.serviceData, " ") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    val validation = viewModel.validation()
    val preview = viewModel.hexPreview()

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(0.dp))
                Text(
                    "Payload Editor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- Live hex preview ---
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hex Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text(
                            validation.message,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (validation.fitsLegacy) StatusOk else StatusError,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    if (preview.isEmpty()) {
                        Text("No AD structures yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    preview.forEach { line ->
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(line.label, style = MaterialTheme.typography.labelMedium, color = glass.accent)
                            Text(
                                line.hex,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Fields", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    GlassTextField(
                        value = config.localName.orEmpty(),
                        onValueChange = viewModel::setLocalName,
                        label = "Local Name",
                        placeholder = "BLE Spam",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassTextField(
                        value = config.serviceUuid.orEmpty(),
                        onValueChange = viewModel::setServiceUuid,
                        label = "Service UUID (128-bit)",
                        placeholder = "0000180d-0000-1000-8000-00805f9b34fb",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassTextField(
                        value = config.manufacturerId?.let { "%04X".format(it) }.orEmpty(),
                        onValueChange = viewModel::setManufacturerId,
                        label = "Manufacturer ID (hex)",
                        placeholder = "FFFF",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassTextField(
                        value = manuHex,
                        onValueChange = viewModel::setManufacturerData,
                        label = "Manufacturer Data (hex)",
                        placeholder = "01 02 03 04",
                        isError = !HexUtils.isValidHex(manuHex),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassTextField(
                        value = config.serviceDataUuid.orEmpty(),
                        onValueChange = viewModel::setServiceDataUuid,
                        label = "Service Data UUID",
                        placeholder = "0000fe2c-0000-1000-8000-00805f9b34fb",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassTextField(
                        value = serviceHex,
                        onValueChange = viewModel::setServiceData,
                        label = "Service Data (hex)",
                        placeholder = "AA BB CC",
                        isError = !HexUtils.isValidHex(serviceHex),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Connectable", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = config.connectable, onCheckedChange = viewModel::setConnectable)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlassCard(modifier = Modifier.weight(1f), onClick = viewModel::randomizePayload) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Casino, null, tint = glass.accent)
                        Text("  Randomize", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    GlassTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        label = "Configuration name",
                        placeholder = "My test config",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    GradientActionButton(
                        text = "Save Configuration",
                        icon = Icons.Rounded.Save,
                        onClick = { viewModel.saveCurrent(saveName); saveName = "" },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}
