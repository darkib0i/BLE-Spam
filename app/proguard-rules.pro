# Keep Room, Hilt and Kotlin metadata used through reflection.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn org.slf4j.**

# Compose already ships with the required rules; keep model classes serialized to logs.
-keep class com.blespam.app.data.model.** { *; }
