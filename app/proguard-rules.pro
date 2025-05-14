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

# Preserva todas las clases de galaxy
-keep class galaxy.** { *; }

# Preserva todas las clases de protobuf
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# Preserva miembros estáticos y descriptores en clases generadas
-keepclassmembers class * {
    private static final java.lang.String[] descriptorData;
    #private static final com.google.protobuf.Descriptors$FileDescriptor descriptor;
}

# Preserva clases específicas de galaxy
-keep class galaxy.Galaxy$Event { *; }
-keep class galaxy.Galaxy$Event$EventDataCase { *; }
-keep class galaxy.Galaxy$Event$EventDataCase$* { *; }

# Ignora advertencias de gRPC
-dontwarn io.grpc.**

# Preserva clases que extienden GeneratedMessageLite
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
    <methods>;
}