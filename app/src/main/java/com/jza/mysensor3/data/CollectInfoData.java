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

public class CollectInfoData extends LitePalSupport {
    private long starttime;
    private long endtime;
    private long timespan;
    private int sensordelay;
    private String uid;

    public long getStarttime(){
        return starttime;
    }
    public void setStarttime(long starttime){
        this.starttime = starttime;
    }
    public long getEndtime(){
        return endtime;
    }
    public void setEndtime(long endtime){
        this.endtime = endtime;
    }
    public long getTimespan(){
        return timespan;
    }
    public void setTimespan(long timespan){
        this.timespan = timespan;
    }
    public int getSensorDelay(){
        return sensordelay;
    }
    public void setSensorDelay(int sensordelay){
        this.sensordelay = sensordelay;
    }
    public String getUid(){
        return uid;
    }
    public void setUid(String uid){
        this.uid = uid;
    }

    public static List<CollectInfoData> getAllInfoList() {
        return LitePal.findAll(CollectInfoData.class);
    }

    public static List<CollectInfoData> getInfoByUiddelay(String uid, int delay){
        return LitePal.where("uid = ? and sensordelay = ?", uid, delay+"").find(CollectInfoData.class);
    }

    public static Boolean Export(File _ExportDir, List<CollectInfoData> collectInfoDataList) {
        //导出AccData
        try {
            String filename = "CollectInfoData.txt";
            File exportFile = new File(_ExportDir, filename);
            if (!exportFile.exists()) {
                exportFile.createNewFile();
                Log.i("SensorData","创建" + Environment.getExternalStorageDirectory() + filename + " 成功");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
            StringBuffer sb =new StringBuffer();
//            List<AccData> accDataList = LitePal.findAll(AccData.class);
            for(CollectInfoData collectInfoData : collectInfoDataList){
                Log.v("SensorData","CollectInfoData \t" + collectInfoData.getStarttime() + "\t" +
                        collectInfoData.getEndtime() + "\t" +
                        collectInfoData.getTimespan() + "\t" +
                        collectInfoData.getSensorDelay() + "\t" +
                        collectInfoData.getUid());
                sb.append(collectInfoData.getStarttime() + " ");
                sb.append(collectInfoData.getEndtime() + " ");
                sb.append(collectInfoData.getTimespan() + " ");
                sb.append(collectInfoData.getSensorDelay() + " ");
                sb.append(collectInfoData.getUid() + "\n");
            }
            try{
                bw.write(sb.toString());
                bw.flush();
                bw.close();
                Log.i("Sensor", "导出 " + filename + " 成功");
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



    /**
     * 将毫秒转化成时间
     * 时间格式：yyyy-MM-dd HH:mm:ss
     * @param millisecond
     * @return
     */
    public static String getDateTimeFromMillisecond(Long millisecond){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 通过毫秒获取时间间隔
     * @param StartTime
     * @param EndTime
     * @return
     */
    public static Long getTimeSpan(Long StartTime, Long EndTime) {
        Long TimeSpan = (EndTime - StartTime) ;
//        Long TimeSpan = (EndTime - StartTime) / 1000;   //转换为秒
        return TimeSpan;
    }
}
