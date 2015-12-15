package mobi.thinkchange.android.fingerscannercn;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import mobi.thinkchange.android.fingerscannercn.util.Utils;

public class SettingsActivity
        extends AbsBaseActivity {

    // 锁屏相关
    public static final String PREF_KEY_LOCK_SWITCH = "cb_enable";
    public static final String PREF_KEY_LOCK_CONDITION = "unlock_times";
    public static final String PREF_KEY_AUTO_START = "cb_autostart";

    // 跳转
    public static final String PREF_KEY_PREVIEW = "pref_key_preview";
    public static final String PREF_KEY_HELP = "pref_key_help";
    public static final String PREF_KEY_INIT_SETTINGS = "pref_key_init_settings";

    // 二级设置
    public static final String PREF_KEY_CUSTOMIZE = "pref_key_customize";
    public static final String PREF_KEY_ADVANCED = "pref_key_advanced";

    // 声音/振动
    public static final String PREF_KEY_VIBRATE = "cb_vibrate";
    public static final String PREF_KEY_BEEP = "cb_scanning_sound";

    // 显示相关
    public static final String PREF_KEY_DATE_FORMAT = "date_format";
    public static final String PREF_KEY_TIME_FORMAT = "time_format";
    public static final String PREF_KEY_WEATHER = "cb_weather";
    public static final String PREF_KEY_LOCK_ANIMATION_STYLE = "pref_key_lock_anim_type";
    public static final String PREF_KEY_LOCK_TEXT_ANIMATION_STYLE = "pref_key_lock_text_anim_type";

    // 标记打开次数
    public static final String PREF_KEY_SPECIAL_OPEN_TIMES = "open_times";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 透明状态栏及导航栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            // 手动设置status bar的"背景"颜色
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.primary_green);

            // 手动设置显示内容的padding
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            findViewById(android.R.id.content).setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(), config.getPixelInsetBottom());
        }

        if (savedInstanceState == null) {
            // Display the fragment as the main content.
            getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!popupFragment())
                NavUtils.navigateUpFromSameTask(this);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!popupFragment())
            super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean popupFragment() {
        // 处理返回按钮的返回事件
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();

            return true;
        }

        return false;
    }

    /**
     * 设置一级界面。
     *
     * @author FIMH
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefsFragment
            extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_entry);

            init();
        }

        @Override
        public void onResume() {
            super.onResume();

            // 还原一级界面的Title
            getActivity().setTitle(R.string.settings);
        }

        private void init() {
            findPreference(PREF_KEY_LOCK_SWITCH).setOnPreferenceChangeListener(this);

            findPreference(PREF_KEY_PREVIEW).setOnPreferenceClickListener(this);
            findPreference(PREF_KEY_HELP).setOnPreferenceClickListener(this);
            findPreference(PREF_KEY_CUSTOMIZE).setOnPreferenceClickListener(this);
            findPreference(PREF_KEY_ADVANCED).setOnPreferenceClickListener(this);
            findPreference(PREF_KEY_INIT_SETTINGS).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (PREF_KEY_LOCK_SWITCH.equals(key)) {
                Utils.updateLockService(getActivity(), newValue.equals(true));
                return true;
            }

            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            int prefId = -1;
            Class<?> targetClass = null;

            String key = preference.getKey();
            if (PREF_KEY_PREVIEW.equals(key)) {
                targetClass = LockActivity.class;
            } else if (PREF_KEY_HELP.equals(key)) {
                targetClass = CourseActivity.class;
            } else if (PREF_KEY_CUSTOMIZE.equals(key)) {
                prefId = R.xml.pref_inner_customize;
            } else if (PREF_KEY_ADVANCED.equals(key)) {
                prefId = R.xml.pref_inner_advanced;
            } else if (PREF_KEY_INIT_SETTINGS.equals(key)) {
                targetClass = InitSettingsActivity.class;
            }

            // 跳转Activity
            if (targetClass != null) {
                Intent intent = new Intent(getActivity(), targetClass);
                intent.putExtra("from", "settings");
                startActivity(intent);

                return true;
            }

            // 打开二级设置
            if (prefId != -1) {
                openPrefsFragmentInner(prefId, getActivity(), preference.getTitle());

                return true;
            }

            return false;
        }

        private static void openPrefsFragmentInner(int preferencesResId, Activity activity,
                                                   CharSequence title) {
            // 构建二级界面Fragment
            Bundle params = new Bundle();
            params.putInt("pref_id", preferencesResId);

            PrefsFragmentInner targetFragment = new PrefsFragmentInner();
            targetFragment.setArguments(params);

            FragmentTransaction beginTransaction = activity.getFragmentManager().beginTransaction();
            beginTransaction.replace(android.R.id.content, targetFragment);
            beginTransaction.addToBackStack(null);
            beginTransaction.commit();

            // 更新title，与二级界面对应
            activity.setTitle(title);
        }
    }

    /**
     * 设置二级界面。
     *
     * @author FIMH
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefsFragmentInner
            extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            int prefId = getArguments().getInt("pref_id");
            addPreferencesFromResource(prefId);

            init();
        }

        private void init() {
            Preference preference = findPreference(PREF_KEY_AUTO_START);
            if (preference != null) {
                preference.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (PREF_KEY_AUTO_START.equals(preference.getKey())) {
                // 开启/关闭自启动Receiver
                boolean enable = newValue.equals(true);
                Utils.setComponentEnabledSetting(getActivity(), BootReceiver.class, enable);

                return true;
            }
            return false;
        }
    }

}
