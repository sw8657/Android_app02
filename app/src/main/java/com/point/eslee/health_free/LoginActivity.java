package com.point.eslee.health_free;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.point.eslee.health_free.database.ServerAccess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "admin@test.com:a1234:1", "foo@test.com:hello:2", "bar@test.com:world:3"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox mRememberLoginCheck;
    private SharedPreferences mPref;

    private Button mCFBLogin;
    private LoginButton mFBLogin;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        callbackManager = CallbackManager.Factory.create();  //로그인 응답을 처리할 콜백 관리자
        // 페이스북 퍼미션추가
        mCFBLogin = (Button) findViewById(R.id.facebook_log_in_button);
        mCFBLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("토큰", loginResult.getAccessToken().getToken());
                        Log.e("유저아이디", loginResult.getAccessToken().getUserId());
                        Log.e("퍼미션 리스트", loginResult.getAccessToken().getPermissions() + "");

                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                String fb_id = "";
                                String email = "";
                                try {
                                    fb_id = object.isNull("id") ? "" : object.getString("id");
                                    email = object.isNull("email") ? fb_id + "@test.com" : object.getString("email");
                                } catch (Exception ex) {

                                }

                                mAuthTask = new UserLoginTask(email, "f1234");
                                mAuthTask.execute((JSONObject) object);

                            }
                        });

                        Bundle param = new Bundle();
                        param.putString("fields", "id,name,email,gender,birthday,friends");
                        request.setParameters(param);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }
                });
            }
        });

        Button mEmailLognInButton = (Button) findViewById(R.id.email_log_in_button);
        mEmailLognInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mSignUpButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mRememberLoginCheck = (CheckBox) findViewById(R.id.remember_login_check);

        // 로그인 인증정보 초기화
        LoginInfoClear(this);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        mRememberLoginCheck.setChecked(mPref.getBoolean("saveLoginChecked", false));

        // 저장된 ID, PW 가져오기
        if (mRememberLoginCheck.isChecked()) {
            mEmailView.setText(mPref.getString("savedEmailText", ""));
            mPasswordView.setText(mPref.getString("savedPwText", ""));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // 로그인 인증정보 초기화
    public static void LoginInfoClear(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putBoolean("USER_LOGIN", false); // 로그인상태
            editor.apply();
        } catch (Exception ex) {
            Log.e("LoginClear:", ex.getMessage());
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((JSONObject) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public void onClickedChk(View view) {
        SharedPreferences.Editor pEdit = mPref.edit();
        switch (view.getId()) {
            case R.id.remember_login_check:
                pEdit.putBoolean("saveLoginChecked", mRememberLoginCheck.isChecked());
                pEdit.apply();
                break;
        }
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<JSONObject, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String jsonResult = "";
        private String mUserName = "";
        private int mUserId = -1;
        private String mFacebookId = "";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            boolean sResult = false;

            if (params != null && params.length > 0) {
                // facebook login
                JSONObject object = params[0];
                try {
                    Log.e("user profile", object.toString());
                    String fb_id = "";
                    String email = "";
                    String user_name = "";
                    String sex = "";
                    String birthday = "";
                    String friends = "";

                    fb_id = object.getString("id");
                    email = object.isNull("email") ? fb_id + "@test.com" : object.getString("email");
                    user_name = object.isNull("name") ? "username" : object.getString("name");
                    sex = object.isNull("gender") ? "male" : object.getString("gender");
                    birthday = object.isNull("birthday") ? "01/01/2017" : object.getString("birthday");
                    ArrayList<String> friendsList = new ArrayList<String>();
                    if (object.isNull("friends") == false) {
                        JSONArray pFriends = object.getJSONObject("friends").getJSONArray("data");
                        for (int i = 0; i < pFriends.length(); i++) {
                            friendsList.add(pFriends.getJSONObject(i).getString("id"));
                        }
                    }
                    friends = TextUtils.join(",", friendsList);

                    mFacebookId = fb_id;

                    // 페이스북 사용자 로그인 (서버 인증) http://dream.miraens.com:58080/faceRegistProc.do
                    String url = "http://dream.miraens.com:58080/faceRegistProc.do?" +
                            "USER_NAME=" + URLEncoder.encode(user_name) + "&sex=" + sex + "&FB_KEY=" + fb_id + "&friends=" + friends + "&EMAIL=" + email + "&birthday=" + birthday;
                    jsonResult = ServerAccess.getData(url);
                    // 인증 및 사용자 정보 읽기
                    sResult = VailidateLogin(jsonResult);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                // email login
                try {
                    // Simulate network access.
                    String url = "http://dream.miraens.com:58080/homeMain01.do?" +
//                String url = "http://192.168.1.160:8087/homeMain01.do?" +
                            "user_id=" + mEmail + "&user_pw=" + mPassword;

                    // 사용자 인증 (서버 인증)
                    jsonResult = ServerAccess.getData(url);
                    // 인증 및 사용자 정보 읽기
                    sResult = VailidateLogin(jsonResult);

//                // 디버그 테스트 모드 (샘플 사용자ID 사용)
//                sResult = true;
//                mUserId = 1;
//                mUserName = "HelloAndroid";

                } catch (Exception e) {
                    Log.e("LoginTask:", e.getMessage());
                }
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    mUserId = Integer.valueOf(pieces[2]);
                    mUserName = pieces[0].split("@")[0];
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return sResult;
        }

        // 인증 확인
        private boolean VailidateLogin(String jsonString) {
            boolean result = false;
            try {
                // 안드로이드 JSON 파싱 로직
                JSONObject json = new JSONObject(jsonString);
                JSONArray jArr = json.getJSONArray("result");

                result = jArr.getJSONObject(0).isNull("successYn") ? false : jArr.getJSONObject(0).getString("successYn").equals("SUCCESS");
                mUserName = jArr.getJSONObject(0).isNull("userNm") ? "" : jArr.getJSONObject(0).getString("userNm");
                try {
                    mUserId = Integer.valueOf(jArr.getJSONObject(0).getString("user_id"));
                } catch (Exception ex) {

                }
            } catch (Exception ex) {
                Log.e("VailidateLogin:", ex.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                // 메인액티비티에 사용자정보 전달
                Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
                loginIntent.putExtra("email", mEmail);
                loginIntent.putExtra("user_name", mUserName);
                loginIntent.putExtra("user_id", mUserId);
                loginIntent.putExtra("fb_id", mFacebookId);

                // 로그인확인 FLAG 저장
                mPref.edit().putBoolean("LOGIN_FIRST", true).apply();
                mPref.edit().putInt("user_id", mUserId).apply();
                mPref.edit().putString("user_email", mEmail).apply();
                mPref.edit().putString("user_name", mUserName).apply();
                mPref.edit().putString("fb_id", mFacebookId).apply();

                // ID, PW 저장
                if (mRememberLoginCheck.isChecked()) {
                    SharedPreferences.Editor pEdit = mPref.edit();
                    pEdit.putString("savedEmailText", mEmail);
                    pEdit.putString("savedPwText", mPassword);
                    pEdit.apply();
                }
                setResult(RESULT_OK, loginIntent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}

