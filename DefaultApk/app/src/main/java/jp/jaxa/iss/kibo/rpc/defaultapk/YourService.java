package jp.jaxa.iss.kibo.rpc.defaultapk;

import java.lang.Math;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    // roll (x), pitch (y), yaw (z)
    private double roll, pitch, yaw;
    private double qua_w, qua_x, qua_y, qua_z;
    
    // OTW P1-1, orientasi naik 11.31 derajat
    private final Point POINT_1_P11 = new Point(10.95, -3.95, 4.85);
    private final Point POINT_2_P11 = new Point(11.28, -3.95, 4.85);
    private final Point POINT_3_P11 = new Point(11.28, -3.95, 4.85);
    private final Point POINT_4_P11 = new Point(11.28, -5.7, 4.5);
    private final Point POINT_5_P11 = new Point(11.28, -5.7, 4.5);
    private double[] EULER_1_P11_YAW = new double[]{0, 0, -90};
    private double[] EULER_2_P11_YAW = new double[]{0, 0, 0};
    private double[] EULER_3_P11_YAW = new double[]{0, 0, -90};
    private double[] EULER_4_P11_ROLL = new double[]{-11.31, 0, 0};
    private double[] EULER_5_P11_YAW = new double[]{0, 0, 0};
    
    // OTW P1-2
    
    
    private Point[] arrayPoint = null;
    private Quaternion[] arrayQuaternion = null;
    private double[][] arrayEuler = null;

    @Override
    protected void runPlan1(){
        // write here your plan 1
        Result result = null;
        final int LOOP_MAX = 3;
        arrayPoint = new Point[] {POINT_1_P11, POINT_2_P11, POINT_3_P11, POINT_4_P11, POINT_5_P11};
        arrayEuler = new double[][] {EULER_1_P11_YAW, EULER_2_P11_YAW, EULER_3_P11_YAW, EULER_4_P11_ROLL, EULER_5_P11_YAW};
        
        for (int i = 0; i < arrayPoint.length; i++) {
            qua_w = 0;
            qua_x = 0;
            qua_y = 0;
            qua_z = 0;
            
            final Quaternion quaternion = eulerToQuaternion(arrayEuler[i]);
            final Point point = arrayPoint[i];
            result = api.moveTo(point, quaternion, true);
            // api.stopAllMotion();
            
            int loopCounter = 0;
            while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;
            }
        }
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

