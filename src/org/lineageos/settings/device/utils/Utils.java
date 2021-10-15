package org.lineageos.settings.device.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import org.lineageos.settings.device.MainApplication;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class Utils {

    private static final String TAG = "DeviceSettings_Utils";

    public static Context applicationContext;
    public static boolean isSwitchingCDMA = false;
    public static boolean isSwitchingCamHal = false;

    public static final boolean AT_LEAST_Q = Build.VERSION.SDK_INT >= 29;
    public static final int NETWORK_MODE_LTE_ONLY = getLTEONLY();
    public static final int NETWORK_MODE_GLOBAL = getGlobalMode();
    public static final int NETWORK_MODE_GSM_ONLY = getGSMONLY();

    public static final String[] HTTP_PROXY_DEPENDENCY_APPS = new String[] {
            "com.v2ray.ang",
            "com.cqyapp.tinyproxy"
    };

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
            case "46003":
            case "46011":
            case "46012":
                return Operator.CHINA_TELECOM;
            case "46001":
            case "46006":
            case "46009":
            case "46010":
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
        Log.d(TAG, "getPublicIpRegionName+++");
        String url = "http://whois.pconline.com.cn/ipJson.jsp?json=true";
        Request request = new Request.Builder()
                .url(url)
                .build();
        final OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        String json = null;
        try (Response response = client.newCall(request).execute()) {
            json = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == json) {
            return null;
        }
        try {
            Log.d(TAG, "getPublicIpRegionName: json = " + json);
            IPDetailBean ipDetailBean = JSONObject.parseObject(json.toString(),
                    IPDetailBean.class);
            Log.d(TAG, "getPublicIpRegionName---");
            return ipDetailBean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hasNetWork() {
        try {
            ConnectivityManager connectivity =
                    (ConnectivityManager) MainApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
            return  null != connectivity.getActiveNetworkInfo();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean allAppsInstalled(String[] apps) {
        if (null == apps
                || apps.length == 0) {
            return false;
        }
        final PackageManager packageManager = MainApplication.getInstance().getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if(info == null || info.isEmpty()) {
            return false;
        }
        int installedAppsCount = 0;
        for (String app : apps) {
            for ( int i = 0; i < info.size(); i++ ) {
                if (TextUtils.equals(info.get(i).packageName, app)) {
                    installedAppsCount++;
                    break;
                }
            }
        }
        return installedAppsCount == apps.length;
    }
}
