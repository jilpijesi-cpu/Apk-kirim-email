# Keep Retrofit, OkHttp, and Gson models/interfaces under R8 minification
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

-keep class com.gswift.relay.data.model.** { *; }
-keepclassmembers class com.gswift.relay.data.model.** { *; }
-keep interface com.gswift.relay.data.remote.GSwiftApiService { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
