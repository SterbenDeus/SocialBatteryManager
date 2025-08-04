# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep data model classes used for Gson serialization and Firebase
# to ensure fields are not stripped or obfuscated during minification.
-keep class com.example.socialbatterymanager.data.model.** { *; }

# Keep ViewModel subclasses that may be accessed via reflection
-keep class com.example.socialbatterymanager.**ViewModel { *; }

# Keep Worker implementations for WorkManager
-keep class com.example.socialbatterymanager.sync.** { *; }

# Keep only the PDFBox classes referenced in the app
-keep class org.apache.pdfbox.pdmodel.PDDocument { *; }
-keep class org.apache.pdfbox.pdmodel.PDPage { *; }
-keep class org.apache.pdfbox.pdmodel.common.PDRectangle { *; }
-keep class org.apache.pdfbox.pdmodel.PDPageContentStream { *; }
-keep class org.apache.pdfbox.pdmodel.font.PDType1Font { *; }

# Keep only the MPAndroidChart classes referenced in the app
-keep class com.github.mikephil.charting.charts.LineChart { *; }
-keep class com.github.mikephil.charting.charts.BarChart { *; }
-keep class com.github.mikephil.charting.data.Entry { *; }
-keep class com.github.mikephil.charting.data.LineData { *; }
-keep class com.github.mikephil.charting.data.LineDataSet { *; }
-keep class com.github.mikephil.charting.data.BarEntry { *; }
-keep class com.github.mikephil.charting.data.BarData { *; }
-keep class com.github.mikephil.charting.data.BarDataSet { *; }
-keep class com.github.mikephil.charting.formatter.IndexAxisValueFormatter { *; }

# Suppress warnings from unused classes in these libraries
-dontwarn org.apache.pdfbox.**
-dontwarn com.github.mikephil.charting.**
