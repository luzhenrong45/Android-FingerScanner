package mobi.thinkchange.android.fingerscannercn;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * 关于。
 *
 * @author Ke Shang
 * @since 2015/01/27
 */
public class AboutActivity
        extends AbsBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 动态读取版本号
        TextView versionText = (TextView) findViewById(R.id.about_version);
        runOnUiThread(getVersionCode(this, versionText));

        findViewById(R.id.about_btn_website).setOnClickListener(this);
    }

    /**
     * 获取软件版本号
     *
     * @param activity 上下文
     * @param textView 显示版本号的控件
     */
    public static Runnable getVersionCode(final Activity activity, final TextView textView) {
        Runnable getVersion = new Runnable() {

            @Override
            public void run() {
                String appVersionName = null;
                int appVersionCode = 1;
                try {
                    PackageInfo packageInfo =
                            activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                    if (packageInfo != null) {
                        appVersionName = packageInfo.versionName;
                        appVersionCode = packageInfo.versionCode;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                textView.append(appVersionName);
                textView.append(".");
                textView.append(String.valueOf(appVersionCode));
            }
        };
        return getVersion;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.about_btn_website) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_link))));
        }
    }
}
