package mobi.thinkchange.android.fingerscannercn.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import mobi.thinkchange.android.fingerscannercn.SettingsActivity;

public final class SharedPreferencesHelper {

    static final String FEEDBACK_CONTACT = "feedback_contact";
    static final String INIT_SETTINGS_SHOWN = "init_settings_shown";

    private SharedPreferences pref;

    public SharedPreferencesHelper(Context context) {
        // 解决跨进程读取SharedPreference的问题：http://stackoverflow.com/questions/12125214/shared-preferences-between-two-processes-of-the-same-application
        int flag = Build.VERSION.SDK_INT >= 11 ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE;
        pref = context.getSharedPreferences(getDefaultSharedPreferencesName(context), flag);
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    public void destroy() {
        pref = null;
    }

    /**
     * 用户是否启用锁屏。
     *
     * @param defValue 默认值建议为true
     * @return
     */
    public boolean enableLock(boolean defValue) {
        boolean res = false;

        res = pref.getBoolean(SettingsActivity.PREF_KEY_LOCK_SWITCH, defValue);

        return res;
    }

    public int lockCondition(int defValue) {
        int res = pref.getInt(SettingsActivity.PREF_KEY_LOCK_CONDITION, defValue);

        return res;
    }

    public String dateFormatIndex(String defValue) {
        return pref.getString(SettingsActivity.PREF_KEY_DATE_FORMAT, defValue);
    }

    public String timeFormatIndex(String defValue) {
        return pref.getString(SettingsActivity.PREF_KEY_TIME_FORMAT, defValue);
    }

    public boolean vibrate(boolean defValue) {
        return pref.getBoolean(SettingsActivity.PREF_KEY_VIBRATE, defValue);
    }

    public boolean beep(boolean defValue) {
        return pref.getBoolean(SettingsActivity.PREF_KEY_BEEP, defValue);
    }

    public boolean autoStart(boolean defValue) {
        return pref.getBoolean(SettingsActivity.PREF_KEY_AUTO_START, defValue);
    }

    public int openTimes(int defValue) {
        return pref.getInt(SettingsActivity.PREF_KEY_SPECIAL_OPEN_TIMES, defValue);
    }

    public void increaseOpenTimes(int originalOpenTimes) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(SettingsActivity.PREF_KEY_SPECIAL_OPEN_TIMES, originalOpenTimes + 1);
        applyCompat(edit);
    }

    /**
     * 保存意见反馈联系方式
     *
     * @param contact 联系方式
     */
    public void setFeedBackContact(String contact) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(FEEDBACK_CONTACT, contact);
        applyCompat(edit);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static void applyCompat(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();
    }

    /**
     * 获取意见反馈联系方式
     */
    public String feedBackContact() {
        return pref.getString(FEEDBACK_CONTACT, "");
    }

    /**
     * 是否开启天气预报。
     * @param defValue
     * @return
     */
    public boolean weather(boolean defValue) {
        return pref.getBoolean(SettingsActivity.PREF_KEY_WEATHER, defValue);
    }

    public String lockAnimStyle(String defValue) {
        return pref.getString(SettingsActivity.PREF_KEY_LOCK_ANIMATION_STYLE, defValue);
    }

    public String lockTextAnimStyle(String defValue) {
        return pref.getString(SettingsActivity.PREF_KEY_LOCK_TEXT_ANIMATION_STYLE, defValue);
    }

    /**
     * 是否显示过初始化设置，默认为false
     * @return
     */
    public boolean initSettingsShown() {
        return pref.getBoolean(INIT_SETTINGS_SHOWN, false);
    }

    public void setInitSettingsShown(boolean shown) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(INIT_SETTINGS_SHOWN, shown);
        applyCompat(edit);
    }

}
