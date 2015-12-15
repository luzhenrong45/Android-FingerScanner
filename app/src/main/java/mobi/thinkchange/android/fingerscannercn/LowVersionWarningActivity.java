package mobi.thinkchange.android.fingerscannercn;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import mobi.thinkchange.android.fingerscannercn.dialog.VersionLowDialog;

/**
 * 系统版本过低提示。
 *
 * @author Ke Shang
 * @since 2015/01/26
 */
public class LowVersionWarningActivity
        extends AbsBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_version_warning);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        VersionLowDialog dialog = new VersionLowDialog(this);
        dialog.setOnClickListener(new VersionLowDialog.OnClickListener() {
            @Override
            public void onOKClicked() {
                startUrl(R.string.old_version_link_fingerscanner);

                statLowVersion(5);

                finish();
            }

            @Override
            public void onIconClicked(int id) {
                int url = -1;
                int v6 = -1;
                switch (id) {
                    case R.id.low_flashlight:
                        url = R.string.old_version_link_flashlight;
                        v6 = 1;
                        break;
                    case R.id.low_qrcode:
                        url = R.string.old_version_link_qrcode;
                        v6 = 2;
                        break;
                    case R.id.low_fbi:
                        url = R.string.old_version_link_fbi;
                        v6 = 3;
                        break;
                    case R.id.low_crystaltimer:
                        url = R.string.old_version_link_crystaltimer;
                        v6 = 4;
                        break;
                    default:
                        break;
                }

                if (url != -1)
                    startUrl(url);

                if (v6 != -1) {
                    statLowVersion(v6);
                }

                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                statLowVersion(6);

                finish();
            }
        });
        dialog.show();

        // 统计Activity打开
        MyApplication application = (MyApplication) getApplication();
        application.statActivityOpen(4);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean showActionBar() {
        return false;
    }

    private void startUrl(int url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(url)));
        startActivity(intent);
    }

    private void statLowVersion(int v6) {
//        MyApplication myApplication = (MyApplication) getApplication();
//
//        Map<String, String> statParams = myApplication.getStatCommonParams(4);
//        statParams.put("v6", String.valueOf(v6));
//        TCUManager3.getInstance().trackGeneric(statParams);
    }
}
