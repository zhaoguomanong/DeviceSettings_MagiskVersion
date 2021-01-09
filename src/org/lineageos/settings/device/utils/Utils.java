package org.lineageos.settings.device.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Utils {

    private static final String TAG = "DeviceSettings_Utils";

    public static Context applicationContext;
    public static boolean isSwitchingCDMA = false;
    public static boolean isSwitchingCamHal = false;

    public static final boolean AT_LEAST_Q = Build.VERSION.SDK_INT >= 29;
    public static final int NETWORK_MODE_LTE_ONLY = getLTEONLY();
    public static final int NETWORK_MODE_GLOBAL = getGlobalMode();
    public static final int NETWORK_MODE_GSM_ONLY = getGSMONLY();

    private static int getLTEONLY() {
        return 11;
    }

    private static int getGSMONLY() {
        return 1;
    }

    private static int getGlobalMode() {
        return 10;
    }

    public static Operator toOperator(SubscriptionInfo subscriptionInfo) {
        if (null == subscriptionInfo) {
            return Operator.OTHER;
        }
        String MCC_MNC = subscriptionInfo.getMcc()
                + String.format("%02d", subscriptionInfo.getMnc());
        if (TextUtils.isEmpty(MCC_MNC)) {
            return Operator.OTHER;
        }
        switch (MCC_MNC) {
            case "46011":
            case "46003":
                return Operator.CHINA_TELECOM;
            case "46001":
                return Operator.CHINA_UNICOM;
            default:
                return Operator.OTHER;
        }
    }

    public static String serviceState2Str(int state) {
        switch (state) {
            case ServiceState.STATE_EMERGENCY_ONLY:
                return "STATE_EMERGENCY_ONLY";
            case ServiceState.STATE_IN_SERVICE:
                return "STATE_IN_SERVICE";
            case ServiceState.STATE_OUT_OF_SERVICE:
                return "STATE_OUT_OF_SERVICE";
            case ServiceState.STATE_POWER_OFF:
                return "STATE_POWER_OFF";

        }
        return "STATE_UNKNOWN";
    }

    public static IPDetailBean getPublicIpRegionName() {
        URL url = null;
        InputStream inStream = null;
        StringBuilder json = new StringBuilder();
        try {
            url = new URL("http://whois.pconline.com.cn/ipJson.jsp?json=true");
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "GBK"));
                String tmp;
                while ((tmp = reader.readLine()) != null) {
                    json.append(tmp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(json.toString())) {
            return null;
        }
        try {
            Log.d(TAG, "getPublicIpRegionName: json = " + json);
            IPDetailBean ipDetailBean = JSONObject.parseObject(json.toString(),
                    IPDetailBean.class);
            return ipDetailBean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
