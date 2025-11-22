# Compose rules
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Keep Model classes used in JSON/Reflection (Safe practice)
-keep class com.sourcepack.data.** { *; }

# Keep VM structure
-keepclassmembers class com.sourcepack.viewmodel.MainVM {
    <init>(...);
}

# Standard Android
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable