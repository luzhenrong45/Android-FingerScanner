package mobi.thinkchange.android.fingerscannercn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;
import mobi.thinkchange.android.fingerscannercn.util.Utils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferencesHelper prefHelper = new SharedPreferencesHelper(
                    context);
            boolean autoStart = prefHelper.autoStart(false);
            prefHelper.destroy();

            if (autoStart) {
                // 启动锁屏服务
                Utils.updateLockService(context, true);

                // 启动锁屏界面
                Intent intentLockScreen = new Intent(context, LockActivity.class);
                intentLockScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentLockScreen.putExtra("from", "boot.receiver");
                context.startActivity(intentLockScreen);
            }
        }
    }

}
