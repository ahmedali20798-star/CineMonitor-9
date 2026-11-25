# Keep Kotlinx Serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.canon.multicampro.domain.model.**$$serializer { *; }
-keepclassmembers class com.canon.multicampro.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.canon.multicampro.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
