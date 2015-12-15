package mobi.thinkchange.android.fingerscannercn.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import mobi.thinkchange.android.fingerscannercn.R;
import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;

public class MyTextClock extends TextView {

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime();
        }
    };
    private SimpleDateFormat mTimeFormat;

    public MyTextClock(Context context) {
        super(context);
        init();
    }

    public MyTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // 格式化时间
        SharedPreferencesHelper helper = new SharedPreferencesHelper(getContext());

        int datePatternIndex = Integer.parseInt(helper.timeFormatIndex("1"));
        String format = getContext().getResources()
                .getStringArray(R.array.time_format_pattern)[datePatternIndex];
        mTimeFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        helper.destroy();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        updateTime();

        getContext().registerReceiver(mReceiver,
                new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // trycatch修复以下FC
        // IllegalArgumentException:Receiver not registered: null (@TCDigitalClock.onDetachedFromWindow:-1) {main}
        // 此FC占指纹解锁总FC的 59.7%(imei)
        // 指纹解锁总FC为5.4%(imei)
        // 上述数据来自：指纹解锁4.6,2014/05/06
        try {
            this.getContext().unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTime() {
        setText(mTimeFormat.format(Calendar.getInstance().getTime()));
    }
}
