package com.example.nos.secureflomessaging;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import com.example.nos.secureflomessaging.data.User;
import com.example.nos.secureflomessaging.webservices.WebServiceTask;
import com.example.nos.secureflomessaging.webservices.WebServiceUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Nos on 11/26/2016.
 */


public class MainActivity extends AppCompatActivity {
    private UserInfoTask mUserInfoTask = null;
    private UserEditTask mUserEditTask = null;
    private UserEditTask2 mreset = null;
    private TextView display;
    private EditText mNoteText;
    private EditText mSendTo;
    Handler mHandler;
    private boolean exit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Message Timed Out", Toast.LENGTH_SHORT).show();
                mreset = new UserEditTask2();
                mreset.execute();
                refreshmsg();
            }
        },60000);
        initViews();
        showProgress(true);
        mUserInfoTask = new UserInfoTask();
        mUserInfoTask.execute();

    }

    @Override
    public void onBackPressed() {
        if (exit) {
            System.exit(0);
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Pressing Back again will logout",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
    /*private final Runnable m_Runnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "Timed Out", Toast.LENGTH_SHORT).show();
            mreset = new UserEditTask2();
            mreset.execute();
            MainActivity.this.mHandler.postDelayed(m_Runnable, 60000);
        }
    }, 5000);*/
    private void initViews() {
        display = (TextView) findViewById(R.id.note);
        mNoteText = (EditText) findViewById(R.id.input);
        mSendTo = (EditText) findViewById(R.id.to);
    }

    private void showProgress(final boolean isShow) {
        //findViewById(R.id.input).setVisibility(isShow  ? View.GONE : View.VISIBLE);
        findViewById(R.id.note).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private void populateText() {
        User user = RESTServiceApplication.getInstance().getUser();
        display.setText(user.getNote() == null ? "" : user.getNote());
    }


    public void clickSignOutButton(View view) {
        mreset = new UserEditTask2();
        mreset.execute();
        finish();
        System.exit(0);
        showLoginScreen();

    }


    public void clickComposeButton(View view) {
        mUserEditTask = new UserEditTask();
        mUserEditTask.execute();
        Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
        refreshmsg();
    }
    public void refresh(View view) {
        refreshmsg();
        //showLoginScreen();
    }
    public void refreshmsg() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private class UserEditTask2 extends ActivityWebServiceTask {
        public UserEditTask2() {
            super(mreset);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.NOTE, "");
            contentValues.put(Constants.ID, user.getId());
            //contentValues.put(Constants.USERNAME, mSendTo.getText().toString());

            ContentValues urlValues = new ContentValues();
            urlValues.put(Constants.ACCESS_TOKEN, RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.UPDATE_URL,
                    WebServiceUtils.METHOD.POST, urlValues, contentValues);
            if(!hasError(obj)) {
                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setNote(jsonObject.optString(Constants.NOTE));
                return true;
            }

            return false;
        }
    }





    private void showLoginScreen() {
        Intent intent = new Intent(MainActivity.this, LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private abstract class ActivityWebServiceTask extends WebServiceTask {
        public ActivityWebServiceTask(WebServiceTask webServiceTask)     {
            super(MainActivity.this);
        }

        @Override
        public void showProgress() {
            MainActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            MainActivity.this.showProgress(false);
        }

        @Override
        public void performSuccessfulOperation() {
            populateText();
        }
    }

    private class UserInfoTask extends ActivityWebServiceTask {
        public UserInfoTask() {
            super(mUserInfoTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.INFO_URL,
                    WebServiceUtils.METHOD.GET, contentValues, null);
            if(!hasError(obj)) {
                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);

                user.setNote(jsonObject.optString(Constants.NOTE));
                if(user.getNote().equalsIgnoreCase("null")) {
                    user.setNote(null);
                }

                user.setId(jsonObject.optLong(Constants.ID_INFO));
                return true;
            }
            return false;
        }
    }

    private class UserEditTask extends ActivityWebServiceTask {
        public UserEditTask() {
            super(mUserEditTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.NOTE, mNoteText.getText().toString());
            contentValues.put(Constants.ID, mSendTo.getText().toString());
            //contentValues.put(Constants.USERNAME, mSendTo.getText().toString());

            ContentValues urlValues = new ContentValues();
            urlValues.put(Constants.ACCESS_TOKEN, RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.UPDATE_URL,
                    WebServiceUtils.METHOD.POST, urlValues, contentValues);
            if(!hasError(obj)) {
                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setNote(jsonObject.optString(Constants.NOTE));
                return true;
            }

            return false;
        }
    }


}

/*public class MainActivity extends AppCompatActivity {
    //private final ContentValues contentValues = new ContentValues();
    private SomethingCool mSomethingCool = null;
    private TextView mNoteText;
    private EditText input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        populateText();
        Toast successfulLogin = Toast.makeText(this, "Login Sucessful", Toast.LENGTH_LONG);
        successfulLogin.show();
    }

    private void initViews() {
        mNoteText = (TextView) findViewById(R.id.note);
        input = (EditText) findViewById(R.id.input);
    }

    public void clickSignOutButton(View view) {
        showLoginScreen();
    }

    public void clickComposeButton(View view) {
        writeMessage();
        //showLoginScreen();
    }

    public void refresh(View view) {
        refreshmsg();
        //showLoginScreen();
    }

    private void populateText() {
        ContentValues contentValues = new ContentValues();
        User user = RESTServiceApplication.getInstance().getUser();
        JSONObject obj = WebServiceUtils.requestJSONObject(Constants.INFO_URL,
                WebServiceUtils.METHOD.GET, contentValues, null);
        JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
        JSONObject jsonObject = jsonArray.optJSONObject(0);
        user.setNote(jsonObject.optString(Constants.NOTE));
        mNoteText.setText(user.getNote() == null ? "NO MESSAGES" : user.getNote());
    }


    public void showLoginScreen() {
        Toast successfulLogin = Toast.makeText(this, "Logout Sucessful", Toast.LENGTH_LONG);
        successfulLogin.show();
        Intent intent = new Intent(MainActivity.this, LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void writeMessage() {
        User user = RESTServiceApplication.getInstance().getUser();
        String msg = input.getText().toString();
        user.setNote(msg);
        mSomethingCool = new SomethingCool(msg, true);
        mSomethingCool.execute((Void) null);


        ContentValues contentValues = new ContentValues();
        User user = RESTServiceApplication.getInstance().getUser();
        contentValues.put(Constants.NOTE, mNoteText.getText().toString());
        ContentValues urlValues = new ContentValues();
        urlValues.put(Constants.ACCESS_TOKEN, RESTServiceApplication.getInstance().getAccessToken());
        JSONObject obj = WebServiceUtils.requestJSONObject(Constants.UPDATE_URL,
                WebServiceUtils.METHOD.POST, urlValues, contentValues);
        JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
        JSONObject jsonObject = jsonArray.optJSONObject(0);
        user.setNote(jsonObject.optString(Constants.NOTE));
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);


    }

    public void refreshmsg() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void showProgress(final boolean isShow) {
        findViewById(R.id.login_progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.login_form).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private class SomethingCool extends WebServiceTask {
        private final ContentValues contentValues = new ContentValues();
        private boolean mIsLogin;

        SomethingCool(String note, boolean isLogin) {
            super(MainActivity.this);
            mIsLogin = isLogin;
            contentValues.put(Constants.NOTE, note);
        }

        @Override
        public void showProgress() {
            MainActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            MainActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            JSONObject obj = WebServiceUtils.requestJSONObject(mIsLogin ? Constants.LOGIN_URL : Constants.SIGNUP_URL,
                    WebServiceUtils.METHOD.POST, contentValues, true);
            mSomethingCool = null;
            if(!hasError(obj)) {
                if(mIsLogin) {
                    User user = RESTServiceApplication.getInstance().getUser();
                    user.setNote(contentValues.getAsString(Constants.NOTE));
                    return true;
                } else {
                    mIsLogin = true;
                    performRequest();
                    return true;
                }
            }
            return false;
        }


        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
    }


}*/