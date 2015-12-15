package mobi.thinkchange.android.fingerscannercn;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import mobi.thinkchange.android.fingerscannercn.util.TriangleShape;
import mobi.thinkchange.android.fingerscannercn.util.Utils;


/**
 * 初始化设置界面 - 主要是设置开启悬浮窗权限(miui/flyme等rom)。
 *
 * @author Ke Shang
 * @since 2015/03/30
 */
public class InitSettingsActivity
        extends AbsBaseActivity implements View.OnClickListener {

    private static boolean IS_MEIZU = Utils.isMeizu();
    private static boolean IS_XIAOMI = Utils.isXiaomi();

    // 用于数据采集的常量
    private static final int SETTING_TYPE_FLOATING_WINDOW = 1;
    private static final int SETTING_TYPE_AUTO_START = SETTING_TYPE_FLOATING_WINDOW + 1;
    private static final int SETTING_TYPE_SYSTEM_LOCK = SETTING_TYPE_FLOATING_WINDOW + 2;

    private static final int SYSTEM_TYPE_XIAOMI = 1;
    private static final int SYSTEM_TYPE_MEIZU = SYSTEM_TYPE_XIAOMI + 1;
    private static final int SYSTEM_TYPE_OTHER = SYSTEM_TYPE_XIAOMI + 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_settings);

        initTitle();
        initItems();
        findViewById(R.id.init_settings_btn_done).setOnClickListener(this);
    }

    private void initTitle() {
        TextView title = (TextView) findViewById(R.id.init_settings_title);
        if (IS_XIAOMI)
            title.setText(R.string.init_settings_title_xiaomi);
        else if (IS_MEIZU)
            title.setText(R.string.init_settings_title_meizu);
    }

    private void initItems() {
        ViewGroup settingsContainer = (ViewGroup) findViewById(R.id.init_settings_container);
        LayoutInflater layoutInflater = getLayoutInflater();
        // 动态读取内容并添加
        String[] itemTitles = getResources().getStringArray(R.array.init_settings_item_title);
        String[] itemMsgs = getResources().getStringArray(R.array.init_settings_item_message);
        TypedArray itemIdsTypeArray = getResources().obtainTypedArray(R.array.init_settings_item_ids);
        for (int i = 0; i < itemTitles.length; i++) {
            if (i == 0) {
                if (!IS_XIAOMI)       // 仅限于小米
                    continue;
            } else if (i == 1) {        // 仅限于小米和魅族
                if (!IS_XIAOMI && !IS_MEIZU)
                    continue;
            } else if (i == 2) {
                if (IS_MEIZU)         // 除了魅族
                    continue;
            }

            View item = layoutInflater.inflate(R.layout.item_init_setting, settingsContainer, false);

            TextView tv = (TextView) item.findViewById(R.id.item_left_tip);
            ShapeDrawable shapeDrawable = new ShapeDrawable(new TriangleShape());
            shapeDrawable.getPaint().setColor(0xff828282);
            tv.setBackgroundDrawable(shapeDrawable);

            ((TextView) item.findViewById(R.id.item_left_tip)).setText(String.valueOf(settingsContainer.getChildCount() + 1));
            ((TextView) item.findViewById(R.id.item_right_title)).setText(itemTitles[i]);
            ((TextView) item.findViewById(R.id.item_right_msg)).setText(itemMsgs[i]);

            item.setId(itemIdsTypeArray.getResourceId(i, 0));
            item.setOnClickListener(this);

            settingsContainer.addView(item);
        }
        itemIdsTypeArray.recycle();
    }

    @Override
    protected boolean showActionBar() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.init_settings_btn_done:
                int messageId = IS_XIAOMI ? R.string.init_settings_confirm_message_xiaomi : R.string.init_settings_confirm_message_other;
                new MaterialDialog.Builder(this)
                        .content(Html.fromHtml(getString(messageId)))
                        .positiveText(R.string.init_settings_confirm_back)
                        .negativeText(R.string.init_settings_confirm_ok)
                        .negativeColorRes(android.R.color.black)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                onFinish();
                            }
                        })
                        .show();
                break;
            case R.id.init_settings_item_floating_window:       // 小米 - 悬浮窗
                try {
                    Utils.startXiaomiAppPermissionsEditor(this);
                    Utils.showFloatingTip(this, R.string.init_settings_item_floating_window_xiaomi);
                } catch (ActivityNotFoundException e) {
                    sendParams(SETTING_TYPE_FLOATING_WINDOW, SYSTEM_TYPE_XIAOMI);
                }
                break;
            case R.id.init_settings_item_auto_start:            // 自动启动
                if (IS_MEIZU) {          // 魅族
                    try {
                        Utils.startMeizuAutoStart(this);
                        Utils.showFloatingTip(this, R.string.init_settings_item_auto_start_flyme);
                    } catch (ActivityNotFoundException e) {
                        sendParams(SETTING_TYPE_AUTO_START, SYSTEM_TYPE_MEIZU);
                    }
                } else {            // 小米
                    try {
                        Utils.startXiaomiAutoStart(this);
                        Utils.showFloatingTip(this, R.string.init_settings_item_auto_start_xiaomi);
                    } catch (ActivityNotFoundException localException) {
                        sendParams(SETTING_TYPE_AUTO_START, SYSTEM_TYPE_XIAOMI);
                    }
                }

                break;
            case R.id.init_settings_item_system_lock:   // 跳转到系统锁屏设置
                if (!IS_XIAOMI) {    // 其它(无魅族)
                    try {
                        Utils.startGenericSystemLock(this);
                        Utils.showFloatingTip(this, R.string.init_settings_item_system_lock_other);
                    } catch (ActivityNotFoundException e) {
                        sendParams(SETTING_TYPE_SYSTEM_LOCK, SYSTEM_TYPE_OTHER);
                    }
                } else {        // 小米
                    try {
                        Utils.startXiaomiSystemLock(this);
                        Utils.showFloatingTip(this, R.string.init_settings_item_system_lock_xiaomi);
                    } catch (ActivityNotFoundException e) {
                        sendParams(SETTING_TYPE_SYSTEM_LOCK, SYSTEM_TYPE_XIAOMI);
                }
                }
                break;
            default:
                break;
        }

        // 更改左边数字的颜色
        if (v instanceof ViewGroup) {
            View leftTip = v.findViewById(R.id.item_left_tip);
            if (leftTip != null) {
                ShapeDrawable drawable = (ShapeDrawable) leftTip.getBackground();
                if (drawable != null) {
                    drawable.getPaint().setColor(0xff42bd41);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        onFinish();
    }

    private void onFinish() {
        String from = getIntent().getStringExtra("from");
        if ("splash".equals(from) || "course".equals(from)) {
            // 启动主界面
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("from", "init.settings");
            startActivity(intent);
        }

        finish();
    }

    /**
     * 初始设置异常情况采集(相应设置界面无法打开，因为机型众多，所以可能会有找不到的情况；如果找不到，则什么也不做)
     * @param settingType 设置类型:  1 – 悬浮窗，2 – 自动启动，3 – 关闭系统解锁
     * @param systemType 系统类型(冗余): 1 – 小米；魅族 – 2；其他 – 3
     */
    private void sendParams(int settingType, int systemType) {
//        MyApplication myApplication = ((MyApplication) getApplication());
//        Map<String, String> params = myApplication.getStatCommonParams(9);
//        params.put("v14", String.valueOf(settingType));
//        params.put("v15", String.valueOf(systemType));
//        TCUManager3.getInstance().trackGeneric(params);
    }

}
