package com.tencent.tsf.femas.util;


import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.entity.metrix.TimeSeries;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/13 11:25 上午
 */
public class AdminTimeUtil {

    public static String toSecondStamp(Long millStamp) {
        String strStamp = millStamp + "";
        return (strStamp).substring(0, strStamp.length() - 3);
    }

    public static String millStamp2UTC(Long millStamp) {
        millStamp = millStamp - 28800000;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String formatTime = simpleDateFormat.format(new Date(millStamp));
        return formatTime;
    }

    public static Long doubleSecond2MillStamp(String doubleSecondStr) {
        Double millStamp = Double.parseDouble(doubleSecondStr) * 1000;
        return millStamp.longValue();
    }

    public static List<TimeSeries> metricData2TimeSeries(List<List<Object>> metricData) {
        ArrayList<TimeSeries> res = new ArrayList<>();
        if (CollectionUtil.isEmpty(metricData)) {
            return res;
        }
        // 返回时间戳格式统一处理(prometheus获取的时间错有double和Integer)
        boolean isDouble = true;
        try {
            // double类型装换尝试
            Double test = (Double) metricData.get(0).get(0);
        } catch (Exception e) {
            isDouble = false;
        }
        if (isDouble) {
            for (List<Object> data : metricData) {
                Double time = (Double) data.get(0);
                Double doubleTimeStamp = time * 1000;
                TimeSeries series = new TimeSeries(doubleTimeStamp.longValue(), (String) data.get(1));
                res.add(series);
            }
        } else {
            for (List<Object> data : metricData) {
                Long time = new Long((Integer) data.get(0));
                Long timeStamp = time * 1000;
                TimeSeries series = new TimeSeries(timeStamp, (String) data.get(1));
                res.add(series);
            }
        }
        return res;
    }
}
