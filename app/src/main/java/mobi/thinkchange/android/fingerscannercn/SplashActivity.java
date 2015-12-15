package mobi.thinkchange.android.fingerscannercn;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.WindowManager;

import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;

/**
 * 闪屏。
 *
 * @author Ke Shang
 * @since 2015/01/26
 */
public class SplashActivity
        extends AbsBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(R.layout.activity_splash);

        // 标记软件上次使用的时间戳
//        TCUManager3.updateAppUseTimestamp(this);

        // 刷新数据采集id
        ((MyApplication) getApplication()).refreshStatId();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean showActionBar() {
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long period = SystemClock.elapsedRealtime();

                // 公共类库3.12初始化
//                TCUManager3.getInstance().setContext(SplashActivity.this);

                period = SystemClock.elapsedRealtime() - period;

                if (period > 1500L) {
                    goToNextScreen();
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            goToNextScreen();
                        }
                    }, 1500L - period);
                }
            }
        }, 300L);
    }

    @Override
    public void onBackPressed() {
    }

    private void goToNextScreen() {
        // 判断是否是第一次打开软件，是否需要显示使用教程
        SharedPreferencesHelper helper = new SharedPreferencesHelper(this);
        int openTimes = helper.openTimes(0);
        helper.increaseOpenTimes(openTimes);
        openTimes ++;

        boolean initSettingsShown = helper.initSettingsShown();

        // 统计Activity打开
        MyApplication application = (MyApplication) getApplication();
        application.statActivityOpen(1);
        // 统计LockSwitch的状态
        application.statLockSwitchState(true);

        Class<?> targetClass = openTimes > 1 ? MainActivity.class : CourseActivity.class;
        // 判断系统版本
        targetClass = Build.VERSION.SDK_INT < 14 ? LowVersionWarningActivity.class : targetClass;

        // 没显示过初始化设置，则显示
        if(targetClass == MainActivity.class && !initSettingsShown){
            targetClass = InitSettingsActivity.class;
            helper.setInitSettingsShown(true);
        }

        helper.destroy();

        Intent intent = new Intent(this, targetClass);
        intent.putExtras(getIntent());
        intent.putExtra("from", "splash");
        startActivity(intent);
        finish();
    }

}
