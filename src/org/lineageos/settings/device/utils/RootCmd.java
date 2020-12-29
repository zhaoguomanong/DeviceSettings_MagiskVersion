package org.lineageos.settings.device.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class RootCmd {

    private static final String TAG = "RootCmd";

    /**
     *   判断机器Android是否已经root，即是否获取root权限
     */
    public static boolean haveRoot() {
        int ret = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
        if (ret == 0) {
            Log.i(TAG, "have root!");
        } else {
            Log.i(TAG, "not root!");
        }
        return ret == 0;
    }

    /**
     * 执行命令并且输出结果
     */
    public static boolean execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;
        boolean success = true;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                Log.d("result", line);
                result += line;
            }
            p.waitFor();
            success = p.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }
        Log.d(TAG, "result = " + result);
        return success;
    }

    public static String execRootCmdWithResults(String cmd) {
        String result = "";
        BufferedWriter dos = null;
        BufferedReader dis = null;
        boolean success = true;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), Charset.forName("UTF-8")));
            dis = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
            Log.i(TAG, cmd);
            dos.write(cmd + "\n");
            dos.flush();
            dos.write("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                Log.d("result", line);
                result += line;
            }
            p.waitFor();
            success = p.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }
        Log.d(TAG, "result = " + result);
        return result;
    }

    /**
     * 执行命令但不关注结果输出
     */
    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
