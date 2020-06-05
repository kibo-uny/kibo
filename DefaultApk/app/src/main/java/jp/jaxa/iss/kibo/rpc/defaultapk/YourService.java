package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.graphics.Matrix;

import java.lang.Math;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import gov.nasa.arc.astrobee.Kinematics;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    // roll (x), pitch (y), yaw (z)

    private double qua_w, qua_x, qua_y, qua_z;
    private double pos_x, pos_y, pos_z;
    public static final String TAG = "TAG";

    int width = 1280;
    int height = 960;

    // OTW P1-1
    private final Point POINT_P11 = new Point(11.5, -5.7, 4.5);
    private final double[] QUAT_P11 = new double[]{0, 0, -201.8};

    // OTW P1-3
    private final Point POINT_P13_1 = new Point(11, -5.5, 4.5);
    private final Point POINT_P13 = new Point(11, -5.5, 4.33);
    private final double[] QUAT_P13_1 = new double[]{0, 0, -180};
    private final double[] QUAT_P13 = new double[]{0, -90, -180};

    // OTW P1-2
    private final Point POINT_P12 = new Point(11, -6, 5.5);
    private final double[] QUAT_P12 = new double[]{0, -90, -90};

    // LEWATI RINTANGAN
    private final Point POINT_AVOID_1 = new Point(10.529,-6.2,5.5);
    private final Point POINT_AVOID_2 = new Point(10.529, -6.837, 5.5);
    private final Point POINT_AVOID_3 = new Point(11.161, -6.837, 5.5);
    private final double[] QUAT_AVOID_1 = new double[]{0, 0, -90};
    private final double[] QUAT_AVOID_2 = new double[]{0, 0, 0};
    private final double[] QUAT_AVOID_3 = new double[]{0, 0, -100.55};

    // OTW P2-3
    private final Point POINT_P23 = new Point(11, -7.7, 5.55);
    private final double[] QUAT_P23 = new double[]{0, 90, -90};

    // OTW P2-1
    private final Point POINT_P21 = new Point(10.3, -7.5, 4.7);
    private final double[] QUAT_P21 = new double[]{0, 0, -180};

    // OTW P2-2
    private final Point POINT_P22 = new Point(11.5, -8, 5);
    private final double[] QUAT_P22 = new double[]{0, 0, 0};

    private java.util.ArrayList<String> QR_LIST = new java.util.ArrayList<>();

    @Override
    protected void runPlan1(){
        // write here your plan 1
        api.judgeSendStart();

        moveRobot(POINT_P11, QUAT_P11);
        getQR(0, "dock");

        moveRobot(POINT_P13_1, QUAT_P13_1);
        moveRobot(POINT_P13, QUAT_P13);
        getQR(2, "dock");

        moveRobot(POINT_P12, QUAT_P12);
        getQR(1, "nav");

        moveRobot(POINT_AVOID_1, QUAT_AVOID_1);
        moveRobot(POINT_AVOID_2, QUAT_AVOID_2);
        moveRobot(POINT_AVOID_3, QUAT_AVOID_3);

        moveRobot(POINT_P23, QUAT_P23);
        getQR(5, "dock");

        moveRobot(POINT_P21, QUAT_P21);
        getQR(3, "nav");

        moveRobot(POINT_P22, QUAT_P22);
        getQR(4, "nav");

        for(int i = 0; i < QR_LIST.size(); i++){
            String text = QR_LIST.get(i);
            String[] arrSplit = text.split(", ");
            switch (arrSplit[0]){
                case "pos_x":
                    pos_x = Double.parseDouble(arrSplit[1]);
                    Log.d(TAG, "Found: " + pos_x);
                    break;
                case "pos_y":
                    pos_y = Double.parseDouble(arrSplit[1]);
                    Log.d(TAG, "Found: " + pos_y);
                    break;
                case "pos_z":
                    pos_z = Double.parseDouble(arrSplit[1]);
                    Log.d(TAG, "Found: " + pos_z);
                    break;
                case "qua_x":
                    qua_x = Double.parseDouble(arrSplit[1]);
                    Log.d(TAG, "Found: " + qua_x);
                    break;
                case "qua_y":
                    qua_y = Double.parseDouble(arrSplit[1]);
                    Log.d(TAG, "Found: " + qua_y);
                    break;
                case "qua_z":
                    qua_z = Double.parseDouble(arrSplit[1]);
                    Log.d(TAG, "Found: " + qua_z);
                    break;
            }
        }

        // OTW P3 dengan Posisi / Orientasi yang sudah diperoleh


    }

    @Override
    protected void runPlan2(){
        // write here your plan 2

    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    public void moveRobot(Point pt, double[] qt){
        int LOOP_MAX = 6;
        Result result = null;
        qua_w = 0;
        qua_x = 0;
        qua_y = 0;
        qua_z = 0;
        Quaternion quat = eulerToQuaternion(qt);
        result = api.moveTo(pt, quat, true);
        int loopCounter = 0;
        while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
            result = api.moveTo(pt, quat, true);
            ++loopCounter;
        }
    }

    public Quaternion eulerToQuaternion(double[] rpy){
        double roll, pitch, yaw;
        roll = Math.toRadians(rpy[0]);
        pitch = Math.toRadians(rpy[1]);
        yaw = Math.toRadians(rpy[2]);
        qua_x = Math.sin(roll / 2) * Math.cos(pitch / 2) * Math.cos(yaw / 2) - Math.cos(roll / 2) * Math.sin(pitch / 2) * Math.sin(yaw / 2);
        qua_y = Math.cos(roll / 2) * Math.sin(pitch / 2) * Math.cos(yaw / 2) + Math.sin(roll / 2) * Math.cos(pitch / 2) * Math.sin(yaw / 2);
        qua_z = Math.cos(roll / 2) * Math.cos(pitch / 2) * Math.sin(yaw / 2) - Math.sin(roll / 2) * Math.sin(pitch / 2) * Math.cos(yaw / 2);
        qua_w = Math.cos(roll / 2) * Math.cos(pitch / 2) * Math.cos(yaw / 2) + Math.sin(roll / 2) * Math.sin(pitch / 2) * Math.sin(yaw / 2);
        return new Quaternion((float)qua_x, (float)qua_y, (float)qua_z, (float)qua_w);
    }

    public void getQR(int number, String camera){
        // Raw bitmap -> grayscaled -> scaled down
        Bitmap bmap = null;
        if (camera.equals("dock")) {
            bmap = api.getBitmapDockCam();
        }
        else if (camera.equals("nav")) {
            bmap = api.getBitmapNavCam();
        }
        Bitmap grayscaled = toGrayscale(bmap);
        Bitmap scaledBitmap = getResizedBitmap(grayscaled, height/2, width/2);
        
        readQR_ZBar(scaledBitmap, number);
    }

    public void readQR_ZBar(Bitmap bmapzbar, int zbar_number){
        ImageScanner scanner = new ImageScanner();
        int LOOP_CHECKQR_MAX = 4;
        int loopCheckQRCount = 0;
        boolean QR_FOUND = false;
        int[] intArray = new int[width * height];
        Log.d(TAG, "DEBUG 1 CHECK!");
        scanner.setConfig(0, Config.ENABLE, 0);
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
        Log.d(TAG, "DEBUG 2 CHECK!");
        while (loopCheckQRCount < LOOP_CHECKQR_MAX && !QR_FOUND) {
            int wdth = bmapzbar.getWidth();
            int hght = bmapzbar.getHeight();
            Log.d(TAG, "New width bmp: " + wdth);
            Log.d(TAG, "New height bmp: " + hght);
            Log.d(TAG, "DEBUG 3 CHECK!");
            bmapzbar.getPixels(intArray, 0, wdth, 0, 0, wdth, hght);
            Log.d(TAG, "DEBUG 4 CHECK!");
            Image barcode = new Image(wdth, hght, "RGB4");
            Log.d(TAG, "DEBUG 5 CHECK!");
            barcode.setData(intArray);
            Log.d(TAG, "DEBUG 6 CHECK!");
            try {
                int result_zbar = scanner.scanImage(barcode.convert("Y800"));
                Log.d(TAG, "DEBUG 7 CHECK!");
                if (result_zbar != 0) {
                    for (Symbol symbol_zbar : scanner.getResults()) {
                        Log.d(TAG, "DEBUG 8 CHECK!");
                        String qrContent_zbar = symbol_zbar.getData();
                        api.judgeSendDiscoveredQR(zbar_number, qrContent_zbar);
                        QR_FOUND = true;
                        QR_LIST.add(qrContent_zbar);
                        Log.d(TAG, "Found QR Content: " + qrContent_zbar);
                    }
                }
                else {
                    Log.d(TAG, "QR ERROR: NOT FOUND!");
                }
            } catch (Exception e) {
                Log.d(TAG, "EXCEPTION OCCURED!");
            }
            bmapzbar = RotateBitmap(bmapzbar, 90);
            ++loopCheckQRCount;
        }
    }

    public Bitmap RotateBitmap(Bitmap src, float angle) {
        Matrix matriks = new Matrix();
        matriks.postRotate(angle);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matriks, true);
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix_resize = new Matrix();
        matrix_resize.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix_resize, false);
        return resizedBitmap;
    }
}
