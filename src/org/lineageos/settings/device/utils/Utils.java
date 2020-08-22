package org.lineageos.settings.device.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import java.util.List;


public class Utils {

    public static Context applicationContext;
    public static boolean isSwitchingCDMA = false;

    public static final boolean AT_LEAST_Q = Build.VERSION.SDK_INT >= 29;
    public static final int NETWORK_MODE_LTE_ONLY = getLTEONLY();
    public static final int NETWORK_MODE_GLOBAL = getGlobalMode();
    public static final int NETWORK_MODE_GSM_ONLY = getGSMONLY();

    private static int getLTEONLY() {
        Integer lteOnly = (Integer) ReflectionUtils.getStaticAttribute(
                "android.telephony.TelephonyManager",
                "NETWORK_MODE_LTE_ONLY");
        if (null == lteOnly) {
            return -100;
        }
        return lteOnly;
    }

    private static int getGSMONLY() {
        Integer gsmOnly = (Integer) ReflectionUtils.getStaticAttribute(
                "android.telephony.TelephonyManager",
                "NETWORK_MODE_GSM_ONLY");
        if (null == gsmOnly) {
            return -100;
        }
        return gsmOnly;
    }

    private static int getGlobalMode() {
        Integer globalMode = (Integer) ReflectionUtils.getStaticAttribute(
                "android.telephony.TelephonyManager",
                "NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA");
        if (null == globalMode) {
            return -100;
        }
        return globalMode;
    }

    public static Operator toOperator(SubscriptionInfo subscriptionInfo) {
        if (null == subscriptionInfo) {
            return Operator.OTHER;
        }
        List<String> ehplmns = (List<String>) ReflectionUtils.invokeMethod(
                subscriptionInfo,
                "getEhplmns",
                null,
                null);
        Operator operator = Operator.OTHER;
        for (String mns : ehplmns) {
            if (TextUtils.isEmpty(mns)) {
                continue;
            }
            if (mns.startsWith("46001")) {
                operator = Operator.CHINA_UNICOM;
                break;
            } else if (mns.startsWith("46011")
                    || mns.startsWith("46003")) {
                operator = Operator.CHINA_TELECOM;
                break;
            }
        }
        return operator;
    }

}
