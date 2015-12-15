package mobi.thinkchange.android.fingerscannercn;

import android.app.Application;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import mobi.thinkchange.android.fingerscannercn.util.SharedPreferencesHelper;

/**
 * Created by FIMH on 2015/2/6.
 */
public class MyApplication extends Application {

    private String statId;

    public String getStatId() {
        return statId;
    }

    public void refreshStatId() {
        long idNum = (new SecureRandom().nextLong() & 0xFFFFFFFF) % 9223372036854775807L + 1L;
        statId = Long.toHexString(idNum).substring(0, 8);
    }

    public void statActivityOpen(int activityId) {
//        Map<String, String> params = getStatCommonParams(1);
//        params.put("v3", String.valueOf(activityId));
//        TCUManager3.getInstance().trackGeneric(params);
    }

    public void statLockSwitchState(boolean appStart){
//        String paramName = appStart ? "v12" : "v13";
//
//        Map<String, String> params = getStatCommonParams(8);
//        params.put(paramName, getLockSwitchState() ? "1" : "0");
//        TCUManager3.getInstance().trackGeneric(params);
    }

    public Map<String, String> getStatCommonParams(int v19) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("v1", getStatId());
        params.put("v2", String.valueOf(getOpenTimes()));
        params.put("v19", String.valueOf(v19));
        params.put("v20", "0104");

        return params;
    }

    public int getOpenTimes() {
        SharedPreferencesHelper helper = new SharedPreferencesHelper(this);
        int openTimes = helper.openTimes(0);
        helper.destroy();

        return openTimes;
    }

    public boolean getLockSwitchState() {
        SharedPreferencesHelper helper = new SharedPreferencesHelper(this);
        boolean res = helper.enableLock(true);
        helper.destroy();

        return res;
    }
}
