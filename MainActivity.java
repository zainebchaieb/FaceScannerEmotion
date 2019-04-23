package com.example.hp.facescanneremotion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {
    private EditText Name ;
    private EditText Password ;
    private  Button Login ;
    private  TextView Info ;
    private int counter =5 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Name =(EditText)findViewById(R.id.edName);
        Password=(EditText)findViewById(R.id.edPassword);
        Info=(Button)findViewById(R.id.btnLogin);


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(Name.getText().toString(),Password.getText().toString());
            }
        });



    }
    private  void validate (String userName , String userPassword)
    {
        if ((userName=="Admin")&&(userPassword=="1234"))
        {
            Intent intent = new Intent (MainActivity.this,SecondActivity.class);
            startActivity(intent);
        }
        else
        {
            counter--;
            Info.setText("No of attempts remaining "+String.valueOf(counter));
            if (counter == 0)
            {
                Login.setEnabled(false);
            }
        }
    }
}
