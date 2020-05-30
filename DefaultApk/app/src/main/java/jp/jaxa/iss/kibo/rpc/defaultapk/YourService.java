package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.graphics.Bitmap;

import java.lang.Math;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    // roll (x), pitch (y), yaw (z)
    private double roll, pitch, yaw;
    private double qua_w, qua_x, qua_y, qua_z;


    // OTW P1-1, orientasi naik 11.31 derajat
    private final Point POINT_1_P11 = new Point(10.95, -3.95, 4.85);
    private final Point POINT_2_P11 = new Point(11.5, -3.95, 4.85);
    private final Point POINT_3_P11 = new Point(11.5, -3.95, 4.85);
    private final Point POINT_4_P11 = new Point(11.5, -5.7, 4.5);
    private final Point POINT_5_P11 = new Point(11.5, -5.7, 4.5);
    private double[] QUAT_1_P11_z = new double[]{0, 0, -90};
    private double[] QUAT_2_P11_z = new double[]{0, 0, 0};
    private double[] QUAT_3_P11_z = new double[]{0, 0, -90};
    private double[] QUAT_4_P11_x = new double[]{-11.31, 0, 0};
    private double[] QUAT_5_P11_z = new double[]{0, 0, 0};

    private Point[] arrayPoint = null;
    private Quaternion[] arrayQuaternion = null;
    private double[][] arrayQuat = null;

    @Override
    protected void runPlan1(){
        // write here your plan 1
        api.judgeSendStart();
        Result result = null;
        final int LOOP_MAX = 3;
        arrayPoint = new Point[] {POINT_1_P11, POINT_2_P11, POINT_3_P11, POINT_4_P11, POINT_5_P11};
        arrayQuat = new double[][]{QUAT_1_P11_z, QUAT_2_P11_z, QUAT_3_P11_z, QUAT_4_P11_x, QUAT_5_P11_z};
        for (int i = 0; i < arrayPoint.length; i++) {
            qua_w = 0;
            qua_x = 0;
            qua_y = 0;
            qua_z = 0;

            final Quaternion quaternion = eulerToQuaternion(arrayQuat[i]);
            final Point point = arrayPoint[i];
            result = api.moveTo(point, quaternion, true);
            // api.stopAllMotion();
            int loopCounter = 0;
            while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;
            }
        }

        Bitmap bmp = api.getBitmapNavCam();
        String contents;

        int[] intArray = new int[bmp.getWidth()*bmp.getHeight()];
        bmp.getPixels(intArray, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bmp.getWidth(), bmp.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        com.google.zxing.Reader reader = new MultiFormatReader();
        com.google.zxing.Result resultP11 = null;
        try {
            resultP11 = reader.decode(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        contents = resultP11.getText();
        api.judgeSendDiscoveredQR(0, contents);

    }

    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    private Quaternion eulerToQuaternion(double[] rpy){
        roll = Math.toRadians(rpy[0]);
        pitch = Math.toRadians(rpy[1]);
        yaw = Math.toRadians(rpy[2]);
        qua_x = Math.sin(roll / 2) * Math.cos(pitch / 2) * Math.cos(yaw / 2) - Math.cos(roll / 2) * Math.sin(pitch / 2) * Math.sin(yaw / 2);
        qua_y = Math.cos(roll / 2) * Math.sin(pitch / 2) * Math.cos(yaw / 2) + Math.sin(roll / 2) * Math.cos(pitch / 2) * Math.sin(yaw / 2);
        qua_z = Math.cos(roll / 2) * Math.cos(pitch / 2) * Math.sin(yaw / 2) - Math.sin(roll / 2) * Math.sin(pitch / 2) * Math.cos(yaw / 2);
        qua_w = Math.cos(roll / 2) * Math.cos(pitch / 2) * Math.cos(yaw / 2) + Math.sin(roll / 2) * Math.sin(pitch / 2) * Math.sin(yaw / 2);
        Quaternion quat = new Quaternion((float)qua_x, (float)qua_y, (float)qua_z, (float)qua_w);
        return quat;
    }

}

