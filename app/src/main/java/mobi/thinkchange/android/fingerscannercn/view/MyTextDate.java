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

public class MyTextDate extends TextView {
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime();
        }
    };
    private SimpleDateFormat mDateFormat;

    public MyTextDate(Context context) {
        super(context);

        init();
    }

    public MyTextDate(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MyTextDate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        // 格式化日期
        SharedPreferencesHelper helper = new SharedPreferencesHelper(getContext());

        int datePatternIndex = Integer.parseInt(helper.dateFormatIndex("1"));
        String format = getContext().getResources()
                .getStringArray(R.array.date_format_pattern)[datePatternIndex];
        mDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        helper.destroy();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        updateTime();

        getContext().registerReceiver(mReceiver,
                new IntentFilter(Intent.ACTION_DATE_CHANGED));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // trycatch修复以下FC
        // IllegalArgumentException:Receiver not registered: null(@TCDateView.onDetachedFromWindow:-1) {main}
        // 此FC占指纹解锁总FC的 xxx%(imei)
        // 指纹解锁总FC为0.6%(imei)
        // 上述数据来自：指纹解锁4.8,2014/06/16
        try {
            getContext().unregisterReceiver(mReceiver);
        } catch (Exception e) {
        }
    }

    private void updateTime() {
        setText(mDateFormat.format(Calendar.getInstance().getTime()));
    }
}