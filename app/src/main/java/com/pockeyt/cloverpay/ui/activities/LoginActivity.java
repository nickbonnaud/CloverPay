package com.pockeyt.cloverpay.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.handlers.BusinessHandler;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.utils.TokenHandler;

import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private UserLoginTask mAuthTask = null;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup Login form
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((v, id, event) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        Button emailSignInButton = findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(v -> attemptLogin());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset Errors
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at time of login attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered on
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password_length));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address
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
            // There was an error; focus on first form field with error
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, this);
            mAuthTask.execute();
        }
    }


    private void showProgress(final boolean show) {
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
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }


    public class UserLoginTask {
        private String mEmail;
        private String mPassword;
        private Context mContext;



        UserLoginTask(String email, String password, Context context) {
            mEmail = email;
            mPassword = password;
            mContext = context;
        }

        public void execute() {
            APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
            Observable<Response<Business>> businessObservable = apiInterface.doLogin(mEmail, mPassword);
            Disposable disposable = businessObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResult, this::handleError);

            mCompositeDisposable.add(disposable);
        }

        private void handleResult(Response<Business> businessResponse) throws IOException {
            if (businessResponse.isSuccessful()) {
                Business business =  businessResponse.body();
                setBusiness(business);
                goToMainActivity();
            } else {
                showError(businessResponse);
            }
            httpCallFinished();
        }

        private void showError(Response<Business> businessResponse) {
            try {
                JSONObject errorObject = new JSONObject(businessResponse.errorBody().string());
                Log.d(TAG, errorObject.getString("error"));
                if (errorObject.getString("error").equals("invalid_email")) {
                    mEmailView.setError(getString(R.string.email_not_found));
                    mEmailView.requestFocus();
                } else if (errorObject.getString("error").equals("invalid_password")) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    mPasswordView.requestFocus();
                } else {
                    Toast.makeText(mContext, R.string.try_again_error_message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private void goToMainActivity() {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(mContext.getString(R.string.get_business_data_enter_main), false);
            startActivity(intent);
            finish();
        }

        private void handleError(Throwable throwable) {
            Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_LONG).show();
        }


        private void setBusiness(Business business) {
            TokenModel token  = setAndSaveTokenModel(business);
            BusinessModel businessModel = new BusinessModel(
                    business.getData().getId(),
                    business.getData().getSlug(),
                    business.getData().getBusinessName(),
                    token,
                    business.getData().getLogo()
            );
            BusinessHandler.setBusiness(businessModel);
        }

        private TokenModel setAndSaveTokenModel(Business business) {
            TokenModel token = new TokenModel(business.getData().getToken().getValue(), business.getData().getToken().getExpiry());
            TokenHandler tokenHandler = new TokenHandler();
//            tokenHandler.saveToken(token);
            return token;
        }


        private void httpCallFinished() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
