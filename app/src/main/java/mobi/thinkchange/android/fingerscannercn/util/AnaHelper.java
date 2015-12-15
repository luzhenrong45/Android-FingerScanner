package mobi.thinkchange.android.fingerscannercn.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AnaHelper {
	
	private static final String KEY_OF_ANA_EXCEEDS_DATE = "ana_exc_dt";
	private static final String KEY_OF_ANA_EXCEEDS_COUNTER = "ana_exc_cou";
	private static final long ONE_DAY_IN_MILISECOND = 24 * 60 * 60 * 1000;
    private static final int ANA_TOP_LIMIT_ONE_DAY = 100;
	
	private static SharedPreferences getSharedPreferences(Context context) {
        int flag = Build.VERSION.SDK_INT >= 11 ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE;
		return context.getSharedPreferences(getDefaultSharedPreferencesName(context), flag);
	}

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
	
	/**
	 * 获得当前时间与软件首次使用时间比较后的对应日期的字符串，格式如：2012-05-13。<br/>
	 * <br/>
	 * <i>计算方法变更：当前时间(除日期外，即时分秒)<安装时间(时分秒)，则返回前一天的字符串；否则，则返回今天的字符串。<br/>
	 * 简单的说，就是以安装的时间(时分秒)作为0点，在此基础上每隔24小时为新的一天。<br/>
	 * 以此尝试平滑用户的请求访问曲线(因为公共类库1.3.1及之前的版本在真正0点过后的短时间内发送的请求比较集中)。</i>
	 * 
	 * @return 获得当前时间与软件首次使用时间比较后的对应日期的字符串，格式如：2012-05-13
	 * @since 2012-07-26
	 * @version 1.1
	 */
	private static String getLogicDateInString(Context context) {
		long currTime = System.currentTimeMillis();
//		long installTime = TCUManager3.getInstance().getInstallDt();
		long installTime = 0;
		long processTime;

		// 取当前时间的时分秒与安装时间的时分秒作比较
		long currTimeOffSet = getTimePartInMillis(currTime);
		long installTimeOffset = getTimePartInMillis(installTime);
		if (currTimeOffSet < installTimeOffset)
			processTime = currTime - ONE_DAY_IN_MILISECOND;
		else
			processTime = currTime;

		return getDateInString(processTime);
	}
	
	/**
	 * 获得指定时间对应日期的字符串，格式如：2012-05-13
	 * 
	 * @param time
	 *            以毫秒为单位的时间
	 * @return 指定时间对应日期的字符串，格式如：2012-05-13
	 */
	private static String getDateInString(long time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		return dateFormat.format(new Date(time));
	}
	
	/**
	 * 取得一个指定时间的时分秒(包括毫秒)部分并以毫秒的形式返回。
	 * 
	 * @param time
	 *            指定的一个时间
	 * @return 一个指定时间的时分秒(包括毫秒)部分(以毫秒的形式)
	 * @since 2012-07-26
	 * @version 1.0
	 */
	private static long getTimePartInMillis(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return (time - cal.getTimeInMillis());
	}

	/**
	 * 当日统计是否超过上限。<br/>
	 * 上限为100条/逻辑天
	 * @param context
	 * @return
	 */
	public static synchronized boolean anaExceeds(Context context, int flag){
        String counterKey = KEY_OF_ANA_EXCEEDS_COUNTER + "_" + flag;
		boolean res = false;
		int anaCounter = 1;					// 发送次数计数器
		String logicDateInString = getLogicDateInString(context);
		
		SharedPreferences settings = getSharedPreferences(context);
		
		String msgDate = settings.getString(KEY_OF_ANA_EXCEEDS_DATE, "");
		if (msgDate.equals(logicDateInString))
			anaCounter = settings.getInt(counterKey, anaCounter);
		
		if(anaCounter > ANA_TOP_LIMIT_ONE_DAY){			// 上限是100次/逻辑天
			res = true;
		}else{								// 没有超过上限，将计数器加1
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(KEY_OF_ANA_EXCEEDS_DATE, logicDateInString);
			editor.putInt(counterKey, anaCounter + 1);
			applyCompat(editor);
		}
		
		return res;
	}

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static void applyCompat(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();
    }
	
}
