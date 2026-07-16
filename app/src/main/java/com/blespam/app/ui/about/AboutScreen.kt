package com.blespam.app.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.blespam.app.BuildConfig
import com.blespam.app.R
import com.blespam.app.ui.components.EnterAnimation
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.components.floating
import com.blespam.app.ui.theme.LocalGlass

/**
 * The About page: animated hero, version/developer/license/privacy info, the
 * responsible-use statement, and the list of open-source libraries the app is
 * built on.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val glass = LocalGlass.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 28.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(8.dp))
        EnterAnimation {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_ble_logo),
                    contentDescription = null,
                    modifier = Modifier.size(130.dp).blur(34.dp),
                )
                Image(
                    painter = painterResource(R.drawable.ic_ble_logo),
                    contentDescription = "BLE Spam",
                    modifier = Modifier.size(110.dp).floating(),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        EnterAnimation(delayMillis = 60) {
            Text("BLE Spam", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        EnterAnimation(delayMillis = 100) {
            Text(
                "Bluetooth Low Energy Testing Suite",
                color = glass.accent,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        EnterAnimation(delayMillis = 140) {
            Text(
                "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(28.dp))

        EnterAnimation(delayMillis = 180) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AboutRow(Icons.Rounded.Person, "Developer", "BLE Spam Project")
                    AboutRow(Icons.Rounded.Gavel, "License", "Apache License 2.0")
                    AboutRow(Icons.Rounded.Lock, "Privacy", "No data leaves your device. No analytics, no tracking.")
                    AboutRow(Icons.Rounded.Shield, "Purpose", "Development, debugging and BLE interoperability testing.")
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        EnterAnimation(delayMillis = 220) {
            SectionHeader("Responsible Use", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
        }
        EnterAnimation(delayMillis = 240) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "This tool broadcasts only within the standard Android BLE " +
                        "advertising API and its OS-enforced limits. Use it to test devices " +
                        "you own or are authorised to test. Broadcasting to disrupt, deceive, " +
                        "or harass people or devices you do not control may be illegal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        EnterAnimation(delayMillis = 260) {
            SectionHeader("Open Source Libraries", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
        }
        EnterAnimation(delayMillis = 280) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    libraries.forEach { (name, license) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(license, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun AboutRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = LocalGlass.current.accent, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private val libraries = listOf(
    "Jetpack Compose" to "Apache-2.0",
    "Material 3" to "Apache-2.0",
    "AndroidX Navigation" to "Apache-2.0",
    "Hilt (Dagger)" to "Apache-2.0",
    "Room" to "Apache-2.0",
    "DataStore" to "Apache-2.0",
    "Kotlin Coroutines" to "Apache-2.0",
    "Accompanist Permissions" to "Apache-2.0",
)
