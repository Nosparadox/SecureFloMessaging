package com.example.nos.secureflomessaging;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nos.secureflomessaging.data.User;
import com.example.nos.secureflomessaging.webservices.WebServiceTask;
import com.example.nos.secureflomessaging.webservices.WebServiceUtils;

import org.json.JSONObject;

/**
 * Created by Nos on 11/26/2016.
 */

public class LoginRegisterActivity extends AppCompatActivity {
    private UserLoginRegisterTask mUserLoginRegisterTask = null;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private TextView mResetLink;
    int failed = 0;
    private Boolean exit = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        initViews();
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
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

    private void initViews() {
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mResetLink = (TextView) findViewById(R.id.reset_link);

        mResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetLoginPrompt();
            }
        });
    }

    public void resetLoginPrompt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginRegisterActivity.this);

        builder.setTitle("Forgot Username/Password?");
        final SpannableString s =
                new SpannableString("wingate670@gmail.com");
        Linkify.addLinks(s, Linkify.WEB_URLS);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setMessage("Please contact the system admin at " + s);
        builder.setCancelable(true);
        builder.setPositiveButton("Dismiss", null);
        builder.show();

    }

    public void attemptLoginRegister(View view) {
        if(mUserLoginRegisterTask != null) {
            return;
        }

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_password_length));
            focusView = mPasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if(!isEmailValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
        } else {
            mUserLoginRegisterTask = new UserLoginRegisterTask(username, password, view.getId() == R.id.email_sign_in_button);
            mUserLoginRegisterTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return email.length() > 4;
        //return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgress(final boolean isShow) {
        findViewById(R.id.login_progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.login_form).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private class UserLoginRegisterTask extends WebServiceTask {
        private final ContentValues contentValues = new ContentValues();
        private boolean mIsLogin;

        UserLoginRegisterTask(String username, String password, boolean isLogin) {
            super(LoginRegisterActivity.this);
            contentValues.put(Constants.USERNAME, username);
            contentValues.put(Constants.PASSWORD, password);
            contentValues.put(Constants.GRANT_TYPE, Constants.CLIENT_CREDENTIALS);
            mIsLogin = isLogin;
        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            JSONObject obj = WebServiceUtils.requestJSONObject(mIsLogin ? Constants.LOGIN_URL : Constants.SIGNUP_URL,
                    WebServiceUtils.METHOD.POST, contentValues, true);
            mUserLoginRegisterTask = null;
            if(!hasError(obj)) {
                if(mIsLogin) {
                    User user = new User();
                    user.setId(obj.optLong(Constants.ID));
                    user.setUsername(contentValues.getAsString(Constants.USERNAME));
                    user.setPassword(contentValues.getAsString(Constants.PASSWORD));
                    RESTServiceApplication.getInstance().setUser(user);
                    RESTServiceApplication.getInstance().setAccessToken(
                            obj.optJSONObject(Constants.ACCESS).optString(Constants.ACCESS_TOKEN));

                    return true;
                } else {
                    mIsLogin = true;
                    performRequest();
                    return true;
                }
            }
            failed++;
            System.out.println("Failed Attempts: " + failed);


            if(failed > 2){
                System.out.println("Login attempts exceeded. Terminating Application");
                System.exit(0);

            }
            return false;
        }


        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }
    }
}
