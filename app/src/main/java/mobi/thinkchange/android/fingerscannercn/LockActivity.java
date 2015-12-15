package mobi.thinkchange.android.fingerscannercn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.easyandroidanimations.library.Animation;
import com.easyandroidanimations.library.AnimationListener;
import com.easyandroidanimations.library.ExplodeAnimation;
import com.easyandroidanimations.library.FadeInAnimation;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.romainpiel.titanic.library.Titanic;
import com.romainpiel.titanic.library.TitanicTextView;

import de.greenrobot.event.EventBus;
import mobi.thinkchange.android.fingerscannercn.location.LocationEvent;
import mobi.thinkchange.android.fingerscannercn.location.MyLocationService;
import mobi.thinkchange.android.fingerscannercn.util.HomeWatcher;
import mobi.thinkchange.android.fingerscannercn.util.MediaPlayerHelper;
import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;
import mobi.thinkchange.android.fingerscannercn.util.Utils;
import mobi.thinkchange.android.fingerscannercn.weather.HttpUtils;

/**
 * 锁屏界面。
 *
 * @author Ke Shang
 * @since 2015/01/26
 */
public class LockActivity
        extends Activity implements View.OnTouchListener {

    private ShimmerTextView mShimmerTextView;
    private Shimmer mShimmer;

    private TitanicTextView mTitanicTextView;
    private Titanic mTitanic;

    // 指纹扫描动画
    private ObjectAnimator mAnimatorScanning;
    private View mScanningLine;
    private int mScanningCounter;
    private int mScanningLogicCounter;

    // 识别错误动画
    private AnimatorSet mAnimatorWarning;
    private View mWarningFrame;
    private TextView mWarningText;

    // 扫描成功动画
    private AnimatorSet mAnimatorSucceed;
    private AnimatorSet mAnimatorSucceedStep01;
    private View mScanGrid;
    private View mScanFingerprint;
    private View mScanFrame;
    private AnimatorSet mAnimatorSucceedStep02;
    private View mScanCircle;

    // 锁屏设置参数
    private int mLockCondition;
    private boolean mVibrate;
    private boolean mBeep;

    // 扫描错误提示
    private int mScanErrorTimes;
    private Runnable mHideFakeErrorToast = new Runnable() {
        @Override
        public void run() {
            if (mRootView != null) {
                // 双层非空判断，避免解锁时偶尔出现的nullpointer FC
                View fakeErrorToast = mRootView.findViewById(R.id.lock_fake_error_toast);
                if (fakeErrorToast != null) {
                    fakeErrorToast.setVisibility(View.GONE);
                }
            }
        }
    };

    // 扫描提示音
    private MediaPlayerHelper mScanningSound;

    private Handler mHandler = new Handler();

    private Vibrator mVibrator;

    // 百度定位相关
    private MyLocationService mLocationService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationService = ((MyLocationService.LocalBinder) service).getService();

            // 开始定位
            mLocationService.startLocation();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
        }
    };
    private boolean mServiceBinded;
    // root view
    private ViewGroup mRootView;

    // Home按键监听器
    private HomeWatcher mHomeWatcher;

    private String mLockAnimStyle;
    private String mLockTextAnimStyle;

    // 紧急解锁相关
    private static boolean mEmergencyLockPanelShown = false;
    private Button mButtonEmergencyLockAndHelp;
    private Button mButtonEmergencyLock;
    private ViewGroup mEmergencyLockPanel;
    private int mHomeClickCounter = 0;
    private long mHomeClickLastTimestamp = -1;
    private static final int MAX_HOME_CLICK_INTERVAL = 500;

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {                                // 屏幕关闭
                // 结束Shimmer动画
                stopShimmerAnim();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {                          // 屏幕开启
                // 开始Shimmer动画
                startShimmerAnim();
            }
        }
    };

    // 手势操作移除紧急解锁面板
    private GestureDetectorCompat mDetector;
    private GestureDetector.SimpleOnGestureListener mMyGestureDectorListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 紧急解锁面板 - 手势滑动取消
            if(mEmergencyLockPanel != null){
                float targetValue = mEmergencyLockPanel.getWidth() * 1.2f;
                targetValue = velocityX > 0 ? targetValue : -targetValue;
                ObjectAnimator translationX = ObjectAnimator.ofFloat(mEmergencyLockPanel, "translationX", 0f, targetValue);
                translationX.setDuration(200L);
                translationX.setInterpolator(new AccelerateInterpolator());
                translationX.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mEmergencyLockPanel.setVisibility(View.GONE);
                        mEmergencyLockPanel.setTranslationX(0f);
                        mEmergencyLockPanelShown = false;

                        statEmerengyUnlockOperation(3);
                    }
                });
                translationX.start();
            }


            return true;
        }
    };

    private BroadcastReceiver mTelephoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("ring.unlock".equals(action)) {
                // 解锁
                if (mRootView != null && mRootView.isShown()) {
                    mHomeWatcher.stopWatch();
                    getApplicationContext().unregisterReceiver(mScreenReceiver);

                    getApplicationContext().sendBroadcast(new Intent("ring.unlock.succeed"));

                    final WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                    wm.removeView(mRootView);
                    finish();
                }
            }
        }
    };

    private void startShimmerAnim() {
        stopShimmerAnim();

        if ("0".equals(mLockTextAnimStyle)) {
            // 开始Shimmer动画
            mShimmer = new Shimmer();
            mShimmer.start(mShimmerTextView);
        } else {
            // 开始Titanic动画
            mTitanic = new Titanic();
            mTitanic.start(mTitanicTextView);
        }
    }

    private void stopShimmerAnim() {
        // 结束Shimmer动画
        if (mShimmer != null && mShimmer.isAnimating()) {
            mShimmer.cancel();
        }
        mShimmer = null;

        // 结束Titanic动画
        if (mTitanic != null) {
            mTitanic.cancel();
        }
        mTitanic = null;
    }

    private void registerRingReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("ring.unlock");

        getApplicationContext().registerReceiver(mTelephoneReceiver, filter);
    }

    private void registerScreenEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        getApplicationContext().registerReceiver(mScreenReceiver, intentFilter);
    }

    //提取图像Alpha位图
    public static Bitmap getAlphaBitmap(Bitmap mBitmap, int mColor) {
//	    	BitmapDrawable mBitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.enemy_infantry_ninja);
//	    	Bitmap mBitmap = mBitmapDrawable.getBitmap();

        //BitmapDrawable的getIntrinsicWidth（）方法，Bitmap的getWidth（）方法
        //注意这两个方法的区别
        //Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmapDrawable.getIntrinsicWidth(), mBitmapDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas mCanvas = new Canvas(mAlphaBitmap);
        Paint mPaint = new Paint();

        mPaint.setColor(mColor);
        //从原位图中提取只包含alpha的位图
        Bitmap alphaBitmap = mBitmap.extractAlpha();
        //在画布上（mAlphaBitmap）绘制alpha位图
        mCanvas.drawBitmap(alphaBitmap, 0, 0, mPaint);

        return mAlphaBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 参考链接：http://www.tagwith.com/question_262903_android-lock-screen-using-windowmanager-and-home-button
        initFloatingView();

        mHomeWatcher = new HomeWatcher(getApplicationContext());
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                // 最初一直启动LockActivity的用意是不让按了Home或者任务管理器按钮导致状态栏/导航栏隐藏或变色(但是有副作用 - 有时候会出现重复加锁)
//                Intent intent = new Intent(getApplicationContext(), LockActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("from", "homekey");
//                startActivity(intent);

                if (!mEmergencyLockPanelShown) {
                    // 连续点击计数器
                    long currTimeStamp = SystemClock.elapsedRealtime();
                    if (currTimeStamp - mHomeClickLastTimestamp <= MAX_HOME_CLICK_INTERVAL) {
                        mHomeClickCounter++;

                        // 显示紧急解锁面板
                        if (mHomeClickCounter >= 5) {
                            if (mEmergencyLockPanel != null) {
                                mEmergencyLockPanel.setVisibility(View.VISIBLE);
                                mEmergencyLockPanelShown = true;

                                statEmerengyUnlockShown();
                            }

                            // 清0
                            mHomeClickCounter = 0;
                        }
                    } else {
                        mHomeClickCounter = 1;
                    }
                    mHomeClickLastTimestamp = currTimeStamp;
                }
            }

            @Override
            public void onHomeLongPressed() {
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(getApplicationContext(), LockActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("from", "recentapps");
//                        startActivity(intent);
//                    }
//                }, 500L);   // 延迟一定时间，在RecentApps之后再启动
            }
        });
        mHomeWatcher.startWatch();
    }

    private static WindowManager.LayoutParams getFloatingViewParams() {
        WindowManager.LayoutParams rootViewParams = new WindowManager.LayoutParams();
        rootViewParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        rootViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        rootViewParams.format = PixelFormat.RGBA_8888; // 不设置的话，显示会出现问题 - 如花屏
        rootViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        rootViewParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        return rootViewParams;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initFloatingView() {
        //此处如果直接getSystemService(WINDOW_SERVICE);
        //在点击home的时候会文本框会被隐藏
        // 直接添加到WindowManager中的View，不能使用?attr/primary_color形式的颜色设置
        final WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = (ViewGroup) inflater.inflate(R.layout.activity_lock,
                null);

        // 向WindowManager中添加View的参数
        wm.addView(mRootView, getFloatingViewParams());

        mDetector = new GestureDetectorCompat(getApplicationContext(), mMyGestureDectorListener);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 设置监听器
        mRootView.findViewById(R.id.lock_scan_fingerprint).setOnTouchListener(this);
        mScanningLine = mRootView.findViewById(R.id.lock_scan_scaning_line);
        mWarningFrame = mRootView.findViewById(R.id.lock_warning_frame);
        mWarningText = (TextView) mRootView.findViewById(R.id.lock_warning_text);

        mScanGrid = mRootView.findViewById(R.id.lock_scan_grid);
        mScanFrame = mRootView.findViewById(R.id.lock_scan_frame);
        mScanFingerprint = mRootView.findViewById(R.id.lock_scan_fingerprint);

        mScanCircle = mRootView.findViewById(R.id.lock_scan);

        mVibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);

        mShimmerTextView = (ShimmerTextView) mRootView.findViewById(R.id.lock_shimmer_text);
        mTitanicTextView = (TitanicTextView) mRootView.findViewById(R.id.lock_titanic_text);

        // 紧急解锁
        mEmergencyLockPanel = (ViewGroup) mRootView.findViewById(R.id.lock_emergency_panel);
        mButtonEmergencyLockAndHelp = (Button) mRootView.findViewById(R.id.lock_emergency_unlock_and_help);
        mButtonEmergencyLock = (Button) mRootView.findViewById(R.id.lock_emergency_unlock);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.lock_emergency_unlock_and_help:
                        Intent intentHelp = new Intent(getApplicationContext(), CourseActivity.class);
                        intentHelp.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intentHelp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentHelp);
                        doNormalUnlock();

                        statEmerengyUnlockOperation(1);

                        break;
                    case R.id.lock_emergency_unlock:
                        doNormalUnlock();

                        statEmerengyUnlockOperation(2);

                        break;
                }
            }
        };
        mButtonEmergencyLockAndHelp.setOnClickListener(onClickListener);
        mButtonEmergencyLock.setOnClickListener(onClickListener);
        mEmergencyLockPanelShown = false;

        // 手势监控
        mEmergencyLockPanel.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {       // 实际的面板
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);

                return true;
            }
        });

        // 设置文字字体
        // 加载字体
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/digital-7.ttf");
        ((TextView) mRootView.findViewById(R.id.lock_clock)).setTypeface(typeface);
        ((TextView) mRootView.findViewById(R.id.lock_date)).setTypeface(typeface);
        ((TextView) mRootView.findViewById(R.id.lock_shimmer_text)).setTypeface(typeface);
        ((TextView) mRootView.findViewById(R.id.lock_temperature)).setTypeface(typeface);
        mShimmerTextView.setTypeface(typeface);
        mTitanicTextView.setTypeface(typeface);
        mWarningText.setTypeface(typeface);

        // 测试：动态修改图片颜色
//        int newColor = getResources().getColor(R.color.material_blue_500);
//        ImageView iv = (ImageView) mRootView.findViewById(R.id.lock_scan_fingerprint);
//        Bitmap fingerprintBmp = ((BitmapDrawable) getResources().getDrawable(R.drawable.lock_scan_fingerprint)).getBitmap();
//        Bitmap anotherBmp = getAlphaBitmap(fingerprintBmp, newColor);
//        iv.setImageBitmap(anotherBmp);
//        fingerprintBmp.recycle();

        // 启动动画
        new FadeInAnimation(mRootView.findViewById(R.id.lock_root)).animate();

        // 初始化扫描音
        mScanningSound = new MediaPlayerHelper(this, R.raw.dot);

        // 重置错误次数
        mScanErrorTimes = 0;

        // 统计Activity打开(只统计用户手动进入)
        String from = getIntent().getStringExtra("from");
        if ("main".equals(from)) {
            MyApplication application = (MyApplication) getApplication();
            application.statActivityOpen(5);
        }

        // 这里先延时初始化一下，避免解锁成功/失败动画卡顿
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                TCUManager3.getInstance().setContext(getApplicationContext());
            }
        }, 500L);

        // 注册EventBus
        EventBus.getDefault().register(this);

        mServiceBinded = false;
        SharedPreferencesHelper helper = new SharedPreferencesHelper(this);
        // 初始锁屏设置
        mLockCondition = helper.lockCondition(3);
        mVibrate = helper.vibrate(true);
        mBeep = helper.beep(true);
        mLockAnimStyle = helper.lockAnimStyle("2");
        mLockTextAnimStyle = helper.lockTextAnimStyle("0");
        if (helper.weather(true)) {
            // 绑定定位服务
            Intent intentLocation = new Intent(this, MyLocationService.class);
            bindService(intentLocation, mServiceConnection, BIND_AUTO_CREATE);

            mServiceBinded = true;
        }
        helper.destroy();

        // 初始化解锁文字控件
        mShimmerTextView.setVisibility(View.GONE);
        mTitanicTextView.setVisibility(View.GONE);
        int color = getResources().getColor(R.color.primary_green);
        if ("0".equals(mLockTextAnimStyle)) {
            mShimmerTextView.setVisibility(View.VISIBLE);
            int colorLight = Color.argb(120, Color.red(color), Color.green(color), Color.blue(color));
            mShimmerTextView.setPrimaryColor(colorLight);
            mShimmerTextView.setReflectionColor(color);
        } else {
            mTitanicTextView.setVisibility(View.VISIBLE);
        }

        // Shimmer动画
        registerScreenEventReceiver();
        if (Utils.isScreenOn(this)) {
            startShimmerAnim();
        }

        // 电话监听器
        registerRingReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mScanningSound.release();
        mScanningSound = null;

        // 释放EventBus
        EventBus.getDefault().unregister(this);

        getApplicationContext().unregisterReceiver(mTelephoneReceiver);

        if (mServiceBinded) {
            unbindService(mServiceConnection);

            mServiceBinded = false;
        }
        mLocationService = null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onResume() {
        super.onResume();

        // 刷新flag，解锁系统默认锁屏(测试发现，只能使用activity的window来实现)
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // 隐藏导航栏与状态栏
        int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (mRootView != null)
            mRootView.setSystemUiVisibility(systemUiVisibility);
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);

        if (mLocationService != null) {
            mLocationService.startLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationService != null) {
            mLocationService.stopLocation();
        }
    }

    /**
     * 定义事件处理方法：具体有四种类型：PostThread、MainThread、BackgroundThread、Async
     *
     * @param event
     */
    public void onEventMainThread(LocationEvent event) {
        HttpUtils httpUtils = new HttpUtils(this);
        httpUtils.getWeatherData(event.getText(), ((TextView) mRootView.findViewById(R.id.lock_weather_separator)), ((TextView) mRootView.findViewById(R.id.lock_temperature)));

        if (mLocationService != null) {
            mLocationService.stopLocation();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.lock_scan_fingerprint) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startScanningAnim();
                    return true;
                case MotionEvent.ACTION_UP:
                    cancelScanningAnim();

                    if (mScanningLogicCounter != mLockCondition) {      // 解锁失败
                        startWarningAnim();

                        // 增加解锁失败次数
                        mScanErrorTimes++;

                        // 错误3次或3的倍数提示用户正确解锁次数
                        if (mScanErrorTimes > 0 && (mScanErrorTimes % 3 == 0)) {
                            final TextView tv = (TextView) mRootView.findViewById(R.id.lock_fake_error_toast);
                            tv.setVisibility(View.VISIBLE);
                            tv.setText("【" + mLockCondition + "】次震动/滴滴音后放开手指解锁");

                            mHandler.removeCallbacks(mHideFakeErrorToast);
                            mHandler.postDelayed(mHideFakeErrorToast, 3000L);
                        }

                        // 统计解锁失败的次数
                        statLockFailed(mScanErrorTimes);
                    } else {                                                // 解锁成功
                        startSucceedAnim();

                        // 禁止用户再次扫描指纹
                        v.setOnTouchListener(null);

                        // 统计解锁成功前失败的次数
                        statLockSucceed(mScanErrorTimes);
                    }
                    return true;
                default:
                    break;
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startScanningAnim() {
        // lazy initialization
        if (mAnimatorScanning == null) {
            mAnimatorScanning = ObjectAnimator.ofFloat(mScanningLine, "translationY", 0, Utils.dip2px(this, 255));
            mAnimatorScanning.setRepeatMode(ValueAnimator.REVERSE);
            mAnimatorScanning.setRepeatCount(ValueAnimator.INFINITE);
            mAnimatorScanning.setDuration(300L);
            mAnimatorScanning.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    // 计数器自增
                    if (mScanningCounter % 2 == 0) {
                        mScanningLogicCounter++;

                        // 加上非空判断，解决某些设备上此处FC的问题
                        if (mVibrate && mVibrator != null)
                            mVibrator.vibrate(30L);

                        if (mBeep && mScanningSound != null)
                            mScanningSound.start();
                    }
                    mScanningCounter++;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    // 计数器重置
                    mScanningCounter = 0;
                    mScanningLogicCounter = 0;
                }
            });
        }
        // 启动动画
        if (!mAnimatorScanning.isRunning()) {
            mScanningLine.setVisibility(View.VISIBLE);
            mAnimatorScanning.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void cancelScanningAnim() {
        // 取消动画
        mAnimatorScanning.cancel();
        mScanningLine.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startWarningAnim() {
        // lazy initialization
        if (mAnimatorWarning == null) {
            mAnimatorWarning = new AnimatorSet();
            mAnimatorWarning.setDuration(1000L);

            PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("alpha", 1, 0, 1, 0);
            ObjectAnimator frameAnimator = ObjectAnimator.ofPropertyValuesHolder(mWarningFrame, pvh);
            ObjectAnimator textAnimator = ObjectAnimator.ofPropertyValuesHolder(mWarningText, pvh);
            mAnimatorWarning.playTogether(frameAnimator, textAnimator);
        }

        // 启动动画
        if (!mAnimatorWarning.isRunning()) {
            mAnimatorWarning.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startSucceedAnim() {
        // lazy initialization
        if (mAnimatorSucceedStep01 == null) {
            PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("scaleY", 0);
            ObjectAnimator frameAnimator = ObjectAnimator.ofPropertyValuesHolder(mScanFrame, pvh);
            ObjectAnimator gridAnimator = ObjectAnimator.ofPropertyValuesHolder(mScanGrid, pvh);
            ObjectAnimator fingerprintAnimator = ObjectAnimator.ofPropertyValuesHolder(mScanFingerprint, pvh);

            mAnimatorSucceedStep01 = new AnimatorSet();
            mAnimatorSucceedStep01.setDuration(150L);
            mAnimatorSucceedStep01.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimatorSucceedStep01.playTogether(frameAnimator, gridAnimator, fingerprintAnimator);
        }

        if (mAnimatorSucceedStep02 == null) {
            ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(mScanCircle, "rotation", 360.0f * 7);
            rotateAnimator.setDuration(500);
            rotateAnimator.setInterpolator(new AccelerateInterpolator());

            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 2.1f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 2.1f);
            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mScanCircle, scaleX, scaleY);
            scaleAnimator.setStartDelay(200);
            scaleAnimator.setDuration(300);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mScanCircle, "alpha", 0.0f);
            alphaAnimator.setDuration(200);
            alphaAnimator.setStartDelay(300);

            mAnimatorSucceedStep02 = new AnimatorSet();
            mAnimatorSucceedStep02.playTogether(rotateAnimator, scaleAnimator, alphaAnimator);
            mAnimatorSucceedStep02.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {        // 播放最终的Explode动画
                    // 保证操作的View的parentview为viewgroup类型，否则会产生类似下面的错误
                    // FC: java.lang.ClassCastException: android.view.ViewRootImpl cannot be cast to android.view.ViewGroup
                    ExplodeAnimation ea = new ExplodeAnimation(mRootView.findViewById(R.id.lock_inner_root));

                    // 解锁成功动画
                    int explodeMatrix = ExplodeAnimation.MATRIX_2X2;        // 默认
                    if ("0".equals(mLockAnimStyle)) {
                        explodeMatrix = ExplodeAnimation.MATRIX_1X2;
                    } else if ("1".equals(mLockAnimStyle)) {
                        explodeMatrix = ExplodeAnimation.MATRIX_2X1;
                    } else if ("3".equals(mLockAnimStyle)) {
                        explodeMatrix = ExplodeAnimation.MATRIX_3X3;
                    }

                    ea.setExplodeMatrix(explodeMatrix);
                    ea.setListener(new AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            doNormalUnlock();
                        }
                    });
                    ea.animate();
                }
            });
        }

        if (mAnimatorSucceed == null) {
            mAnimatorSucceed = new AnimatorSet();
            mAnimatorSucceed.playSequentially(mAnimatorSucceedStep01, mAnimatorSucceedStep02);
        }

        // 启动动画
        if (!mAnimatorSucceed.isRunning()) {
            mAnimatorSucceed.start();
        }
    }

    private void doNormalUnlock() {
        if (mRootView != null && mRootView.isShown()) {
            mHomeWatcher.stopWatch();
            getApplicationContext().unregisterReceiver(mScreenReceiver);

            getApplicationContext().sendBroadcast(new Intent("normal.unlock"));

            final WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mRootView);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void statLockSucceed(int v7) {
//        // 设置上限：100条/逻辑天
//        if (!AnaHelper.anaExceeds(getApplicationContext(), 7)) {
//            MyApplication myApplication = (MyApplication) getApplication();
//
//            Map<String, String> statParams = myApplication.getStatCommonParams(5);
//            statParams.put("v7", String.valueOf(v7));
//            TCUManager3.getInstance().setContext(getApplicationContext());
//            TCUManager3.getInstance().trackGeneric(statParams);
//        }
    }

    private void statLockFailed(int v8) {
//        // 设置上限：100条/逻辑天
//        if (!AnaHelper.anaExceeds(getApplicationContext(), 8)) {
//            MyApplication myApplication = (MyApplication) getApplication();
//
//            Map<String, String> statParams = myApplication.getStatCommonParams(5);
//            statParams.put("v8", String.valueOf(v8));
//            TCUManager3.getInstance().setContext(getApplicationContext());
//            TCUManager3.getInstance().trackGeneric(statParams);
//        }
    }

    /**
     * 紧急解锁出现时发送(连续按5次home按键)，取值为1
     */
    private void statEmerengyUnlockShown() {
//        MyApplication myApplication = ((MyApplication) getApplication());
//        Map<String, String> params = myApplication.getStatCommonParams(10);
//        params.put("v16", "1");
//        TCUManager3.getInstance().trackGeneric(params);
    }

    /**
     * 紧急解锁的操作情况统计。
     * @param operation 1 – 查看帮助，2 – 直接解锁，3 – 手势滑动取消
     */
    private void statEmerengyUnlockOperation(int operation) {
//        Object object = new Object();
//                MyApplication myApplication = ((MyApplication) getApplication());
//        Map<String, String> params = myApplication.getStatCommonParams(10);
//        params.put("v17", String.valueOf(operation));
//        TCUManager3.getInstance().trackGeneric(params);
    }
}
