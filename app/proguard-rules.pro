# --- BLE Spam ProGuard / R8 rules ---

# Keep Hilt generated components.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room: keep entities and generated DAOs referenced via reflection.
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Kotlin coroutines internals used via reflection.
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Keep Compose runtime annotations.
-keep class androidx.compose.runtime.** { *; }

# Keep our own serializable-ish model classes (used for export/import of configs).
-keep class com.blespam.app.data.local.entity.** { *; }
-keep class com.blespam.app.domain.model.** { *; }

# Standard Android component keeps.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
