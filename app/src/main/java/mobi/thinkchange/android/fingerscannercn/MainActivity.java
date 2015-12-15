package mobi.thinkchange.android.fingerscannercn;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import mobi.thinkchange.android.fingerscannercn.fragment.NavigationDrawerFragment;
import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;
import mobi.thinkchange.android.fingerscannercn.util.Utils;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Toolbar mToolbar;
    private Handler mHandler = new Handler();
    private Class<?> mTargetClass = null;
    private Runnable mOpenActivity = new Runnable() {

        @Override
        public void run() {
            if (mTargetClass != null) {
                Intent intent = new Intent(MainActivity.this, mTargetClass);
                intent.putExtra("from", "main");
                startActivity(intent);
            }
        }
    };

    // 公3成员变量
    private boolean mAdInit = false;
    private Runnable mOpenOwn = new Runnable() {

        @Override
        public void run() {
            showOwn();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTitle = getTitle();

        initToolbar();

        initNavigationDrawer();
        initMainFragment();

        // 透明状态栏及导航栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);

            tintManager.setStatusBarTintResource(R.color.primary_green);

            // 4.4与5.0对fitsSystemWindows的解析不同(使用了自定义的ActionBar，4.4没有效果，5.0有效果)，所以采取下面的方法来解决该问题
            // 因为使用了自定义的ActionBar，所以要手动设置它的padding
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            findViewById(R.id.outer_container).setPadding(0, config.getPixelInsetTop(false), config.getPixelInsetRight(), config.getPixelInsetBottom());
            mNavigationDrawerFragment.getView().setPadding(0, config.getPixelInsetTop(false), config.getPixelInsetRight(), config.getPixelInsetBottom());
        }

        // 执行检查更新
        issueTCCKU();

//        // 统计Activity打开
//        MyApplication application = (MyApplication) getApplication();
//        application.statActivityOpen(3);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
    }

    private void initNavigationDrawer() {
        mNavigationDrawerFragment =
                (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        mNavigationDrawerFragment.closeDrawer();

        // 动态设置Drawer的width，即 屏幕宽度-56dp
        View drawerView = mNavigationDrawerFragment.getView();
        ViewGroup.LayoutParams layoutParams = drawerView.getLayoutParams();
        layoutParams.width =
                Utils.getScreenWidth(this) -
                        getResources().getDimensionPixelSize(R.dimen.navigation_drawer_gap_to_edge_of_screen);
        drawerView.setLayoutParams(layoutParams);
    }

    private void initMainFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new MainFragment())
                .commit();
    }

    /**
     * 初始化并发送CKU请求。
     */
    private void issueTCCKU() {
//        // TCU 3.x 检查更新
//        String from = getIntent().getStringExtra("from");
//        Map<String, String> params = new HashMap<String, String>();
//        if ("tcu3.notifycku".equals(from)) { // notifycku启动
//            params.put("isnotifycku", "1");
//        } else if ("tcu3.notifyopen".equals(from)) { // notifyopen启动
//            params.put("isnotifycku", "2");
//        }
//
//        mAdInit = false; // 设置ad初始化标记
//
//        // 执行检查更新
//        TCUManager3.getInstance().setContext(getApplicationContext());
//        TCUManager3.getInstance().issueTCCKU(this, params);
    }

    @Override
    public void onNavigationDrawerItemSelected(int viewId) {
        // 在这里进行各个Activity的跳转
        int delay = getResources().getInteger(R.integer.ref_drawer_anim_duration);
        mTargetClass = null;
        int v5 = -1;
        switch (viewId) {
            case R.id.drawer_action_feedback:
                mTargetClass = FeedbackActivity.class;
                v5 = 5;
                break;
            case R.id.drawer_action_share:
                Utils.shareText(this, getString(R.string.share_intent_subject),
                        getString(R.string.share_intent_text));
                v5 = 6;
                break;
            case R.id.drawer_action_about_us:
                mTargetClass = AboutActivity.class;
                v5 = 7;
                break;
            case R.id.drawer_action_our_app:
                mHandler.postDelayed(mOpenOwn, delay);
                v5 = 8;
                break;

            default:
                break;
        }

        if (mTargetClass != null)
            mHandler.postDelayed(mOpenActivity, delay);

        if (v5 != -1)
            statMain(v5);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_help) {
            mTargetClass = CourseActivity.class;
            mHandler.postDelayed(mOpenActivity, 50L);

            statMain(4);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void statMain(int v5) {
//        MyApplication myApplication = (MyApplication) getApplication();
//
//        Map<String, String> statParams = myApplication.getStatCommonParams(3);
//        statParams.put("v5", String.valueOf(v5));
//        TCUManager3.getInstance().trackGeneric(statParams);
    }

    private void showOwn() {
//        // 没有可用Own，则跳网页
//        if (mADShowControl == null) {
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_link))));
//        } else {
//            if (!mADShowControl.getOwnControl().isAdAvil()) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_link))));
//            } else {
//                DialogOwn dlgOwn =
//                        new DialogOwn(MainActivity.this, R.style.DialogCKU,
//                                mADShowControl.getOwnControl().getAds(),
//                                mADShowControl.getCkuBaseBean());
//                dlgOwn.setCanceledOnTouchOutside(false);
//                dlgOwn.show();
//
//                // track own广告图标点击(即广告打开),reqtype=2
//                Map<String, String> params =
//                        ParamsHelper.getIAACommonTrackParams("2", mADShowControl.getCkuBaseBean());
//                TCUManager3.getInstance().trackOwn(params);
//            }
//        }
    }

    private void showGift() {
//        // 没有可用Gift，则跳网页
//        if (mADShowControl == null) {
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_link))));
//        } else {
//            if (!mADShowControl.getListControl().isAdAvil()) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_link))));
//            } else {
//
//                DialogList dlgList =
//                        new DialogList(this, R.style.DialogGIFT, mADShowControl.getListControl().getAds(),
//                                mADShowControl.getCkuBaseBean());
//                dlgList.setCanceledOnTouchOutside(false);
//                dlgList.show();
//
//                // track list广告入口点击(即广告打开),reqtype=1
//                Map<String, String> params =
//                        ParamsHelper.getIAACommonTrackParams("1", mADShowControl.getCkuBaseBean());
//                params.put("e1", "2"); // 表示点击icon打开list广告
//                TCUManager3.getInstance().trackList(params);
//            }
//        }
    }

    /**
     * 显示退出对话框
     */
    private void showExit() {
//        DialogExit dlgExit =
//                new DialogExit(this, R.style.DialogCKU, false, mADShowControl != null
//                        ? mADShowControl.getExitControl() : null, mADShowControl != null
//                        ? mADShowControl.getCkuBaseBean() : null);
//        dlgExit.setCanceledOnTouchOutside(false);
//        dlgExit.setOnExitButtonClickListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                if (paramInt == Dialog.BUTTON_POSITIVE) { // 退出
//                    finish();
//                }
//            }
//        });
//        dlgExit.show();
    }

    @Override
    protected void onDestroy() {
        // 统计LockSwitch的状态
        MyApplication application = (MyApplication) getApplication();
        application.statLockSwitchState(false);

        super.onDestroy();
    }

    public static class MainFragment extends Fragment implements View.OnClickListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            init();
        }

        private void init() {
            SharedPreferencesHelper prefHelper = new SharedPreferencesHelper(getActivity());
            boolean startLockScreen = prefHelper.enableLock(true);
            prefHelper.destroy();

            Utils.updateLockService(getActivity(), startLockScreen);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            rootView.findViewById(R.id.main_btn_lock).setOnClickListener(this);
            rootView.findViewById(R.id.main_btn_settings).setOnClickListener(this);
            rootView.findViewById(R.id.main_btn_gift).setOnClickListener(this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            Class<?> targetClass = null;
            int v5 = -1;
            switch (v.getId()) {
                case R.id.main_btn_lock:
                    targetClass = LockActivity.class;
                    v5 = 1;
                    break;
                case R.id.main_btn_settings:
                    targetClass = SettingsActivity.class;
                    v5 = 2;
                    break;
                case R.id.main_btn_gift:
                    ((MainActivity) getActivity()).showGift();
                    v5 = 3;
                    break;
            }

            if (targetClass != null) {
                Intent intent = new Intent(getActivity(), targetClass);
                intent.putExtra("from", "main");
                startActivity(intent);
            }

            // 统计按钮点击
            if (v5 != -1) {
                ((MainActivity) getActivity()).statMain(v5);
            }
        }
    }

}
