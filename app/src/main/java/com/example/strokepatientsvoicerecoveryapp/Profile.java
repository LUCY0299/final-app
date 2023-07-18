package com.example.strokepatientsvoicerecoveryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class Profile extends AppCompatActivity  {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Button edit_password = (Button) findViewById(R.id.edit_password);
        edit_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(Profile.this,Edit_password.class);
                startActivity(intent);
            }
        });
        Button edit_infor = (Button) findViewById(R.id.edit_infor);
        edit_infor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(Profile.this,Modify.class);
                startActivity(intent);
            }
        });
    }

}