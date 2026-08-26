# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/katherinekuan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Gson reflects these unannotated USGS DTO fields by their stable wire names.
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeatureDTO { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQGeometryDTO { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQMetadataDTO { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQPropertiesDTO { <fields>; <init>(...); }

# These classes are read from JSON persisted by the statistics cache.
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsWindowCache { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsWindowsCache { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.data.local.TrendPointCache { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsInsightsCache { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket { <fields>; <init>(...); }
-keep,allowoptimization class com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.ActiveRegion { <fields>; <init>(...); }

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
