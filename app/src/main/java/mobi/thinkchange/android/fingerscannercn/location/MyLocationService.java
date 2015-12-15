package mobi.thinkchange.android.fingerscannercn.location;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import de.greenrobot.event.EventBus;

public class MyLocationService extends Service {

    public LocationClient mLocationClient;
//    public GeofenceClient mGeofenceClient;

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener
            implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("城市名称: ");
            sb.append(cutCityName(location.getCity()));
//            Log.i("BaiduLocationApiDem", sb.toString());

            // 发送消息
            EventBus.getDefault().post(new LocationEvent(cutCityName(location.getCity())));
        }

    }

    public MyLocationService() {
    }

    public class LocalBinder extends Binder {
        public MyLocationService getService() {
            return MyLocationService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLocation();

        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        initLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopLocation();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02
        int span = 1000;
        try {
            span = 1000;
        }
        catch (Exception e) {
            // TODO: handle exception
        }
        option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public void startLocation(){
        mLocationClient.start();
    }

    public void stopLocation(){
        mLocationClient.stop();
    }

    /**
     * 截取城市名称（最后一个字符为“市”或者“县”截取掉），如果城市名称只有两个字符不截取
     *
     * @param str
     */
    static String cutCityName(String str) {
        String string = str;
        if (str != null && str.length() > 2) {
            if (str.endsWith("市") || str.endsWith("县")) {
                // 去掉最后一个字符
                string = str.substring(0, str.length() - 1);
            }
        }
        return string;
    }
}
