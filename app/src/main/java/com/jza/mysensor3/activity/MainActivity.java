package com.jza.mysensor3.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jza.mysensor3.R;
import com.jza.mysensor3.data.CollectInfoData;
import com.jza.mysensor3.data.SensorData;
import com.jza.mysensor3.data.UserData;

import org.litepal.LitePal;

public class MainActivity extends AppCompatActivity {

    private ImageButton userview, collection, dbview, identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariables();
        initButton();
        initDataBase();

    }

    private void initVariables() {
        userview = (ImageButton)findViewById(R.id.userViewImgBtn);
        collection = (ImageButton)findViewById(R.id.collectImgBtn);
        dbview = (ImageButton)findViewById(R.id.DBViewImgBtn);
        identifier = (ImageButton)findViewById(R.id.identifierImgBtn);
    }

    private void initButton(){
        userview.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        collection.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SensorCollectActivity.class);
                startActivity(intent);
            }
        });
        dbview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DBViewActivity.class);
                startActivity(intent);
            }
        });
        identifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IdentifierActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDataBase() {
        LitePal.getDatabase();
        if(UserData.getAllUserList().isEmpty()){
            UserData unknownuser = new UserData();
            unknownuser.setUid("000000");
            unknownuser.setName("Unknown");
            unknownuser.save();
        }
    }

}
