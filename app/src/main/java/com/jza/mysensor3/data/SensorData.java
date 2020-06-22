package com.jza.mysensor3.data;

import android.os.Environment;
import android.util.Log;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SensorData extends LitePalSupport {
    private int sensortype;
    private long timestamp;
    private double x;
    private double y;
    private double z;

    public int getSensorType(){
        return sensortype;
    }
    public void setSensorType(int sensortype){
        this.sensortype = sensortype;
    }
    public long getTimeStamp(){
        return timestamp;
    }
    public void setTimeStamp(long timestamp){
        this.timestamp = timestamp;
    }
    public double getX(){
        return x;
    }
    public void setX(double x){
        this.x = x;
    }
    public double getY(){
        return y;
    }
    public void setY(double y){
        this.y = y;
    }
    public double getZ(){
        return z;
    }
    public void setZ(double z){
        this.z = z;
    }

    public double getXYZ( int dimension){
        if(dimension == 1){
            return y;
        }
        else if(dimension == 2){
            return z;
        }
        else {
            return x;
        }
    }

    public static List<SensorData> getAllSensorDataList() {
        return LitePal.findAll(SensorData.class);
    }

    public static List<SensorData> getSensorDataByInfo(CollectInfoData infodata, int sensortype){
        return LitePal.where("sensortype = ? and timestamp > ? and timestamp < ?",
                sensortype+"", Long.toString(infodata.getStarttime()), Long.toString(infodata.getEndtime()))
                .order("timestamp asc").find(SensorData.class);
    }

    public static Boolean Export(File _ExportDir, List<SensorData> sensorDataList){
        //导出AccData
        try {
            String filename = "SensorData.txt";
            File exportFile = new File(_ExportDir, filename);
            if (!exportFile.exists()) {
                exportFile.createNewFile();
                Log.i("Sensor","创建" + Environment.getExternalStorageDirectory() + filename + " 成功");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
            StringBuffer sb =new StringBuffer();
//            List<AccData> accDataList = LitePal.findAll(AccData.class);
            for(SensorData sensorData: sensorDataList){
                Log.v("SensorData","SensorData \t" + sensorData.getTimeStamp() + "\t" + sensorData.getSensorType() + "\t" +
                        sensorData.getX() + "\t" + sensorData.getY() + "\t" + sensorData.getZ());
                sb.append(sensorData.getTimeStamp() + " ");
                sb.append(sensorData.getSensorType() + " ");
                sb.append(sensorData.getX() + " ");
                sb.append(sensorData.getY() + " ");
                sb.append(sensorData.getZ() + "\n");
            }
            try{
                bw.write(sb.toString());
                bw.flush();
                bw.close();
                Log.i("SensorData", "导出 " + filename + " 成功");
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



}
