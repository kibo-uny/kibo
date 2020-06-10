package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.graphics.Matrix;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import boofcv.abst.fiducial.QrCodeDetector;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.android.ConvertBitmap;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.struct.image.GrayU8;
import gov.nasa.arc.astrobee.Robot;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Mat;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    // roll (x), pitch (y), yaw (z)

    private double qua_w, qua_x, qua_y, qua_z;
    private double pos_x, pos_y, pos_z;
    public static final String TAG = "TAG";
    boolean TEST_FOUND = false;
    boolean AR_FOUND = false;
    int width = 1280;
    int height = 960;

    // OTW P1-1
    private final Point POINT_P11 = new Point(11.4, -5.7, 4.5);
    private final double[] QUAT_P11 = new double[]{0, 0, -201.8}; //-201.8

    // OTW P1-3
    private final Point POINT_P13_1 = new Point(11, -5.5, 4.5);
    private final Point POINT_P13 = new Point(11, -5.5, 4.33);
    private final double[] QUAT_P13_1 = new double[]{0, 0, -180};
    private final double[] QUAT_P13 = new double[]{0, -90, -180};

    // OTW P1-2
    private final Point POINT_P12 = new Point(11, -6, 5.4);
    private final double[] QUAT_P12 = new double[]{0, -90, -90};

    // LEWATI RINTANGAN 1
    private final Point POINT1_AVOID_1 = new Point(10.529,-6.2,5.4);
    private final Point POINT1_AVOID_2 = new Point(10.529, -6.837, 5.4);
    private final Point POINT1_AVOID_3 = new Point(11.161, -6.837, 5.4);
    private final double[] QUAT1_AVOID_1 = new double[]{0, 0, -90};
    private final double[] QUAT1_AVOID_2 = new double[]{0, 0, 0};
    private final double[] QUAT1_AVOID_3 = new double[]{0, 0, -100.55}; // -100.55

    // OTW P2-3
    private final Point POINT_P23 = new Point(11, -7.7, 5.4);
    private final double[] QUAT_P23 = new double[]{0, 90, -180};

    // OTW P2-1
    private final Point POINT_P21 = new Point(10.4, -7.5, 4.7);
    private final double[] QUAT_P21 = new double[]{0, 0, -180};

    // OTW P2-2
    private final Point POINT_P22 = new Point(11.4, -8, 5);
    private final double[] QUAT_P22 = new double[]{0, 0, 0};

    // LEWATI RINTANGAN 2
    private final Point POINT2_AVOID_1 = new Point(11.063,-8.3,4.693);
    private final Point POINT2_AVOID_2 = new Point(11.063,-8.941,4.693);
    private final double[] QUAT2_AVOID_1 = new double[]{0, 0, -90};
    private final double[] QUAT2_AVOID_2 = new double[]{0, 0, -90};

    private java.util.ArrayList<String> QR_LIST = new java.util.ArrayList<>();
    private Robot robot;
    @Override
    protected void runPlan1(){
        // write here your plan 1
        api.judgeSendStart();

        moveRobot(POINT_P11, QUAT_P11);
        readQRBoofCV(0, "dock");

        moveRobot(POINT_P13_1, QUAT_P13_1);
        moveRobot(POINT_P13, QUAT_P13);
        readQRBoofCV(2, "dock");

        moveRobot(POINT_P12, QUAT_P12);
        readQRBoofCV(1, "nav");

        moveRobot(POINT1_AVOID_1, QUAT1_AVOID_1);
        moveRobot(POINT1_AVOID_2, QUAT1_AVOID_2);
        moveRobot(POINT1_AVOID_3, QUAT1_AVOID_3);

        moveRobot(POINT_P23, QUAT_P23);
        readQRBoofCV(5, "dock");

        moveRobot(POINT_P21, QUAT_P21);
        readQRBoofCV(3, "nav");

        moveRobot(POINT_P22, QUAT_P22);
        readQRBoofCV(4, "nav");

        moveRobot(POINT2_AVOID_1, QUAT2_AVOID_1);
        moveRobot(POINT2_AVOID_2, QUAT2_AVOID_2);

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

        if (pos_x != 0 && pos_y !=0 && pos_z != 0 && qua_x != 0 && qua_y != 0 && qua_z != 0 && qua_w != 0) {
            Point POINT_P3 = new Point(10.95, -9.63, 5.35);
            double[] QUAT_P3 = new double[]{0, 0, -90};
            moveRobot(POINT_P3, QUAT_P3);

            findAR();
        }
        api.judgeSendFinishSimulation();
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
        int LOOP_MAX = 16;
        Result result = null;
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

    public void findAR() {
        Mat mat = api.getMatNavCam();
        List<Mat> corners = new ArrayList<>();
        Mat ids = new Mat();
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
        try {
            Log.d(TAG, "DEBUG AR 1 CHECK");
            Aruco.detectMarkers(mat, dictionary, corners, ids);
            Log.d(TAG, "DEBUG AR 2 CHECK");
            double[] test_id = ids.get(0,0);
            int id = (int)test_id[0];
            Log.d(TAG, "DEBUG AR 3 CHECK");
            String ar_id = String.valueOf(id);
            Log.d(TAG, "DEBUG AR 4 CHECK");
            if (ar_id.length() > 0) {
                api.judgeSendDiscoveredAR(ar_id);
                Log.d(TAG, "FOUND AR ID: " + ar_id);
                Log.d(TAG, "FOUND AR ID: " + id);
                api.laserControl(true);
                double[] corner1 = corners.get(0).get(0,0);
                double[] corner2 = corners.get(0).get(0,1);
                double[] corner3 = corners.get(0).get(0,2);
                double[] corner4 = corners.get(0).get(0,3);

                Log.d(TAG, "Corner 1: " + Arrays.toString(corner1));
                Log.d(TAG, "Corner 2: " + Arrays.toString(corner2));
                Log.d(TAG, "Corner 3: " + Arrays.toString(corner3));
                Log.d(TAG, "Corner 4: " + Arrays.toString(corner4));

                AR_FOUND = true;
            }
            else {
                Log.d(TAG, "AR TAG NOT FOUND!");
            }
        } catch (Exception e){
            Log.d(TAG, "AR TAG ERROR: EXCEPTION OCCURRED! " + e.getMessage());
        }
    }

    public void readQRBoofCV(int nomer, String camBoof) {
        int LOOP_CHECKQR_MAX = 4;
        int loopCheckQRCount = 0;
        int rotate = 0;
        boolean QR_FOUND = false;
        while (loopCheckQRCount < LOOP_CHECKQR_MAX && !QR_FOUND){
            //selfPosition(nomer);
            Bitmap bBoof = null;
            if (camBoof.equals("dock")) {
                bBoof = api.getBitmapDockCam();
                Log.d(TAG, "BOOF: DEBUG 0 CHECK!");
                if (rotate == 1) bBoof = RotateBitmap(bBoof, 1);
                if (rotate == 2) bBoof = RotateBitmap(bBoof, 2);
                if (rotate == 3) bBoof = RotateBitmap(bBoof, 3);
            }
            else if (camBoof.equals("nav")) {
                bBoof = api.getBitmapNavCam();
                Log.d(TAG, "BOOF: DEBUG 0 CHECK!");
                if (rotate == 1) bBoof = RotateBitmap(bBoof, 1);
                if (rotate == 2) bBoof = RotateBitmap(bBoof, 2);
                if (rotate == 3) bBoof = RotateBitmap(bBoof, 3);
            }
            if (bBoof == null) Log.d(TAG, "BOOF: BITMAP IS NULL!");
            Bitmap resize = getResizedBitmap(bBoof, height/2, width/2);
            try {
                GrayU8 image = ConvertBitmap.bitmapToGray(resize, (GrayU8)null, null);
                Log.d(TAG, "BOOF: DEBUG 1 CHECK!");
                Log.d(TAG, "BOOF: DEBUG 2 CHECK!");
                QrCodeDetector<GrayU8> detector = FactoryFiducial.qrcode(null, GrayU8.class);
                Log.d(TAG, "BOOF: DEBUG 3 CHECK!");
                detector.process(image);
                Log.d(TAG, "BOOF: DEBUG 4 CHECK!");
                List<QrCode> detections = detector.getDetections();
                Log.d(TAG, "BOOF: DEBUG 5 CHECK!");
                Log.d(TAG, "BOOF: DEBUG 5.5 CHECK!");
                if (detections != null) {
                    for(QrCode qr : detections) {
                        Log.d(TAG, "BOOF: DEBUG 6 CHECK!");
                        if (qr.message.length() > 0) {
                            Log.d(TAG, "BOOF: DEBUG 7 CHECK!");
                            api.judgeSendDiscoveredQR(nomer, qr.message);
                            QR_FOUND = true;
                            TEST_FOUND = true;
                            QR_LIST.add(qr.message);
                            Log.d(TAG, "Posisi P3: " + qr.message);
                        }
                        else {
                            Log.d(TAG, "QR ERROR: NOT FOUND!");
                        }
                    }
                }
                else {
                    Log.d(TAG, "DETECTION NOT FOUND!");
                }

            } catch (Exception e) {
                Log.d(TAG, "EXCEPTION OCCURRED!: " + e.getMessage());
            }
            bBoof.recycle();
            resize.recycle();
            resize = null;
            rotate++;
            ++loopCheckQRCount;
        }

        if (!TEST_FOUND) {
            selfPosition(nomer);
            Log.d(TAG, "Trying with ZBar..");
            readQR_ZBar(nomer, camBoof);
        }
    }

    public void selfPosition(int n) {
        switch (n) {
            case 0:
                moveRobot(POINT_P11, QUAT_P11);
                break;
            case 1:
                moveRobot(POINT_P12, QUAT_P12);
                break;
            case 2:
                moveRobot(POINT_P13_1, QUAT_P13_1);
                moveRobot(POINT_P13, QUAT_P13);
                break;
            case 3:
                moveRobot(POINT_P21, QUAT_P21);
                break;
            case 4:
                moveRobot(POINT_P22, QUAT_P22);
                break;
            case 5:
                moveRobot(POINT_P23, QUAT_P23);
                break;
        }
    }

    public void readQR_ZBar(int zbar_number, String cam){
        ImageScanner scanner = new ImageScanner();
        int LOOP_CHECKQR_MAX = 4;
        int loopCheckQRCount = 0;
        int rotate = 0;
        boolean QR_ZBAR_FOUND = false;
        Log.d(TAG, "ZBAR: DEBUG 1 CHECK!");
        scanner.setConfig(0, Config.ENABLE, 0);
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
        Log.d(TAG, "ZBAR: DEBUG 2 CHECK!");
        while (loopCheckQRCount < LOOP_CHECKQR_MAX && !QR_ZBAR_FOUND) {
            Bitmap bmapzbar = null;
            if (cam.equals("dock")) {
                bmapzbar = api.getBitmapDockCam();
                if (rotate == 1) bmapzbar = RotateBitmap(bmapzbar, 1);
                if (rotate == 2) bmapzbar = RotateBitmap(bmapzbar, 2);
                if (rotate == 3) bmapzbar = RotateBitmap(bmapzbar, 3);

            }
            else if (cam.equals("nav")) {
                bmapzbar = api.getBitmapNavCam();
                if (rotate == 1) bmapzbar = RotateBitmap(bmapzbar, 1);
                if (rotate == 2) bmapzbar = RotateBitmap(bmapzbar, 2);
                if (rotate == 3) bmapzbar = RotateBitmap(bmapzbar, 3);
            }
            if (bmapzbar == null) {
                Log.d(TAG, "ZBAR: BITMAP IS NULL!");
            }
            Bitmap grayscaled = toGrayscale(bmapzbar);
            Bitmap scaled = getResizedBitmap(grayscaled, height/2, width/2);
            int[] intArray = new int[bmapzbar.getWidth() * bmapzbar.getHeight()];
            int wdth = scaled.getWidth();
            int hght = scaled.getHeight();
            Log.d(TAG, "New width bmp: " + wdth);
            Log.d(TAG, "New height bmp: " + hght);
            Log.d(TAG, "ZBAR: DEBUG 3 CHECK!");
            try {
                scaled.getPixels(intArray, 0, wdth, 0, 0, wdth, hght);
                Log.d(TAG, "ZBAR: DEBUG 4 CHECK!");
                Image barcode = new Image(wdth, hght, "RGB4");
                Log.d(TAG, "ZBAR: DEBUG 5 CHECK!");
                barcode.setData(intArray);
                Log.d(TAG, "ZBAR: DEBUG 6 CHECK!");
                int result_zbar = scanner.scanImage(barcode.convert("Y800"));
                Log.d(TAG, "ZBAR: DEBUG 7 CHECK!");
                if (result_zbar != 0) {
                    for (Symbol symbol_zbar : scanner.getResults()) {
                        Log.d(TAG, "ZBAR: DEBUG 8 CHECK!");
                        String qrContent_zbar = symbol_zbar.getData();
                        api.judgeSendDiscoveredQR(zbar_number, qrContent_zbar);
                        QR_ZBAR_FOUND = true;
                        TEST_FOUND = true;
                        QR_LIST.add(qrContent_zbar);
                        Log.d(TAG, "Found QR Content: " + qrContent_zbar);
                    }
                }
                else {
                    Log.d(TAG, "QR ERROR: NOT FOUND!");
                }
            } catch (Exception e) {
                Log.d(TAG, "EXCEPTION OCCURED! " + e.getMessage());
            }
            grayscaled.recycle();
            scaled.recycle();
            scaled = null;
            grayscaled = null;
            bmapzbar.recycle();
            ++loopCheckQRCount;
            rotate++;
        }
    }

    public Bitmap RotateBitmap(Bitmap src, int no) {
        float angle = 0;
        if (no == 1) angle = 90;
        if (no == 2) angle = 180;
        if (no == 3) angle = 270;
        Log.d(TAG, "Rotated: " + no + ", " + angle + " degree");
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
