package mobi.thinkchange.android.fingerscannercn.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

import mobi.thinkchange.android.fingerscannercn.R;

public class VersionLowDialog
        extends Dialog implements View.OnClickListener {
    private Context context;
    private Button okButton;

    private OnClickListener mOnClickListener;

    public VersionLowDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_low_version);
        setupViews();
        init();
    }

    private void setupViews() {
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnClickListener != null) {
                    mOnClickListener.onOKClicked();
                }
            }
        });

        // 下方的四个图标
        findViewById(R.id.low_flashlight).setOnClickListener(this);
        findViewById(R.id.low_qrcode).setOnClickListener(this);
        findViewById(R.id.low_fbi).setOnClickListener(this);
        findViewById(R.id.low_crystaltimer).setOnClickListener(this);
    }

    private void init() {
        WindowManager m = ((Activity) context).getWindowManager();
        Display d = m.getDefaultDisplay();
        LayoutParams p = getWindow().getAttributes();
        p.width = (int) (d.getWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        getWindow().setAttributes(p);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mOnClickListener != null) {
            mOnClickListener.onIconClicked(v.getId());
        }
    }

    public static interface OnClickListener {

        /**
         * 点击了“确定”按钮。
         */
        void onOKClicked();

        /**
         * 点击了下方的4个icon中的任何一个。
         *
         * @param id icon对应的id
         */
        void onIconClicked(int id);
    }

}
