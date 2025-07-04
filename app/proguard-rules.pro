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
-keepattributes SourceFile,LineNumberTable,Signature

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-dontobfuscate # not obfuscate ALL
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keep class space.active.taskmanager1c.data.remote.* { *; }
-keep class space.active.taskmanager1c.data.remote.retrofit.* { *; }
-keep class space.active.taskmanager1c.data.remote.model.* { *; }
-keep class space.active.taskmanager1c.data.remote.model.files.* { *; }
-keep class space.active.taskmanager1c.data.remote.model.messages_dto.* { *; }
-keep class space.active.taskmanager1c.data.remote.model.reading_times.* { *; }
-keep class space.active.taskmanager1c.data.remote.model.temp_update.* { *; }
-keep class com.jakewharton.retrofit2.adapter.kotlin.coroutines.** { *; }
-keep class kotlin.coroutines.Continuation { *; }
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
