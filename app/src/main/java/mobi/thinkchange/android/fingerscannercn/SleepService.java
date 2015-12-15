package mobi.thinkchange.android.fingerscannercn;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class SleepService extends Service {

    /**
     * 电话监听器
     */
    private TelephonyManager mTelephonyManager;

    // Keyguard相关
    private KeyguardManager mKeyguardManager;
    private KeyguardManager.KeyguardLock mKeyguardLock;

    private boolean mNormalStop;

    private static boolean mPreviousLocked = false;

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {                                // 屏幕关闭
                // 通话或来电期间不启动锁屏
                int callState = MyTelListener.getInstance(SleepService.this).callState;
                if (callState != TelephonyManager.CALL_STATE_OFFHOOK
                        && callState != TelephonyManager.CALL_STATE_RINGING) {                            // 解决接电话时会锁屏的问题，TelephonyManager.CALL_STATE_RINGING
                    startLockActivity();
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {                        // 屏幕开启
            }
        }
    };

    private BroadcastReceiver mUnlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if("normal.unlock".equals(intent.getAction())){
                mPreviousLocked = false;
            } else if("ring.unlock.succeed".equals(intent.getAction())){
                mPreviousLocked = true;
            }
        }
    };

    private void registerUnlockReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("normal.unlock");
        filter.addAction("ring.unlock.succeed");

        registerReceiver(mUnlockReceiver, filter);
    }

    private static PendingIntent getPendingIntent(Context context, boolean oneshot) {
        Intent intent = new Intent(context, SleepService.class);
        intent.putExtra("reason", "disable.keyguard");
        if(oneshot)
            intent.putExtra("from", "alarm.oneshot");
        else
            intent.putExtra("from", "alarm");
        PendingIntent scheduledIntent = PendingIntent.getService(context, R.string.app_name, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return scheduledIntent;
    }

    private void startLockActivity() {
        Intent intentFP = new Intent(SleepService.this,
                LockActivity.class);
        intentFP.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(SleepService.this, 0,
                intentFP, 0);
        try {
            pi.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化
        mPreviousLocked = false;

        mNormalStop = false;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(MyTelListener.getInstance(this),
                PhoneStateListener.LISTEN_CALL_STATE);

        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock(getPackageName());

        registerScreenEventReceiver();

        disableKeyguard();

        startRepeatingSchedule();

        registerUnlockReceiver();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        startImmediateSchedule();

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        mPreviousLocked = false;

        // 对应的，反注册
        mTelephonyManager.listen(MyTelListener.getInstance(this),
                PhoneStateListener.LISTEN_NONE);
        mTelephonyManager = null;

        reenableKeyguard();
        mKeyguardLock = null;
        mKeyguardManager = null;

        unregisterReceiver(mScreenReceiver);

        unregisterReceiver(mUnlockReceiver);

//        Toast.makeText(this, "SleepService being killed.", Toast.LENGTH_SHORT).show();

        if (!mNormalStop)
            startImmediateSchedule();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String reason = intent.getStringExtra("reason");

            if ("disable.keyguard".equals(reason)) {
                if ((flags & START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY) {
//                    Toast.makeText(this, "SleepService being recreated.", Toast.LENGTH_SHORT).show();
                } else {    // 非系统自动启动
                    String from = intent.getStringExtra("from");
                    if("alarm.oneshot".equals(from)){   // 统计onTaskRemoved事件
                        // 统计用户从最近运行的应用列表中关闭程序的情况
                        statTaskRemoved();
                    }
                }

            } else if ("stop.service".equals(reason)) {
                removeSchedule();
                stopSelf();

                mNormalStop = true;
            }
        }

        return START_REDELIVER_INTENT;
    }

    private void disableKeyguard() {
        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {      // FIXME 待理解KeyguardRestrictedInputMode的真正含义
            mKeyguardLock.disableKeyguard();
        }
    }

    private void reenableKeyguard() {
        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
            mKeyguardLock.reenableKeyguard();
        }
    }

    private void registerScreenEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        registerReceiver(mScreenReceiver, intentFilter);
    }

    private void startImmediateSchedule() {
        // 解决启动Service的Task被Remove后，Service也随着被Remove的问题
        startSchedule(1, false);
    }

    private void startRepeatingSchedule() {
        startSchedule(60, true);
    }

    /**
     * 原理参考：<a href="http://www.vogella.com/tutorials/AndroidTaskScheduling/article.html">Scheduling of tasks in Android with the AlarmManager and the JobScheduler - Tutorial</a>
     */
    private void startSchedule(int delayInSecond, boolean repeating) {
        int delay = delayInSecond * 1000; // 60 seconds
        // Using an Alarm to restart the Service, which should be reliable in 4.4.1 / 4.4.2.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (repeating)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + delay, delay, getPendingIntent(getApplicationContext(), false));   // 不唤醒设备
        else
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + delay, getPendingIntent(getApplicationContext(), true));  // one shot
    }

    private void removeSchedule() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(getApplicationContext(), false));
    }

    private void statTaskRemoved() {
//        MyApplication myApplication = (MyApplication) getApplication();
//
//        Map<String, String> statParams = myApplication.getStatCommonParams(6);
//        statParams.put("v9", "1");
//        TCUManager3.getInstance().setContext(getApplicationContext());
//        TCUManager3.getInstance().trackGeneric(statParams);
    }

    /**
     * 电话状态监听器。 <li>TelephonyManager.CALL_STATE_OFFHOOK --- 打出电话或通话中</li> <li>
     * TelephonyManager.CALL_STATE_RINGING --- 来电未接通时</li>
     *
     * @author Ke Shang
     */
    public static class MyTelListener extends PhoneStateListener {

        private static MyTelListener instance;
        public int callState = TelephonyManager.CALL_STATE_IDLE;

        private Context mContext;

        private MyTelListener() {
        }

        private MyTelListener(Context context){
            mContext = context.getApplicationContext();
        }

        public static MyTelListener getInstance(Context context) {
            if (instance == null) {
                instance = new MyTelListener(context);
            }

            return instance;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            callState = state;

            if(callState != TelephonyManager.CALL_STATE_IDLE){
                mContext.sendBroadcast(new Intent("ring.unlock"));
            } else {
                if(mPreviousLocked) {
                    Intent intentFP = new Intent(mContext,
                            LockActivity.class);
                    intentFP.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pi = PendingIntent.getActivity(mContext, 0,
                            intentFP, 0);
                    try {
                        pi.send();

                        mPreviousLocked = false;
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}