# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ── Preserve line numbers for crash reports ──────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── kotlinx-serialization ────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

-keepclasseswithmembers class dev.harrisonsoftware.stitchCounter.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers @kotlinx.serialization.Serializable class dev.harrisonsoftware.stitchCounter.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Room ─────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Hilt ─────────────────────────────────────────────────────────────
-dontwarn dagger.hilt.android.internal.**

# ── Compose Destinations ─────────────────────────────────────────────
-keep class dev.harrisonsoftware.stitchCounter.feature.destinations.** { *; }
-keep class dev.harrisonsoftware.stitchCounter.feature.NavGraphs { *; }
