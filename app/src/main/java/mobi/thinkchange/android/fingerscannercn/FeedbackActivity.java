package mobi.thinkchange.android.fingerscannercn;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;
import mobi.thinkchange.android.fingerscannercn.util.Utils;

/**
 * 关于。
 *
 * @author Ke Shang
 * @since 2015/01/27
 */
public class FeedbackActivity
        extends AbsBaseActivity implements View.OnClickListener {

    private Button mBtnFeedback;
    private EditText mETContact;
    private EditText mETContent;

    private Handler mHandler = new Handler();

    private SharedPreferencesHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mHelper = new SharedPreferencesHelper(this);

        mBtnFeedback = (Button) findViewById(R.id.feedback_button);
        mETContact = (EditText) findViewById(R.id.feedback_contact);
        mETContent = (EditText) findViewById(R.id.feedback_content);

        mBtnFeedback.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 还原联系方式
        mETContact.setText(mHelper.feedBackContact());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 保存联系方式
        mHelper.setFeedBackContact(mETContact.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feedback_button:
                // 检查网络是否可用
                if (Utils.isNetworkAvailable(this)) {
                    // 校验
                    if (checkInput()) {     // 成功
                        // 发送数据采集
                        sendParams();

                        // 成功提示
                        Toast.makeText(this, getString(R.string.feedback_successful), Toast.LENGTH_SHORT).show();

                        // 禁止再次提交
                        mBtnFeedback.setEnabled(false);

                        // 2.5秒后自动退出Activity
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                finish();
                            }
                        }, 2000L);
                    }
                } else {
                    // 提示
                    Toast.makeText(this, getString(R.string.network_not_avaliable), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    // 输入校验
    private boolean checkInput() {
        String warning = null;
        String contactStr = mETContact.getText().toString();
        String contentStr = mETContent.getText().toString();
        if (contactStr.length() > 30) {
            warning = getResources().getString(R.string.warning_feedback_contact_length_30);
        } else if (contentStr == null || contentStr.equals("")) {
            warning = getResources().getString(R.string.warning_feedback_content_null);
        } else if (contentStr.length() > 100) {
            warning = getResources().getString(R.string.warning_feedback_content_length_100);
        }
        if (warning != null) {
            // 提示
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // 发送统计到的数据到公共类库
    private void sendParams() {
//        MyApplication myApplication = ((MyApplication) getApplication());
//        Map<String, String> params = myApplication.getStatCommonParams(7);
//        params.put("v10", mETContact.getText().toString());// 反馈联系方式
//        params.put("v11", mETContent.getText().toString());// 反馈内容
//        TCUManager3.getInstance().trackGeneric(params);
    }
}
