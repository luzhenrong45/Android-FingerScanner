/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobi.thinkchange.android.fingerscannercn.weather;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import mobi.thinkchange.android.fingerscannercn.R;

/**
 * 天气情况对照表。
 */
public class WeatherMapping {
    private static HashSet<String> dayAndNightCache = new HashSet<>();
    private static HashMap<String, Integer> attributes = new HashMap();

    static {
        // 包含白天与晚上的天气情况
        dayAndNightCache.add("晴");
        dayAndNightCache.add("多云");
        dayAndNightCache.add("阵雨");
        dayAndNightCache.add("阵雪");

        // 天气情况对照表
        attributes.put("晴", R.drawable.w_clear_day);        // day&night
        attributes.put("晴-夜", R.drawable.w_clear_night);
        attributes.put("多云", R.drawable.w_cloudy_day);     // day&night
        attributes.put("多云-夜", R.drawable.w_cloudy_night);

        attributes.put("阴", R.drawable.w_overcast);

        attributes.put("阵雨", R.drawable.w_rain_shower_day); // day&night
        attributes.put("阵雨-夜", R.drawable.w_rain_shower_night);

        attributes.put("雷阵雨", R.drawable.w_thundershower);
        attributes.put("雷阵雨伴有冰雹", R.drawable.w_thundershower_with_hail);
        attributes.put("雨夹雪", R.drawable.w_sleet);
        attributes.put("小雨", R.drawable.w_light_rain);
        attributes.put("中雨", R.drawable.w_moderate_rain);
        attributes.put("大雨", R.drawable.w_heavy_rain);
        attributes.put("暴雨", R.drawable.w_rainstorm);
        attributes.put("大暴雨", R.drawable.w_large_rainstorm);
        attributes.put("特大暴雨", R.drawable.w_extraordinay_rainstorm);

        attributes.put("阵雪", R.drawable.w_snow_shower_day);    // day&night
        attributes.put("阵雪-夜", R.drawable.w_snow_shower_night);

        attributes.put("小雪", R.drawable.w_light_snow);
        attributes.put("中雪", R.drawable.w_moderate_snow);
        attributes.put("大雪", R.drawable.w_heavy_snow);
        attributes.put("暴雪", R.drawable.w_snowstorm);

        attributes.put("雾", R.drawable.w_frog);              //  雾
        attributes.put("冻雨", R.drawable.w_light_rain);     //  冻雨 - 使用小雨的图标
        attributes.put("沙尘暴", R.drawable.w_frog);          //  沙尘暴
        attributes.put("浮尘", R.drawable.w_frog);            //  浮尘
        attributes.put("扬沙", R.drawable.w_frog);            //  扬沙
        attributes.put("强沙尘暴", R.drawable.w_frog);        //  强沙尘暴
        attributes.put("霾", R.drawable.w_frog);              //  霾

        attributes.put("小到中雨", R.drawable.w_moderate_rain);
        attributes.put("中到大雨", R.drawable.w_heavy_rain);
        attributes.put("大到暴雨", R.drawable.w_rainstorm);
        attributes.put("暴雨到大暴雨", R.drawable.w_large_rainstorm);
        attributes.put("大暴雨到特大暴雨", R.drawable.w_extraordinay_rainstorm);

        attributes.put("小到中雪", R.drawable.w_moderate_snow);
        attributes.put("中到大雪", R.drawable.w_heavy_snow);
        attributes.put("大到暴雪", R.drawable.w_snowstorm);

        attributes.put("其它", R.drawable.w_unknown);            // 默认的天气图标
    }

    public static Integer lookup(String weather) {
        boolean hasDayAndNight = dayAndNightCache.contains(weather);

        String finalWeather = weather;
        if(hasDayAndNight){
            // 夜晚，加上后缀"-夜"
            // 白天：8:00 - 20:00
            // 夜晚：20:00 - 8:00
            // http://zhidao.baidu.com/link?url=N84IzCfY3Vw8DJYmSHXBXD1-2NaCDafo_cZZHsgali0aqs4SxcuIFj7pGeAn23_K6KRQaaIj8-p3BIpKGYB-lK
            Calendar cal = Calendar.getInstance();
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            if(hourOfDay >= 20 || hourOfDay < 8){   // 晚上
                finalWeather = finalWeather + "-夜";
            }
        }

        Integer weatherId = attributes.get(finalWeather);
        return weatherId == null ? attributes.get("其它") : weatherId;
    }
}
