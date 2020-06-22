package com.jza.mysensor3.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.Edits;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jza.mysensor3.R;
import com.jza.mysensor3.data.CollectInfoData;
import com.jza.mysensor3.data.SensorData;
import com.jza.mysensor3.data.UserData;
import com.jza.mysensor3.svmtools.svm_predict;
import com.jza.mysensor3.svmtools.svm_train;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;

import static java.lang.Double.parseDouble;

public class IdentifierActivity extends AppCompatActivity {

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

    String TAG = "DBView";
    int[] sensortype = new int[]{Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE_UNCALIBRATED, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ROTATION_VECTOR};
    String[] filenameList = new String[]{"AccX.txt","AccY.txt","AccZ.txt",
            "GyrX.txt","GyrY.txt","GyrZ.txt",
            "MagX.txt","MagY.txt","MagZ.txt",
            "RotX.txt","RotY.txt","RotZ.txt"};
    String[] dataType = new String[]{"AccX","AccY","AccZ",
            "GyrX","GyrY","GyrZ",
            "MagX","MagY","MagZ",
            "RotX","RotY","RotZ"};

    private Vibrator vibrator;  //振动
    private Button identifytest, train, whoami;
    private TextView resTextView;

    private boolean isIdentify = false;
    private int accindex, gyrindex, magindex, rotindex = 1;
    private int datatype_size = dataType.length;
    private Long starttime = 1L, endtime = 1L;
    private List<String> mlist = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identifier);


        initVaribles();
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


    public void initVaribles(){
        identifytest = (Button)findViewById(R.id.idytestBtn);
        train = (Button)findViewById(R.id.trainBtn);
        whoami = (Button)findViewById(R.id.whoamiBtn);
        resTextView = (TextView)findViewById(R.id.idfTextView);

        for(int i =0; i<12;i++){
            mlist.add("0");
        }
    }

    public void initButton(){
        identifytest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    libsvm_test_all();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBViewActivity.exportOut(TAG, filenameList, sensortype);
                try{
                    trainmodel();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        whoami.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化特征列表
                /*
                    自采集
                 */
//                if(!isIdentify){
//                    for(int i =0; i<12;i++){
//                        mlist.set(i,"0");
//                    }
//                    isIdentify = true;
//                    accindex=1; gyrindex=1; magindex=1; rotindex = 1;
//                    whoami.setBackgroundResource(R.drawable.button_circle_shape2);
//                    registerSensor(SensorManager.SENSOR_DELAY_NORMAL);
//                }
//                else{
//                    isIdentify = false;
//                    unregisterSensor();
//                    vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
//                    vibrator.vibrate(100);
//                    whoami.setBackgroundResource(R.drawable.button_circle_shape);
//                    List<UserData> userlist = UserData.getAllUserList();
//                    int usernum = userlist.size();
//                    double[] res = predictall(mlist, userlist);
//
//                    //把结果放入结果计数器
//                    int[] res2 = new int[usernum];
//                    for(int i=0;i<datatype_size;i++){
//                        for(int j=0;j<usernum;j++){
//                            if(res[i] == parseDouble(userlist.get(j).getUid())){
//                                res2[j] += 1;
//                                break;
//                            }
//                        }
//                    }
//                    //从结果计数器中取出最大
//                    int max = res2[0];
//                    int max_id = 0;
//                    for(int i=0;i<usernum;i++){
//                        if(res2[i] > max){
//                            max = res2[i];
//                            max_id = i;
//                        }
//                    }
//
//                    int finalid = max_id;
//                    String final_uid = userlist.get(finalid).getUid();
//                    String final_name = userlist.get(finalid).getName();
//
//                    AlertDialog.Builder dialog = new AlertDialog.Builder(IdentifierActivity.this);
//                    dialog.setTitle("The Result");
//                    if(finalid == 0){
//                        dialog.setIcon(R.drawable.error_icon);
//                        dialog.setMessage("Error! \n" + final_name + "(" + final_uid + ") -- " + max + "/" + datatpe_size);
//                    }
//                    else{
//                        dialog.setIcon(R.drawable.right_icon);
//                        dialog.setMessage("Welcome! \n" + final_name + "(" + final_uid + ") -- " + max + "/" + datatype_size);
//                    }
//                    dialog.setCancelable(false);
//                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    });
//                    dialog.show();
//                }

                /*
                    定时采集
                 */
                for(int i =0; i<12;i++){
                    mlist.set(i,"0");
                }
                isIdentify = true;
                accindex=1; gyrindex=1; magindex=1; rotindex = 1;
                whoami.setBackgroundResource(R.drawable.button_circle_shape2);
                whoami.setEnabled(false);
                resTextView.setText("");
                registerSensor(SensorManager.SENSOR_DELAY_NORMAL);

                CountDownTimer timer = new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //还剩下多少秒millisUntilFinished/1000，依次为2、1、0
                    }

                    @Override
                    public void onFinish() {//结束后的操作
                        isIdentify = false;
                        unregisterSensor();
                        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate(100);
                        whoami.setBackgroundResource(R.drawable.button_circle_shape);
                        whoami.setEnabled(true);

                        List<UserData> userlist = UserData.getAllUserList();
                        int usernum = userlist.size();
                        //时间序列分类
                        double[] res = predictall(mlist, userlist);

                        datatype_size -= 3;
                        //把结果放入结果计数器,并显示
                        int[] res2 = new int[usernum];
                        for(int i=0;i<datatype_size;i++){
                            for(int j=0;j<usernum;j++){
                                if(res[i] == parseDouble(userlist.get(j).getUid())){
                                    resTextView.append(dataType[i] + " - " + userlist.get(j).getUid() + "\n");
                                    res2[j] += 1;
                                    break;
                                }
                            }
                        }
                        //从结果计数器中取出最大
                        int max = 0;
                        int max_id = 0;
                        for(int i=1;i<usernum;i++){
                            if(res2[i] > max && res2[i] > 3){
                                max = res2[i];
                               max_id = i;
                            }
                        }
                        if(max == 0) max = res2[0];

                        int finalid = max_id;
                        String final_uid = userlist.get(finalid).getUid();
                        String final_name = userlist.get(finalid).getName();

                        AlertDialog.Builder dialog = new AlertDialog.Builder(IdentifierActivity.this);
                        dialog.setTitle("The Result");
                        if(finalid == 0){
                            dialog.setIcon(R.drawable.error_icon);
                            dialog.setMessage("Who are you! \n" + final_name + "(" + final_uid + ") -- " + max + "/" + datatype_size);
                        }
                        else{
                            dialog.setIcon(R.drawable.right_icon);
                            dialog.setMessage("Welcome! \n" + final_name + "(" + final_uid + ") -- " + max + "/" + datatype_size);
                        }
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog.show();
                        datatype_size = dataType.length;
                    }
                }.start();


            }
        });
    }



    private void registerSensor(int delay){
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
    }

    private SensorEventListener mySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accVals = event.values;
                if(isIdentify) {
                    mlist.set(0,mlist.get(0) + "\t" + accindex + ":" + accVals[0]);
                    mlist.set(1,mlist.get(1) + "\t" + accindex + ":" + accVals[1]);
                    mlist.set(2,mlist.get(2) + "\t" + accindex + ":" + accVals[2]);
                }
                accindex++;
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED){
                gyrVals = event.values;
                if(isIdentify) {
                    mlist.set(3,mlist.get(3)+"\t" + gyrindex + ":" + gyrVals[0]);
                    mlist.set(4,mlist.get(4)+"\t" + gyrindex + ":" + gyrVals[1]);
                    mlist.set(5,mlist.get(5)+"\t" + gyrindex + ":" + gyrVals[2]);
                }
                gyrindex++;
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                magVals = event.values;
                if(isIdentify) {
                    mlist.set(6,mlist.get(6)+"\t" + magindex + ":" + magVals[0]);
                    mlist.set(7,mlist.get(7)+"\t" + magindex + ":" + magVals[1]);
                    mlist.set(8,mlist.get(8)+"\t" + magindex + ":" + magVals[2]);
                }
                magindex++;
            }
            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                float[] mRotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(mRotationMatrix,event.values);
                SensorManager.getOrientation(mRotationMatrix,rotVals);
                rotVals[0] = (float)Math.toDegrees(rotVals[0]);
                rotVals[1] = (float)Math.toDegrees(rotVals[1]);
                rotVals[2] = (float)Math.toDegrees(rotVals[2]);
                rotVals[3] = (float)Math.toDegrees(rotVals[3]);
                if(isIdentify) {
                    mlist.set(9,mlist.get(9)+"\t" + rotindex + ":" + rotVals[0]);
                    mlist.set(10,mlist.get(10)+"\t" + rotindex + ":" + rotVals[1]);
                    mlist.set(11,mlist.get(11)+"\t" + rotindex + ":" + rotVals[2]);
                }
                rotindex++;
            }

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {        }
    };

    public void libsvm_test(String datatype) throws IOException {
        String dirpath = Environment.getExternalStorageDirectory()+ "/Sensor3/Export";
        String[] arg = { dirpath + "/train/"+ datatype+".txt", // 存放SVM训练模型用的数据的路径
                dirpath + "/model/model_"+datatype+".txt" }; // 存放SVM通过训练数据训/ //练出来的模型的路径
        String[] parg = { dirpath + "/test/" + datatype +".txt", // 这个是存放测试数据
                dirpath + "/model/model_"+datatype+".txt", // 调用的是训练以后的模型
                dirpath + "/out/out_"+datatype+".txt" }; // 生成的结果的文件的路径

        System.out.println("........SVM运行开始.........."+ datatype );

        svm_train t = new svm_train();// 创建一个预测或者分类的对象

        svm_predict p = new svm_predict();

        t.main(arg); // 调用训练

        p.main(parg); // 调用测试
    }

    public void libsvm_test_all() throws IOException{
        for(int i=0;i<datatype_size;i++){
            libsvm_test(dataType[i]);
        }
    }

    public void trainmodel() throws IOException{
        String dirpath = Environment.getExternalStorageDirectory()+ "/Sensor3/Export";
        for(int i=0;i<datatype_size;i++){
            String trainpath = dirpath + "/train/" + dataType[i] + ".txt";
            String modelpath = dirpath + "/model/model_" + dataType[i] +".txt";

            String arg[] = {trainpath,modelpath};
            svm_train t = new svm_train();
            t.main(arg);
        }
        Toast.makeText(this, "模型训练完毕", Toast.LENGTH_SHORT).show();
    }
/*
 *根据训练模型，判断单次动作
 */
    public double[] predictall(List<String> predicted_list, List<UserData> userList) {
        int usernum = userList.size();
        /* id | num
         *
         *
         */
        Map<Double,Integer> res_predict = new TreeMap<>();
        for(UserData user: userList){
            res_predict.put(parseDouble(user.getUid()),0);
        }

        double[] resArray = new double[datatype_size];

        String dirpath = Environment.getExternalStorageDirectory()+ "/Sensor3/Export";
        //依次判断九种时间序列特征，放入结果列表
        for(int i=0;i<datatype_size;i++){

            String modelpath = dirpath + "/model/model_" + dataType[i] +".txt";

            //待判断的时间序列，及模型路径
            String parg[] = {predicted_list.get(i), modelpath};
            Log.i("Identifier",dataType[i] + " - " + predicted_list.get(i));
            svm_predict p = new svm_predict();
            double res = p.predict2(parg);
            resArray[i] = res;
            Log.i("Identifier",dataType[i] + " - " + res);
            if(res_predict.containsKey(res)){
                int value = res_predict.get(res);
                res_predict.put(res,value+1);
            }
            else{
                Log.i("Identifier","Map 中没有目标");
            }
        }
        return resArray;
//        int maxres = 0;
//        double resuid = 0.0;
//        String resuidStr = "";
//        Iterator iter = res_predict.keySet().iterator();
//        while(iter.hasNext()){
//            Object key=iter.next();
//            Log.i("Identifier",key.toString()+ " -- "+res_predict.get(key));
//            if(res_predict.get(key) > maxres){
//                maxres = res_predict.get(key);
//                resuidStr = key.toString();
//            }
//        }
//        Log.i("Identifier",maxres/12+"");
//
//        if(resuidStr == "0.0"){
////            Toast.makeText(this, "not my master !" + resuidStr, Toast.LENGTH_LONG).show();
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setTitle("The Result");
//            dialog.setIcon(R.drawable.error_icon);
//            dialog.setMessage("Who are you? \n" + resuidStr + " -- " + maxres +"/9");
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            dialog.show();
//        }
//        else{
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setTitle("The Result");
//            dialog.setIcon(R.drawable.right_icon);
//            dialog.setMessage("Welcome! \n" + resuidStr + " -- " + maxres + "/9");
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            dialog.show();
//        }



//        if(((float)maxres)/9.0 > 0.5){
////            Toast.makeText(this, "Welcome " + resuid, Toast.LENGTH_LONG).show();
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setTitle("The Result");
//            dialog.setIcon(R.drawable.right_icon);
//            dialog.setMessage("Welcome, " + resuidStr);
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            dialog.show();
//
//        }
//        else if(){
////            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show();
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setTitle("The Result");
//            dialog.setIcon(R.drawable.right_icon);
//            dialog.setMessage("Welcome, " + resuid);
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            dialog.show();
//        }

    }
}
