package mobi.thinkchange.android.fingerscannercn.weather;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import mobi.thinkchange.android.fingerscannercn.weather.bean.WeatherInfo;

public class HttpUtils {
    private Context context;

    public HttpUtils(Context context) {
        this.context = context;
    }

    /** 获取天气数据 **/
    public void getWeatherData(String cityName, final TextView separatorView, final TextView infoView) {
        // 获取到一个RequestQueue对象
        RequestQueue mQueue = Volley.newRequestQueue(context);

        GsonRequest<WeatherInfo> gsonRequest =
            new GsonRequest<WeatherInfo>("http://apistore.baidu.com/microservice/weather?cityname=" +
                encode(cityName), WeatherInfo.class, new Response.Listener<WeatherInfo>() {
                @Override
                public void onResponse(WeatherInfo weatherInfo) {
                    // 第一层解析
                    String retMsg = weatherInfo.getErrMsg();
                    if (retMsg.equals("success")) {
                        if(separatorView != null && infoView != null) { // 判断是否已经解锁了(解锁之后就为null了)
                            WeatherInfo.WeatherData weatherData = weatherInfo.getRetData();

                            separatorView.setText("/");
                            Drawable weatherIcon = context.getResources().getDrawable(WeatherMapping.lookup(weatherData.getWeather()));
                            infoView.setCompoundDrawablesWithIntrinsicBounds(weatherIcon, null, null, null);
                            infoView.setText(weatherData.getTemp() + "°C");
                        }
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("TAG", error.getMessage(), error);
                }
            });

        // 将这个StringRequest对象添加到RequestQueue里面
        mQueue.add(gsonRequest);
    }

    /**
     * 对"input"进行url编码(utf-8)。
     * 
     * @param cityName
     * @return
     */
    public static String encode(String cityName) {
        if (cityName == null)
            return "";

        try {
            return URLEncoder.encode(cityName, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new AssertionError(
                                     new StringBuilder().append("URL encoding failed for: ").append(cityName).toString());
        }
    }
}
