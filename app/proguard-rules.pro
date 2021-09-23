-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-repackageclasses
