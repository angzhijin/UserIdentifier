package com.jza.mysensor3.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jza.mysensor3.R;
import com.jza.mysensor3.data.CollectInfoData;
import com.jza.mysensor3.data.UserData;

import org.litepal.LitePal;

import java.util.List;

public class UserActivity extends AppCompatActivity {

    private EditText uidInput, nameInput;
    private Button register;
    private TextView userView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initVariables();
        initButton();
    }

    public void initVariables(){
        uidInput = (EditText)findViewById(R.id.uidInput);
        nameInput = (EditText)findViewById(R.id.nameInput);
        register = (Button)findViewById(R.id.registerBtn);
        userView = (TextView)findViewById(R.id.userTextView);

        updataView();
    }

    public void updataView(){
        userView.setText("");
        List<UserData> userDataList = UserData.getAllUserList();
        for(UserData userData : userDataList){
            userView.append(userData.getUid() + "\t" + userData.getName() + "\n");
        }
    }

    public void initButton(){
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = uidInput.getText().toString();
                String name = nameInput.getText().toString();
                if((LitePal.where("uid = ? ", uid).find(UserData.class)).isEmpty()){
                    UserData user = new UserData();
                    user.setUid(uid);
                    user.setName(name);
                    user.save();
                    updataView();
                }
                else{
                    Toast.makeText(UserActivity.this, "该用户已存在", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
