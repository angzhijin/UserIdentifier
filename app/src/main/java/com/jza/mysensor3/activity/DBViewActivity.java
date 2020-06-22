package com.jza.mysensor3.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jza.mysensor3.R;
import com.jza.mysensor3.SensorOpenHelper;
import com.jza.mysensor3.data.CollectInfoData;
import com.jza.mysensor3.data.SensorData;
import com.jza.mysensor3.data.UserData;

import org.litepal.LitePal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DBViewActivity extends AppCompatActivity {

    String TAG = "DBView";
    int[] sensortype = new int[]{Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE_UNCALIBRATED, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ROTATION_VECTOR};
    String[] filenameList = new String[]{"AccX.txt","AccY.txt","AccZ.txt",
                                    "GyrX.txt","GyrY.txt","GyrZ.txt",
                                    "MagX.txt","MagY.txt","MagZ.txt",
                                    "RotX.txt","RotY.txt","RotZ.txt"};

    private Spinner uidsp = null, typesp, dimensionsp, delaysp;
    private Button search, export, delete, clear;
    private TextView DBview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbview);

        initVariables();
        initButton();
    }

    public void initVariables(){
        uidsp = (Spinner)findViewById(R.id.uidSP);
        typesp = (Spinner)findViewById(R.id.typeSp);
        dimensionsp = (Spinner)findViewById(R.id.dimensionSP);
        delaysp = (Spinner)findViewById(R.id.delaySP);
        search = (Button)findViewById(R.id.searchBtn);
        export = (Button)findViewById(R.id.exportBtn);
        delete = (Button)findViewById(R.id.delateBtn);
        clear = (Button)findViewById(R.id.clearBtn);
        DBview = (TextView)findViewById(R.id.DBTextView);

        List<String> uidsplist = new ArrayList<String>();
        uidsplist.add("*");
        List<UserData> userlist = UserData.getAllUserList();
        for(UserData user : userlist){
            uidsplist.add(user.getUid());
        }
        ArrayAdapter<String> uidadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,uidsplist);
        uidadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uidsp.setAdapter(uidadapter);

    }

    public void initButton(){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBview.setText("");
                String suid = uidsp.getSelectedItem().toString();
                int stype = sensortype[typesp.getSelectedItemPosition()];
                int sdimension = dimensionsp.getSelectedItemPosition();
                int sdelay = 3 - delaysp.getSelectedItemPosition();
                Log.i(TAG, "searching: uid - " + suid +
                        ", sensor_type = " + stype +
                        ", sensor_dimension = " + sdimension +
                        ", sensor_delay = " + sdelay);

                List<CollectInfoData> sInfoDatalist;
                List<UserData> sUserDatalist = UserData.getAllUserList();
                if(suid == "*"){
                    sInfoDatalist = CollectInfoData.getAllInfoList();
                }
                else{
                    sInfoDatalist = CollectInfoData.getInfoByUiddelay(suid,sdelay);
                }
                if(sInfoDatalist.isEmpty()){
                    DBview.append("NULL");
                }
                else{
                    int listnum = sInfoDatalist.size();
                    DBview.append("共有数据 " + listnum + "条:" );
                    for(UserData user:sUserDatalist){
                        DBview.append("\t" + user.getUid() + "-" + CollectInfoData.getInfoByUiddelay(user.getUid(),sdelay).size() + "条");
                    }
                    DBview.append("\n");
                    for(CollectInfoData infodata : sInfoDatalist){
                        DBview.append(infodata.getUid());
                        DBview.append("\t| " + CollectInfoData.getDateTimeFromMillisecond(infodata.getStarttime()));
                        DBview.append("\t| " + infodata.getTimespan());
                        List<SensorData> sSensorDatalist = SensorData.getSensorDataByInfo(infodata, stype);
                        for(SensorData sensordata : sSensorDatalist){
                            DBview.append(" | " + sensordata.getXYZ(sdimension));
                        }
                        DBview.append("\n");
                    }
                }

            }
        });
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportOut(TAG, filenameList, sensortype);
                Toast.makeText(DBViewActivity.this, "导出成功", Toast.LENGTH_SHORT).show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(CollectInfoData.class, "uid <> ?", "000000");
                Toast.makeText(DBViewActivity.this, "清除成功,请重新查询", Toast.LENGTH_SHORT).show();
                if(UserData.getAllUserList().isEmpty()){
                    UserData unknownuser = new UserData();
                    unknownuser.setUid("000000");
                    unknownuser.setName("Unknown");
                    unknownuser.save();
                }
            }

        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(CollectInfoData.class);
                LitePal.deleteAll(SensorData.class);
                Toast.makeText(DBViewActivity.this, "清空成功，请重新查询", Toast.LENGTH_SHORT).show();
                if(UserData.getAllUserList().isEmpty()){
                    UserData unknownuser = new UserData();
                    unknownuser.setUid("000000");
                    unknownuser.setName("Unknown");
                    unknownuser.save();
                }
            }
        });
    }

    public static void exportOut(String TAG, String[] filenameList, int[] sensortype){
        File _ExportDir = new File(Environment.getExternalStorageDirectory()+ "/Sensor3/Export/");
        if (!_ExportDir.exists()) {
            _ExportDir.mkdirs();
            Log.i(TAG,"创建 " + Environment.getExternalStorageDirectory() +"/Sensor3/Export/ 成功");
        }

        //导出UserData
        List<UserData> allUserDataList = UserData.getAllUserList();
//        if(UserData.Export(_ExportDir, allUserDataList)){
////            Log.i(TAG, "存储 userdata 成功");
////            Toast.makeText(MainActivity.this, "存储 userdata 成功", Toast.LENGTH_SHORT).show();
//        }

        //导出InfoData
        List<CollectInfoData> allInfoDataList = CollectInfoData.getAllInfoList();
//        if(CollectInfoData.Export(_ExportDir, allInfoDataList)){
////            Log.i(TAG, "存储 collectinfodata 成功");
////            Toast.makeText(MainActivity.this, "存储 collectinfodata 成功", Toast.LENGTH_SHORT).show();
//        }

        //导出SensorData
        List<SensorData> allSensorDataList = SensorData.getAllSensorDataList();
//        if(SensorData.Export(_ExportDir, allSensorDataList)){
////            Log.i(TAG, "存储 sensordata 成功");
////            Toast.makeText(MainActivity.this, "存储 sensordata 成功", Toast.LENGTH_SHORT).show();
//        }

        //导出训练集 4*3*n
        File _ExportTrainDir = new File(Environment.getExternalStorageDirectory()+ "/Sensor3/Export/train/");
        if (!_ExportTrainDir.exists()) {
            _ExportTrainDir.mkdirs();
            Log.i(TAG,"创建 " + Environment.getExternalStorageDirectory() +"/Sensor3/Export/train/ 成功");
        }
        try {
            int i = 0;  //filename索引
            for (String filename : filenameList) {
                File exportFile = new File(_ExportTrainDir, filename);
                if (!exportFile.exists()) {
                    exportFile.createNewFile();
                    Log.i(TAG, "创建 " + exportFile.getAbsolutePath() + " 成功");
                }

                BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
                StringBuffer sb =new StringBuffer();
                for (CollectInfoData infoData : allInfoDataList) {
                    String uid = infoData.getUid();
                    sb.append(uid);
                    List<SensorData> sensorDataList = SensorData.getSensorDataByInfo(infoData, sensortype[i/3]);
                    int j=0;
                    for(SensorData sensordata : sensorDataList){
                        j++;
                        sb.append("\t" + Integer.toString(j)+ ":" +sensordata.getXYZ(i%3));
                    }
                    sb.append("\n");
                }
                try{
                    bw.write(sb.toString());
                    bw.flush();
                    bw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
