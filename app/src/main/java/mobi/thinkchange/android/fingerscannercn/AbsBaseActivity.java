package mobi.thinkchange.android.fingerscannercn;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

/**
 * About、Feedback等带有ActionBar的普通Activity的基类。
 *
 * @author Ke Shang
 * @since 2014-12-09
 */
public abstract class AbsBaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 显示返回按键
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示ActionBar
        if (showActionBar()) {
            actionBar.show();
        } else {
            actionBar.hide();
        }
    }

    /**
     * 是否显示ActionBar。
     *
     * @return true - 默认值
     */
    protected boolean showActionBar() {
        return true;
    }

}
