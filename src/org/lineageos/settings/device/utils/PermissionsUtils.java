package org.lineageos.settings.device.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import org.lineageos.settings.device.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsUtils {
    private static final String TAG = "PermissionsUtils";

    private static final String[] CORE_PERMISSIONS = new String[] {
            Manifest.permission.READ_PHONE_STATE,
    };

    public static void requestPermissions(final Activity activity) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "api level below than M no need check");
            return;
        }

        boolean hasAllPermissions = !isMissingCorePermissions(activity);
        if (hasAllPermissions) {
            Log.d(TAG, "already have all core permissions return");
            return;
        }
        Log.d(TAG, "lost some core permissions request now");
        final String[] missingPermissions = findMissingPermissions(activity);
        if (null == missingPermissions
                || missingPermissions.length == 0) {
            Log.e(TAG, "missing list is null error!!!");
            return;
        }
        Log.d(TAG, "missing list = " + Arrays.deepToString(missingPermissions));
        PermissionsUtil.requestPermission(activity, new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permission) {
                Log.d(TAG, "permissionGranted: " + Arrays.deepToString(permission));
            }

            @Override
            public void permissionDenied(@NonNull String[] permission) {
                Log.d(TAG, "permissionDenied: " + Arrays.deepToString(permission));
                Toast.makeText(Utils.applicationContext,
                        R.string.core_permissions_lost,
                        Toast.LENGTH_SHORT).show();
                PermissionsUtil.gotoSetting(activity);
            }

        }, missingPermissions, false, null);
    }

    private static String[] findMissingPermissions(Context context) {
        List<String> missing = new ArrayList<>();

        for (String per : CORE_PERMISSIONS) {
            if (!PermissionsUtil.hasPermission(context, per)) {
                missing.add(per);
            }
        }

        if (missing.isEmpty()) {
            return null;
        } else {
            String[] array = new String[missing.size()];
            return missing.toArray(array);
        }
    }

    public static boolean isMissingCorePermissions(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "api level below than M no need check");
            return false;
        }
        return !PermissionsUtil.hasPermission(activity, CORE_PERMISSIONS);
    }

    public static boolean hasPermission(String permission) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return PermissionsUtil.hasPermission(Utils.applicationContext, permission);
    }
}
