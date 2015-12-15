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

public class CourseFinishDialog
        extends Dialog {
    private Context context;
    private Button okButton;

    public CourseFinishDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_course_finish);
        setupViews();
        init();
    }

    private void setupViews() {
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void init() {
        WindowManager m = ((Activity) context).getWindowManager();
        Display d = m.getDefaultDisplay();
        LayoutParams p = getWindow().getAttributes();
        p.width = (int) (d.getWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        getWindow().setAttributes(p);
    }

}
