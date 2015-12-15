package mobi.thinkchange.android.fingerscannercn;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mobi.thinkchange.android.fingerscannercn.util.TriangleShape;


/**
 * 悬浮在系统设置界面上的半透明提示Activity。
 *
 * @author Ke Shang
 * @since 2015/04/02
 */
public class FloatingTipActivity
        extends AbsBaseActivity implements View.OnClickListener {

    public static final String EXTRA_MESSAGE = "extra_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floating_tip);

        initMessage();
        findViewById(R.id.floating_tip_root).setOnClickListener(this);
    }

    private void initMessage(){
        int messageId = getIntent().getIntExtra(EXTRA_MESSAGE, 0);

        TextView message = (TextView) findViewById(R.id.floating_tip_message);
        message.setText(messageId);
    }

    @Override
    protected boolean showActionBar() {
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.floating_tip_root) {
            finish();
        }
    }
}
