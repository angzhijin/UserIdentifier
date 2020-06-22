package com.jza.mysensor3.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jza.mysensor3.R;
import com.jza.mysensor3.data.*;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SensorCollectActivity extends AppCompatActivity {

    String TAG = "SensorData";

    SensorManager mySensorManager; //SensorManager对象引用
    Sensor aSensor; //加速度传感器Accelerometer
    Sensor mSensor; //地磁传感器Magnetic_Field
    Sensor gSensor; //陀螺仪Gyroscope_uncalibrated
    Sensor rSensor; //旋转向量Rotation_Vector

    float[] accVals = new float[3];
    float[] magVals = new float[3];
    float[] gyrVals = new float[3];
    float[] oriVals = new float[3];
    float[] rotVals = new float[4];
    private boolean isRecord = false, isSensor = false;
    private int delay = 3;        //0 -- 停止记录/关闭中；1 -- 记录中/开启中；
    private String uid;
    private Long startTime = 1L, endTime = 1L, timeStamp = 1L;
    String[] oriForm = {"正北","东北","正东","东南","正南","西南","正西","西北"};
//    DecimalFormat df   = new DecimalFormat("######0.00######");

    private Chronometer chronometer = null;    //添加一个计时器，知道采集的时长；
    private Vibrator vibrator;  //振动
    private Button sensor, record;
    private Spinner delaySP, uidSP;
    private TextView myTextViewStartTime;      //StartTime
    private TextView myTextViewEndTime;      //EndTime
    private TextView myTextViewTimeSpan;    //TimeSpan
    private TextView myTextViewTimeStamp;   //TimeStamp
    private TextView myTextViewAccX;        //加速度x
    private TextView myTextViewAccY;        //加速度y
    private TextView myTextViewAccZ;        //加速度z
    private TextView myTextViewGyrX;        //陀螺仪x
    private TextView myTextViewGyrY;        //陀螺仪y
    private TextView myTextViewGyrZ;        //陀螺仪z
    private TextView myTextViewMagX;        //地磁x
    private TextView myTextViewMagY;        //地磁y
    private TextView myTextViewMagZ;        //地磁z
    private TextView myTextViewRotX;        //旋转向量x
    private TextView myTextViewRotY;        //旋转向量y
    private TextView myTextViewRotZ;        //旋转向量z
    private TextView myTextViewRot;        //旋转向量
    private TextView myTextViewOriX;
    private TextView myTextViewOriY;
    private TextView myTextViewOriZ;
    private TextView myTextViewOriX2;
    private TextView myTextViewOriY2;
    private TextView myTextViewOriZ2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_collect);

        initVariables();
        initButton();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
//        unregisterSensor();
        super.onPause();
    }

    protected void onDestroy() {
//        unregisterSensor();
        super.onDestroy();
    }

    private void initVariables() {
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        sensor = (Button)findViewById(R.id.sensor);
        record = (Button)findViewById(R.id.record);
        delaySP = (Spinner)findViewById(R.id.delaySpinner);
        uidSP= (Spinner) findViewById(R.id.uidSpinner);
        myTextViewStartTime = (TextView)findViewById(R.id.STtext);
        myTextViewEndTime = (TextView)findViewById(R.id.ETtext);
        myTextViewTimeSpan = (TextView)findViewById(R.id.TimeSpantext);
        myTextViewTimeStamp = (TextView)findViewById(R.id.TimeStamptext);
        myTextViewAccX = (TextView)findViewById(R.id.aX);
        myTextViewAccY = (TextView)findViewById(R.id.aY);
        myTextViewAccZ = (TextView)findViewById(R.id.aZ);
        myTextViewGyrX = (TextView)findViewById(R.id.gX);
        myTextViewGyrY = (TextView)findViewById(R.id.gY);
        myTextViewGyrZ = (TextView)findViewById(R.id.gZ);
        myTextViewMagX = (TextView)findViewById(R.id.mX);
        myTextViewMagY = (TextView)findViewById(R.id.mY);
        myTextViewMagZ = (TextView)findViewById(R.id.mZ);
        myTextViewOriX = (TextView)findViewById(R.id.oX);
        myTextViewOriY = (TextView)findViewById(R.id.oY);
        myTextViewOriZ = (TextView)findViewById(R.id.oZ);
        myTextViewOriX2 = (TextView)findViewById(R.id.oX2);
        myTextViewOriY2 = (TextView)findViewById(R.id.oY2);
        myTextViewOriZ2 = (TextView)findViewById(R.id.oZ2);
        myTextViewRotX = (TextView)findViewById(R.id.rX);
        myTextViewRotY = (TextView)findViewById(R.id.rY);
        myTextViewRotZ = (TextView)findViewById(R.id.rZ);
        myTextViewRot = (TextView)findViewById(R.id.rr);

        List<String> uidsplist = new ArrayList<String>();
        List<UserData> userlist = UserData.getAllUserList();
        for(UserData user : userlist){
            uidsplist.add(user.getUid());
        }
        ArrayAdapter<String> uidadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,uidsplist);
        uidadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uidSP.setAdapter(uidadapter);
    }



    private void initButton(){
        sensor.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(isSensor==false  && isRecord==false ){
                    //传感器启动
                    isSensor = true;
                    delay = 3 - uidSP.getSelectedItemPosition();
                    registerSensor(SensorManager.SENSOR_DELAY_NORMAL);
                    sensor.setText("关闭传感器");
                    chronometer.setTextColor(chronometer.getResources().getColor(R.color.green));
                }
                else if(isSensor==true && isRecord==false){
                    //传感器关闭
                    isSensor = false;
                    unregisterSensor();
                    sensor.setText("启动传感器");

                    chronometer.setTextColor(chronometer.getResources().getColor(R.color.red));
                } else {
                    Toast.makeText(SensorCollectActivity.this, "请先停止记录", Toast.LENGTH_SHORT).show();
                }
            }
        });
        record.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                /*
                    自采集
                 */
//                if(isRecord==false && isSensor==true){
//                    //开始记录
//                    isRecord = true;
//                    Calendar calendar = Calendar.getInstance();
//                    startTime = System.currentTimeMillis();
//                    uid = uidSP.getSelectedItem().toString();
//                    record.setText("停止记录");
//                    record.setBackgroundResource(R.drawable.button_circle_shape2);
//                    recordLock(true);
//                }
//                else if(isRecord==true && isSensor==true){
//                    //停止记录
//                    isRecord = false;
//                    Calendar calendar = Calendar.getInstance();
//                    endTime = System.currentTimeMillis();
//                    myTextViewStartTime.setText(CollectInfoData.getDateTimeFromMillisecond(startTime));
//                    myTextViewEndTime.setText(CollectInfoData.getDateTimeFromMillisecond(endTime));
//                    myTextViewTimeSpan.setText(CollectInfoData.getTimeSpan(startTime,endTime)+" ms");
//                    record.setText("开始记录");
//                    record.setBackgroundResource(R.drawable.button_circle_shape);
//                    recordLock(false);
//                    vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
//                    vibrator.vibrate(100);
//
//                    //记录InfoData
//                    long timespan = CollectInfoData.getTimeSpan(startTime,endTime);
//                    if(timespan < 700 || timespan > 2000){
//                        Toast.makeText(SensorCollectActivity.this, "数据错误，未录入", Toast.LENGTH_SHORT).show();
//                        Log.i(TAG,"Error Data");
//                    }
//                    else{
//                        CollectInfoData infoData = new CollectInfoData();
//                        infoData.setStarttime(startTime);
//                        infoData.setEndtime(endTime);
//                        infoData.setTimespan(timespan);
//                        infoData.setSensorDelay(delay);
//                        infoData.setUid(uid);
//                        infoData.save();
//                        Log.i(TAG,"insert infoData -- " + startTime + " - " + endTime + " -- " + delay + " -- " + uid);
//                    }
//                } else {
//                    Toast.makeText(SensorCollectActivity.this, "请先启动传感器", Toast.LENGTH_SHORT).show();
//                }



                /*
                    定时采集
                 */
                if(!isSensor){
                    Toast.makeText(SensorCollectActivity.this, "请先启动传感器", Toast.LENGTH_SHORT).show();
                }
                else{
                    isRecord = true;
                    Calendar calendar = Calendar.getInstance();
                    startTime = System.currentTimeMillis();
                    uid = uidSP.getSelectedItem().toString();
                    record.setText("停止记录");
                    record.setBackgroundResource(R.drawable.button_circle_shape2);
                    record.setEnabled(false);
                    recordLock(true);

                    CountDownTimer timer = new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //还剩下多少秒millisUntilFinished/1000，依次为2、1、0
                        }

                        @Override
                        public void onFinish() {//结束后的操作
                            isRecord = false;
                            endTime = System.currentTimeMillis();
                            myTextViewStartTime.setText(CollectInfoData.getDateTimeFromMillisecond(startTime));
                            myTextViewEndTime.setText(CollectInfoData.getDateTimeFromMillisecond(endTime));
                            myTextViewTimeSpan.setText(CollectInfoData.getTimeSpan(startTime,endTime)+" ms");
                            record.setText("开始记录");
                            record.setBackgroundResource(R.drawable.button_circle_shape);
                            record.setEnabled(true);
                            recordLock(false);
                            vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(100);

                            //记录InfoData
                            long timespan = CollectInfoData.getTimeSpan(startTime,endTime);
                                CollectInfoData infoData = new CollectInfoData();
                                infoData.setStarttime(startTime);
                                infoData.setEndtime(endTime);
                                infoData.setTimespan(timespan);
                                infoData.setSensorDelay(delay);
                                infoData.setUid(uid);
                                infoData.save();
                                Log.i(TAG,"insert infoData -- " + startTime + " - " + endTime + " -- " + delay + " -- " + uid);

                        }
                    }.start();
                }
            }
        });

    }

    private void recordLock(boolean isEnlock){
        if (isEnlock == true){
            sensor.setEnabled(false);
            uidSP.setEnabled(false);
        } else{
            sensor.setEnabled(true);
            uidSP.setEnabled(true);
        }
    }

    private void registerSensor(int delay){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        rSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //delay = 0SensorManager.SENSOR_DELAY_FASTEST/1GAME/2UI/3NORMAL
        mySensorManager.registerListener(mySensorListener, aSensor, delay);
        mySensorManager.registerListener(mySensorListener, mSensor, delay);
        mySensorManager.registerListener(mySensorListener, gSensor, delay);
        mySensorManager.registerListener(mySensorListener, rSensor, delay);

    }

    private void unregisterSensor(){
        mySensorManager.unregisterListener(mySensorListener);
        chronometer.stop();
    }

    private SensorEventListener mySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                timeStamp = System.currentTimeMillis();
                myTextViewTimeStamp.setText(CollectInfoData.getDateTimeFromMillisecond(timeStamp));
                accVals = event.values;
                myTextViewAccX.setText(" " + accVals[0] );
                myTextViewAccY.setText(" " + accVals[1]);
                myTextViewAccZ.setText(" " + accVals[2]);
                if(isRecord) {
                    //记录AccData到DB
                    SensorData accData = new SensorData();
                    accData.setSensorType(Sensor.TYPE_ACCELEROMETER);
                    accData.setTimeStamp(timeStamp);
                    accData.setX(accVals[0]);
                    accData.setY(accVals[1]);
                    accData.setZ(accVals[2]);
                    if(accData.save())
                        Log.v(TAG,"insert accData -" +
                                Sensor.TYPE_ACCELEROMETER + "-- " +
                                timeStamp + "|" +
                                accVals[0] + "|" +
                                accVals[1] + "|" +
                                accVals[2] +"  success");
                    else
                        Log.v(TAG,"insert accData -- " + timeStamp + "  fail");
                }
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED){
                timeStamp = System.currentTimeMillis();
                gyrVals = event.values;
                myTextViewGyrX.setText(" " + gyrVals[0]);
                myTextViewGyrY.setText(" " + gyrVals[1]);
                myTextViewGyrZ.setText(" " + gyrVals[2]);
                if(isRecord){
                    //记录GryData到DB
                    SensorData gyrData = new SensorData();
                    gyrData.setSensorType(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
                    gyrData.setTimeStamp(timeStamp);
                    gyrData.setX(gyrVals[0]);
                    gyrData.setY(gyrVals[1]);
                    gyrData.setZ(gyrVals[2]);
                    if(gyrData.save())
                        Log.v(TAG,"insert gyrData -" +
                                Sensor.TYPE_GYROSCOPE_UNCALIBRATED + "-- " +
                                timeStamp + "|" +
                                gyrVals[0] + "|" +
                                gyrVals[0] + "|" +
                                gyrVals[0] + "  success");
                    else
                        Log.v(TAG,"insert gyrData -- " + timeStamp + "  fail");

                }
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                timeStamp = System.currentTimeMillis();
                magVals = event.values;
                myTextViewMagX.setText(" " + magVals[0]);
                myTextViewMagY.setText(" " + magVals[1]);
                myTextViewMagZ.setText(" " + magVals[2]);
                if(isRecord){
                    //记录MagData到DB
                    SensorData magData = new SensorData();
                    magData.setSensorType(Sensor.TYPE_MAGNETIC_FIELD);
                    magData.setTimeStamp(timeStamp);
                    magData.setX(magVals[0]);
                    magData.setY(magVals[1]);
                    magData.setZ(magVals[2]);
                    if(magData.save())
                        Log.v(TAG,"insert magData -" +
                                Sensor.TYPE_MAGNETIC_FIELD + "-- " +
                                timeStamp + "|" +
                                magVals[0] + "|" +
                                magVals[1] + "|" +
                                magVals[2] + "  success");
                    else
                        Log.v(TAG,"insert magData -- " + timeStamp + "  fail");
                }
            }
            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                timeStamp = System.currentTimeMillis();
                float[] mRotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(mRotationMatrix,event.values);
                SensorManager.getOrientation(mRotationMatrix,rotVals);
                rotVals[0] = (float)Math.toDegrees(rotVals[0]);
                rotVals[1] = (float)Math.toDegrees(rotVals[1]);
                rotVals[2] = (float)Math.toDegrees(rotVals[2]);
                rotVals[3] = (float)Math.toDegrees(rotVals[3]);
                myTextViewRotX.setText(" " + rotVals[0]);
                myTextViewRotY.setText(" " + rotVals[1]);
                myTextViewRotZ.setText(" " + rotVals[2]);
                myTextViewRot.setText(" " + rotVals[3]);
                if(isRecord){
                    //记录MagData到DB
                    SensorData rotData = new SensorData();
                    rotData.setSensorType(Sensor.TYPE_ROTATION_VECTOR);
                    rotData.setTimeStamp(timeStamp);
                    rotData.setX(magVals[0]);
                    rotData.setY(magVals[1]);
                    rotData.setZ(magVals[2]);
                    if(rotData.save())
                        Log.v(TAG,"insert rotData -" +
                                Sensor.TYPE_ROTATION_VECTOR + "-- " +
                                timeStamp + "|" +
                                rotVals[0] + "|" +
                                rotVals[1] + "|" +
                                rotVals[2] + "  success");
                    else
                        Log.v(TAG,"insert rotData -- " + timeStamp + "  fail");
                }
            }
            calculateOrientation();

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {        }
    };

    private void calculateOrientation(){
        ///陀螺仪数值转换到角度
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accVals, magVals);
        SensorManager.getOrientation(R, oriVals);

        //要经过一次数据格式的转换，转换为度
        oriVals[0] = (float)Math.toDegrees(oriVals[0]);
        oriVals[1] = (float)Math.toDegrees(oriVals[1]);
        oriVals[2] = (float)Math.toDegrees(oriVals[2]);
//        Log.d(SensorData.TAG, "方位" + oriVals[0] + " ");
//        Log.d(SensorData.TAG, "方位" + oriVals[1] + " ");
//        Log.d(SensorData.TAG, "方位" + oriVals[2] + " ");

        myTextViewOriX.setText(" " + oriVals[0]);
        myTextViewOriY.setText(" " + oriVals[1]);
        myTextViewOriZ.setText(" " + oriVals[2]);
        myTextViewOriX2.setText(" " + oriForm[oriTransform(oriVals[0])-1]);
        myTextViewOriY2.setText(" " + oriForm[oriTransform(oriVals[1])-1]);
        myTextViewOriZ2.setText(" " + oriForm[oriTransform(oriVals[2])-1]);

    }

    /**
     * 方向数据转化为八个方位
     * @param value
     * @return
     */
    private int oriTransform(float value){
        ///转换到方位
        if(value >= -5 && value < 5){
//            Log.d(SensorData.TAG, value + " -> 正北");
            return 1;
        }
        else if(value >= 5 && value < 85){
//            Log.d(SensorData.TAG, value + " -> 东北");
            return 2;
        }
        else if(value >= 85 && value <=95){
//            Log.d(SensorData.TAG, value + " -> 正东");
            return 3;
        }
        else if(value >= 95 && value <175){
//            Log.d(SensorData.TAG, value + " -> 东南");
            return 4;
        }
        else if((value >= 175 && value <= 180) || (value) >= -180 && value < -175){
//            Log.d(SensorData.TAG, value + " -> 正南");
            return 5;
        }
        else if(value >= -175 && value <-95){
//            Log.d(SensorData.TAG, value + " -> 西南");
            return 6;
        }
        else if(value >= -95 && value < -85){
//            Log.d(SensorData.TAG, value + " -> 正西");
            return 7;
        }
        else if(value >= -85 && value <-5){
//            Log.d(SensorData.TAG, value + " -> 西北");
            return 8;
        }
        return 0;
    }



}
