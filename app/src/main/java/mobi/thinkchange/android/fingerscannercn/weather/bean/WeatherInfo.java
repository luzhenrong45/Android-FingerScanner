package mobi.thinkchange.android.fingerscannercn.weather.bean;

public class WeatherInfo {

    private String errMsg;// 是否获取成功
    private WeatherData retData;// 天气数据

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public WeatherData getRetData() {
        return retData;
    }

    public void setRetData(WeatherData retData) {
        this.retData = retData;
    }

    public static class WeatherData {
        private String weather;// 天气情况
        private String temp;// 当前温度

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

    }
}
