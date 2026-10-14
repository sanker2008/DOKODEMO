# libv2ray.aar supplies consumer rules for its go.* and libv2ray.* JNI bridge.
# Keep this app-owned JNI entry point's class and method names for the native symbols.
-keep class com.v2ray.ang.service.TProxyService {
    public native void TProxyStartService(java.lang.String, int);
    public native void TProxyStopService();
    public native long[] TProxyGetStats();
}

# Gson persists CustomRoutingRule as JSON using field and enum constant names.
# Gson 2.11.0 keeps TypeToken signatures, but this model has no no-args constructor:
# R8 full mode must keep the concrete class for Gson's reflective allocation.
# Preserve the stored field and enum names across releases.
-keepattributes Signature,*Annotation*
-keep,allowobfuscation class com.dokodemo.data.preferences.CustomRoutingRule
-keepclassmembers class com.dokodemo.data.preferences.CustomRoutingRule {
    <fields>;
}
-keepclassmembers enum com.dokodemo.data.preferences.CustomRuleMatchType {
    public static final com.dokodemo.data.preferences.CustomRuleMatchType *;
}
-keepclassmembers enum com.dokodemo.data.preferences.CustomRuleAction {
    public static final com.dokodemo.data.preferences.CustomRuleAction *;
}
