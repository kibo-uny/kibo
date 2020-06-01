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


    // OTW P1-1
    private final Point POINT_P11 = new Point(11.5, -5.7, 4.5);
    private final double[] QUAT_P11 = new double[]{0, 0, 0};

    // OTW P1-2
    private final Point POINT_P12 = new Point(11, -6, 5.5);
    private final double[] QUAT_P12 = new double[]{0, -90, 0};

    // OTW P1-3
    private final Point POINT_P13 = new Point(11, -5.5, 4.33);
    private final double[] QUAT_P13 = new double[]{0, 90, -90};

    // LEWATI RINTANGAN
    private final Point POINT_AVOID_1 = new Point(10.529,-6.2,4.33);
    private final Point POINT_AVOID_2 = new Point(10.529, -6.837, 4.33);
    private final Point POINT_AVOID_3 = new Point(11.105, -6.837, 4.33);
    private final Point POINT_AVOID_4 = new Point(11.207, -7.394, 4.33);
    private final double[] QUAT_AVOID_1 = new double[]{0, 0, -90};
    private final double[] QUAT_AVOID_2 = new double[]{0, 0, 0};
    private final double[] QUAT_AVOID_3 = new double[]{0, 0, -90};
    private final double[] QUAT_AVOID_4 = new double[]{0, 0, -100.86};

    // OTW P2-1
    private final Point POINT_P21 = new Point(10.3, -7.5, 4.7);
    private final double[] QUAT_P21 = new double[]{0, 0, 1, 0};

    // OTW P2-2
    private final Point POINT_P22 = new Point(11.5, -8, 5);
    private final double[] QUAT_P22 = new double[]{0, 0, 1};

    // OTW P2-3
    private final Point POINT_P23 = new Point(11, -7.7, 5.55);
    private final double[] QUAT_P23 = new double[]{0, -90, 0};

    private Point[] arrayPoint1 = null;
    private double[][] arrayQuat1 = null;
    private Point[] arrayPoint2 = null;
    private double[][] arrayQuat2 = null;
    private Point[] arrayPoint3 = null;
    private double[][] arrayQuat3 = null;

    int QR_COUNT = 0;

    @Override
    protected void runPlan1(){
        // write here your plan 1
        api.judgeSendStart();

        arrayPoint1 = new Point[]{POINT_P11, POINT_P12, POINT_P13};
        arrayQuat1 = new double[][]{QUAT_P11, QUAT_P12, QUAT_P13};
        moveAstrobee(arrayPoint1, arrayQuat1);

        arrayPoint2 = new Point[]{POINT_AVOID_1, POINT_AVOID_2, POINT_AVOID_3, POINT_AVOID_4};
        arrayQuat2 = new double[][]{QUAT_AVOID_1, QUAT_AVOID_2, QUAT_AVOID_3, QUAT_AVOID_4};
        avoidObstacle(arrayPoint2, arrayQuat2);

        arrayPoint3 = new Point[]{POINT_P21, POINT_P22, POINT_P23};
        arrayQuat3 = new double[][]{QUAT_P21, QUAT_P22, QUAT_P23};
        moveAstrobee(arrayPoint3, arrayQuat3);

    }

    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    private void avoidObstacle(Point[] pt, double[][] quat){
        Point[] p = pt;
        double[][] q = quat;
        final int LOOP_MAX = 3;
        Result result = null;
        for (int i = 0; i < p.length; i++) {
            qua_w = 0;
            qua_x = 0;
            qua_y = 0;
            qua_z = 0;

            final Quaternion quaternion = eulerToQuaternion(q[i]);
            final Point point = p[i];

            result = api.moveTo(point, quaternion, true);

            int loopCounter = 0;
            while (!result.hasSucceeded() || loopCounter < LOOP_MAX) {
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;
            }
        }
    }

    private void moveAstrobee(Point[] pt, double[][] quat){
        Point[] p = pt;
        double[][] q = quat;
        final int LOOP_MAX = 4;
        int LOOP_TARGET_MAX = 0;
        Result result = null;
        for (int i = 0; i < p.length; i++) {
            qua_w = 0;
            qua_x = 0;
            qua_y = 0;
            qua_z = 0;

            final Quaternion quaternion = eulerToQuaternion(q[i]);
            final Point point = p[i];

            result = api.moveTo(point, quaternion, true);

            int loopCounter = 0;
            int loopTargetCounter = 0;
            if (i == 2) {
                LOOP_TARGET_MAX = 5;
            }
            while (!result.hasSucceeded() || loopCounter < LOOP_MAX || loopTargetCounter < LOOP_TARGET_MAX) {
                if (i == 2) getQR(QR_COUNT);
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;
                ++loopTargetCounter;
            }
        }
        ++QR_COUNT;
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

    private String getQR(int no){
        Bitmap bmp = api.getBitmapNavCam();
        String contents;

        int[] intArray = new int[bmp.getWidth()*bmp.getHeight()];
        bmp.getPixels(intArray, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bmp.getWidth(), bmp.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        com.google.zxing.Reader reader = new MultiFormatReader();
        com.google.zxing.Result QR_RESULT = null;
        try {
            QR_RESULT = reader.decode(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        contents = QR_RESULT.getText();
        if (contents != null) {
            api.judgeSendDiscoveredQR(no, contents);
        }

        return contents;
    }

}

