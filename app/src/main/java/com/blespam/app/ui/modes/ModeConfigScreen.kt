package com.blespam.app.ui.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.data.model.AdvertisingMode
import com.blespam.app.data.model.AdvertisingSpeed
import com.blespam.app.data.model.BleConfig
import com.blespam.app.data.model.TxPowerLevel
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GradientButton
import com.blespam.app.ui.components.Pill
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * The visual payload editor. Exposes every configurable advertising field for the selected mode
 * and renders a live hex preview with a running byte count against Android's 31-byte legacy limit.
 * Fields that don't apply to a generated mode are hidden to keep the surface honest.
 */
@Composable
fun ModeConfigScreen(
    viewModel: SessionViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = config.mode.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Payload editor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HexPreviewCard(preview = preview)

        // Identity fields
        val showName = config.mode in setOf(
            AdvertisingMode.GENERIC, AdvertisingMode.CUSTOM_LOCAL_NAME, AdvertisingMode.CUSTOM_PAYLOAD,
            AdvertisingMode.RANDOM_UUID,
        )
        if (showName) {
            SectionHeader(title = "Identity")
            GlassCard {
                LabeledField(
                    label = "Local name",
                    value = config.localName,
                    onValueChange = viewModel::setLocalName,
                )
                ToggleRow(
                    label = "Include device name",
                    checked = config.includeDeviceName,
                    onCheckedChange = viewModel::setIncludeDeviceName,
                )
            }
        }

        val showUuid = config.mode in setOf(AdvertisingMode.GENERIC, AdvertisingMode.CUSTOM_PAYLOAD)
        if (showUuid) {
            SectionHeader(title = "Service")
            GlassCard {
                LabeledField(
                    label = "Service UUID (16-bit or 128-bit)",
                    value = config.serviceUuid,
                    onValueChange = viewModel::setServiceUuid,
                )
                ToggleRow(
                    label = "Include service UUID",
                    checked = config.includeServiceUuid,
                    onCheckedChange = viewModel::setIncludeServiceUuid,
                )
                LabeledField(
                    label = "Service data (hex)",
                    value = config.serviceDataHex,
                    onValueChange = viewModel::setServiceData,
                    monospace = true,
                    keyboardType = KeyboardType.Ascii,
                )
            }
        }

        val showManufacturer = config.mode in setOf(
            AdvertisingMode.GENERIC, AdvertisingMode.CUSTOM_PAYLOAD, AdvertisingMode.RANDOM_MANUFACTURER,
            AdvertisingMode.RANDOM_PAYLOAD,
        )
        if (showManufacturer) {
            SectionHeader(title = "Manufacturer Data")
            GlassCard {
                LabeledField(
                    label = "Manufacturer ID (hex, e.g. FFFF)",
                    value = "%04X".format(config.manufacturerId),
                    onValueChange = { text ->
                        text.toIntOrNull(16)?.let(viewModel::setManufacturerId)
                    },
                    monospace = true,
                    keyboardType = KeyboardType.Ascii,
                )
                val editableData = config.mode == AdvertisingMode.GENERIC || config.mode == AdvertisingMode.CUSTOM_PAYLOAD
                if (editableData) {
                    LabeledField(
                        label = "Manufacturer data (hex)",
                        value = config.manufacturerDataHex,
                        onValueChange = viewModel::setManufacturerData,
                        monospace = true,
                        keyboardType = KeyboardType.Ascii,
                    )
                } else {
                    Text(
                        "Data is generated automatically each session for this mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }

        // Radio settings apply to every mode.
        SectionHeader(title = "Radio")
        GlassCard {
            Text("TX Power", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SegmentedRow(
                options = TxPowerLevel.entries.map { it.displayName },
                selectedIndex = TxPowerLevel.entries.indexOf(config.txPower),
                onSelected = { viewModel.setTxPower(TxPowerLevel.entries[it]) },
            )
            Spacer(Modifier.height(16.dp))
            Text("Advertising interval", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SegmentedRow(
                options = AdvertisingSpeed.entries.map { it.displayName },
                selectedIndex = AdvertisingSpeed.entries.indexOf(config.speed),
                onSelected = { viewModel.setSpeed(AdvertisingSpeed.entries[it]) },
            )
            Spacer(Modifier.height(8.dp))
            ToggleRow(
                label = "Include TX power in payload",
                checked = config.includeTxPower,
                onCheckedChange = viewModel::setIncludeTxPower,
            )
            ToggleRow(
                label = "Connectable",
                checked = config.connectable,
                onCheckedChange = viewModel::setConnectable,
            )
        }

        SectionHeader(title = "Duration")
        GlassCard {
            DurationSelector(
                durationMs = config.durationMs,
                onChange = viewModel::setDurationMs,
            )
        }

        GradientButton(
            text = "Save configuration",
            onClick = { showSaveDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.height(20.dp))
        }
    }

    if (showSaveDialog) {
        SaveConfigDialog(
            defaultName = config.mode.displayName,
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                viewModel.saveCurrentConfig(name)
                showSaveDialog = false
            },
        )
    }
}

@Composable
private fun HexPreviewCard(preview: com.blespam.app.ble.PayloadBuilder.Built) {
    val theme = LocalAppTheme.current
    GlassCard(highlight = !preview.isValid) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Hex Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Pill(
                text = "${preview.estimatedBytes} / ${BleConfig.MAX_LEGACY_PAYLOAD_BYTES} B",
                color = if (preview.isValid) theme.accent else MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = preview.previewHex.ifBlank { "No payload data" },
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
        preview.error?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    monospace: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = if (monospace) {
            MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
        } else {
            MaterialTheme.typography.bodyLarge
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DurationSelector(durationMs: Int, onChange: (Int) -> Unit) {
    val options = listOf(0, 10_000, 30_000, 60_000, 120_000)
    val labels = listOf("∞", "10s", "30s", "60s", "120s")
    SegmentedRow(
        options = labels,
        selectedIndex = options.indexOf(durationMs).coerceAtLeast(0),
        onSelected = { onChange(options[it]) },
    )
}
