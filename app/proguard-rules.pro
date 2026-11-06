# ProGuard rules for StemSplitter
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to rules specified
# in /Users/.../build-tools/.../proguard-android.txt

# Keep names of classes that are referenced in AndroidManifest.xml
-keep class com.sikorokoro44.stemsplitter.MainActivity { *; }

# Keep custom audio classes
-keep class com.sikorokoro44.stemsplitter.audio.* { *; }

# Keep data classes
-keepclassmembers class com.sikorokoro44.stemsplitter.AudioData { *; }
-keepclassmembers class com.sikorokoro44.stemsplitter.SeparationResult { *; }

# Suppress warnings for kotlinx coroutines
-dontwarn kotlinx.**
