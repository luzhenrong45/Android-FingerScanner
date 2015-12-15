package mobi.thinkchange.android.fingerscannercn.location;

/** 定义事件类型 **/
public class LocationEvent {
    private String text;

    public LocationEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
