package mobi.thinkchange.android.fingerscannercn.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.DisplayMetrics;

import java.util.Locale;

import mobi.thinkchange.android.fingerscannercn.FloatingTipActivity;
import mobi.thinkchange.android.fingerscannercn.SleepService;

/**
 * 工具类
 */
public class Utils {

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int width = mDisplayMetrics.widthPixels;
        return width;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenHight(Context context) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int hight = mDisplayMetrics.heightPixels;
        return hight;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 检查网络是否可用
     */
    public static boolean isNetworkAvailable(Context mContext) {
        Context context = mContext.getApplicationContext();
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 分享文本文字 *
     *
     * @param subject 标题
     * @param text    内容
     */
    public static void shareText(Context context, String subject, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*"); // 分享图片的话这里改成image/*
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(intent);
    }

    public static void updateLockService(Context context, boolean enable) {
        if (enable) {
            // 启动锁屏服务
            Intent intent = new Intent(context, SleepService.class);
            intent.putExtra("reason", "disable.keyguard");
            context.startService(intent);
        } else {
            Intent intent = new Intent(context, SleepService.class);
            intent.putExtra("reason", "stop.service");
            context.startService(intent);
        }
    }

    /**
     * 设置Component是否启用。<br/>
     * 可以设置Activity, Service, Receiver, ContentProvider.<br/>
     * 代码出处参考： <a href=
     * "http://developer.android.com/training/scheduling/alarms.html#boot"
     * >Scheduling Repeating Alarms</a>
     *
     * @param context
     * @param targetComponentClass
     * @param enable
     */
    public static void setComponentEnabledSetting(Context context,
                                                  Class<?> targetComponentClass, boolean enable) {
        int targetNewState = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        ComponentName target = new ComponentName(context, targetComponentClass);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(target, targetNewState,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            return true;
        }
        return false;
    }

    public static boolean isMeizu() {
        String str = Build.BRAND;
        if (str == null) ;
        while (str.toLowerCase(Locale.ENGLISH).indexOf("meizu") <= -1)
            return false;
        return true;
    }

    public static boolean isXiaomi() {
        if ("xiaomi".equalsIgnoreCase(Build.MANUFACTURER))
            return true;

        if ("miui".equalsIgnoreCase(Build.ID))
            return true;

        if ("xiaomi".equalsIgnoreCase(Build.BRAND))
            return true;

        if (Build.MODEL != null) {
            String str = Build.MODEL.toLowerCase();
            if (str.contains("xiaomi"))
                return true;
            if (str.contains("miui"))
                return true;
        }

        return false;
    }

    public static void startGenericSystemLock(Context context) {
        Intent intentSystemLock = new Intent();
        intentSystemLock.setComponent(new ComponentName("com.android.settings", "com.android.settings.ChooseLockGeneric"));
        context.startActivity(intentSystemLock);
    }

    public static void startXiaomiSystemLock(Context context) {
        Intent intentDev = new Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS");
        context.startActivity(intentDev);
    }

    public static void startMeizuAutoStart(Context context) {
        Intent intentAppInfo = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intentAppInfo);
    }

    public static void startXiaomiAutoStart(Context context) {
        Intent localIntent = new Intent();
        localIntent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        context.startActivity(localIntent);
    }

    public static void startXiaomiAppPermissionsEditor(Context context) {
        try {
            _startXiaomiAppPermissionsEditorApproach01(context);
        } catch (Exception localException) {
            _startXiaomiAppPermissionsEditorApproach02(context);
        }
    }

    private static void _startXiaomiAppPermissionsEditorApproach01(Context context) {
        try {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setComponent(new ComponentName("com.android.settings", "com.miui.securitycenter.permission.AppPermissionsEditor"));
            intent.putExtra("extra_package_uid", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.uid);
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void _startXiaomiAppPermissionsEditorApproach02(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity"));
        intent.putExtra("extra_pkgname", context.getPackageName());
        context.startActivity(intent);
    }

    public static void showFloatingTip(Context context, int messageId) {
        Intent floatingTip = new Intent(context, FloatingTipActivity.class);
        floatingTip.putExtra(FloatingTipActivity.EXTRA_MESSAGE, messageId);
        context.startActivity(floatingTip);
    }

}
