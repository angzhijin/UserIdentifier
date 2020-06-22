package com.jza.mysensor3.data;

import android.os.Environment;
import android.util.Log;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class UserData extends LitePalSupport {
    private String uid;
    private String name;

    public String getUid(){
        return uid;
    }
    public void setUid(String uid){
        this.uid = uid;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public static String getUsernameByUid(String uid){
        String name = "";
        List<UserData> userList = LitePal.where("uid = ? ", uid).find(UserData.class);
        for(UserData user: userList){
            name = user.getName();
        }
        return name;
    }

    public static List<UserData> getAllUserList() {
        return LitePal.findAll(UserData.class);
    }

    public static Boolean Export(File _ExportDir, List<UserData> userDataList) {
        //导出AccData
        try {
            String filename = "UserData.txt";
            File exportFile = new File(_ExportDir, filename);
            if (!exportFile.exists()) {
                exportFile.createNewFile();
                Log.i("SensorData","创建" + Environment.getExternalStorageDirectory() + filename + " 成功");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
            StringBuffer sb =new StringBuffer();
//            List<AccData> accDataList = LitePal.findAll(AccData.class);
            for(UserData userData: userDataList){
                Log.v("SensorData","SensorData \t" + userData.getUid() + "\t" + userData.getName());
                sb.append(userData.getUid() + " ");
                sb.append(userData.getName() + "\n");
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
}
