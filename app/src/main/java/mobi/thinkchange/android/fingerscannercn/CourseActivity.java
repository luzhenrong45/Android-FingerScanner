package mobi.thinkchange.android.fingerscannercn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import mobi.thinkchange.android.fingerscannercn.dialog.CourseFinishDialog;
import mobi.thinkchange.android.fingerscannercn.dialog.CourseStartDialog;
import mobi.thinkchange.android.fingerscannercn.util.MediaPlayerHelper;
import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;
import mobi.thinkchange.android.fingerscannercn.util.Utils;


/**
 * 使用教程。
 *
 * @author Ke Shang
 * @since 2015/01/26
 */
public class CourseActivity
        extends AbsBaseActivity implements View.OnTouchListener, View.OnClickListener {

    private TextSwitcher mCourseTip;
    private TextSwitcher mCourseCounter;

    // 指纹扫描动画
    private ObjectAnimator mAnimatorScanning;
    private View mScanningLine;
    private int mScanningCounter;
    private int mScanningLogicCounter;

    private Vibrator mVibrator;

    private Handler mHandler = new Handler();
    private Runnable mRunnableResetCounter = new Runnable() {
        public void run() {
            mCourseCounter.setText(" ");
        }
    };
    private Runnable mRunnableCourseHand = new Runnable() {
        @Override
        public void run() {
            initCourseHand();
            mHandler.postDelayed(this, 6000L);
        }
    };

    private PopupWindow mPopupWindow;
    private ObjectAnimator mHandAnimator;

    private MediaPlayerHelper mScanningSound;

    /**
     * 当前扫描模式<br/>
     * 0 --- stage 1，用户练习模式<br/>
     * 1 --- stage 2，小任务模式
     */
    private int scanningMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 显示提示对话框
        CourseStartDialog dialog = new CourseStartDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mHandler.postDelayed(mRunnableCourseHand, 3000L);
            }
        });
        dialog.show();

        initTextSwitcher();

        // 扫描监听器
        findViewById(R.id.course_scan_fingerprint).setOnTouchListener(this);
        findViewById(R.id.course_skip).setOnClickListener(this);

        mScanningLine = findViewById(R.id.course_scan_scaning_line);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // 初始化扫描音
        mScanningSound = new MediaPlayerHelper(this, R.raw.dot);

        // 统计Activity打开
        MyApplication application = (MyApplication) getApplication();
        application.statActivityOpen(2);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initCourseHand() {
        if (mPopupWindow == null) {
            // 创建PopupWindow
            View view = LayoutInflater.from(this).inflate(R.layout.course_hand, null);
            mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);

            PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("alpha", 1, 0, 1, 0, 1, 0);
            mHandAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvh);
            mHandAnimator.setDuration(2000L);
            mHandAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isFinishing()) {
                        mPopupWindow.dismiss();
                    }
                }
            });
        }
        mPopupWindow.showAsDropDown(findViewById(R.id.course_scan_fingerprint), Utils.dip2px(this, 38), Utils.dip2px(this, -110));
        mHandAnimator.start();
    }

    private void initTextSwitcher() {
        // 提示文字切换
        mCourseTip = (TextSwitcher) findViewById(R.id.course_tip);
        final ViewSwitcher.ViewFactory courseTipFactory = new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                Context context = CourseActivity.this;
                TextView tv = new TextView(context);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(22);
                tv.setTextColor(Color.WHITE);
                tv.getPaint().setAntiAlias(true);
                return tv;
            }
        };
        mCourseTip.setFactory(courseTipFactory);
        mCourseTip.setInAnimation(this, android.R.anim.fade_in);
        mCourseTip.setOutAnimation(this, android.R.anim.fade_out);
        mCourseTip.setText("请伸出“任意”手指\n按住下方中间的指纹图案");

        // 计数器
        mCourseCounter = (TextSwitcher) findViewById(R.id.course_counter);
        final ViewSwitcher.ViewFactory courseCounterFactory = new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                Context context = CourseActivity.this;
                TextView tv = new TextView(context);
                tv.setTextSize(48);
                tv.setTextColor(Color.WHITE);
                tv.getPaint().setAntiAlias(true);
                return tv;
            }
        };
        mCourseCounter.setFactory(courseCounterFactory);
        mCourseCounter.setInAnimation(this, android.R.anim.slide_in_left);
        mCourseCounter.setOutAnimation(this, android.R.anim.slide_out_right);
        mHandler.post(mRunnableResetCounter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startScanningAnim() {
        // lazy initialization
        if (mAnimatorScanning == null) {
            mHandler.removeCallbacks(mRunnableCourseHand);

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

                        mVibrator.vibrate(30L);
                        mScanningSound.start();

                        mCourseCounter.setText(String.valueOf(mScanningLogicCounter));
                        // 如果是stage 1并且用户手指正在扫描，则提示用户可以放开手指
                        if (scanningMode == 0 && mScanningLogicCounter == 1) {
                            mCourseTip.setText("感觉到震动或听到滴滴音\n请放开手指");                            // 更改提示文字
                        }
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

            mHandler.removeCallbacks(mRunnableResetCounter);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void cancelScanningAnim() {
        // 取消动画
        mAnimatorScanning.cancel();
        mScanningLine.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        statCourse(3);
        onFinish();
    }

    @Override
    protected boolean showActionBar() {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.course_scan_fingerprint) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startScanningAnim();
                    return true;
                case MotionEvent.ACTION_UP:
                    cancelScanningAnim();

                    updateScanningMode();

                    // 延迟1秒隐藏Counter
                    mHandler.postDelayed(mRunnableResetCounter, 1000L);
                    return true;
                default:
                    break;
            }
        }

        return false;
    }

    private void updateScanningMode() {
        // 如果是stage 1，则转为stage 2
        if (scanningMode == 0) {
            scanningMode = 1;

            // 更改提示文字
            mCourseTip.setText("看来你已经学会扫描了，:)");
            mCourseTip.postDelayed(new Runnable() {
                public void run() {
                    mCourseTip.setText("如何解锁呢？\n扫描【3】次后放开手指");
                }
            }, 2000L);
        } else if (scanningMode == 1) {
            if (mScanningLogicCounter == 3) {
                mCourseTip.setText("恭喜！\n成功扫描【3】次(震动/滴滴音)");

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // dialog显示教程结束
                        CourseFinishDialog dialog = new CourseFinishDialog(CourseActivity.this);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                statCourse(2);
                                onFinish();
                            }
                        });
                        dialog.show();
                    }
                }, 500L);
            } else {
                mCourseTip.setText("你扫描了" + mScanningLogicCounter + "次(震动/滴滴音)\n【3】次后放开手指才能解锁");
            }
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRunnableCourseHand);
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }

        mScanningSound.release();
        mScanningSound = null;

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.course_skip) {
            statCourse(1);
            onFinish();
        }
    }

    private void onFinish() {
        String from = getIntent().getStringExtra("from");
        if ("splash".equals(from)) {
            Intent intent = new Intent(this, InitSettingsActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra("from", "course");
            startActivity(intent);

            // 标记已经显示过初始化设置
            SharedPreferencesHelper helper = new SharedPreferencesHelper(this);
            helper.setInitSettingsShown(true);
            helper.destroy();
        }
        finish();
    }

    private void statCourse(int v4) {
//        MyApplication myApplication = (MyApplication) getApplication();
//
//        Map<String, String> statParams = myApplication.getStatCommonParams(2);
//        statParams.put("v4", String.valueOf(v4));
//        TCUManager3.getInstance().trackGeneric(statParams);
    }
}
