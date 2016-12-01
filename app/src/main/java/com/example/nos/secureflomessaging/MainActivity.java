package com.example.nos.secureflomessaging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Nos on 11/26/2016.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast successfulLogin = Toast.makeText(this, "Login Sucessful", Toast.LENGTH_LONG);
        successfulLogin.show();
    }

    public void clickSignOutButton(View view){
        showLoginScreen();
    }

    public void showLoginScreen(){
        Toast successfulLogin = Toast.makeText(this, "Logout Sucessful", Toast.LENGTH_LONG);
        successfulLogin.show();
        Intent intent = new Intent(MainActivity.this, LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
