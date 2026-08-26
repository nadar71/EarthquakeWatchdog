# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/katherinekuan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# No project keep rules are required by the release bundle or smoke checks. Add a
# narrowly scoped rule only with a reproducible R8 failure as evidence.

# OkHttp probes these optional JVM TLS providers; Android does not package them.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Ktor's transitive SLF4J API probes this optional JVM logging binder.
-dontwarn org.slf4j.impl.StaticLoggerBinder

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
