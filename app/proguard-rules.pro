# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ThinkChange Common Utility 3.12
-keep class mobi.thinkchange.**.R$string {
  public static final int THINKCHANGE_*;
  public static final int TC_*;
  public static final int app_name;
  public static final int dialog_*;
  public static final int dlg_*;
}
-keep class mobi.thinkchange.**.R$layout {
  public static final int dialog_*;
}
-keep class mobi.thinkchange.**.R$id {
  public static final int dialog_*;
}
-keep class mobi.thinkchange.**.R$drawable {
  public static final int dialog_*;
  public static final int tcu_*;
}
-keep class mobi.thinkchange.**.R$style {
  public static final int Dialog*;
  public static final int TCURatingBar;
}

# App releated
-keep class mobi.thinkchange.android.fingerscannercn.LockActivity {
  public void onEventMainThread(mobi.thinkchange.android.fingerscannercn.location.LocationEvent);
}
-keep class mobi.thinkchange.android.fingerscannercn.weather.GsonRequest {
  public *;
}
-keep class mobi.thinkchange.android.fingerscannercn.weather.bean.** {
  *;
}

