package com.pockeyt.cloverpay.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.ui.viewModels.BusinessViewModel;
import com.pockeyt.cloverpay.ui.viewModels.LoginViewModel;
import com.pockeyt.cloverpay.ui.viewModels.TokenViewModel;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class LoginDialogFragment extends DialogFragment {
    private static final String TAG = LoginDialogFragment.class.getSimpleName();
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private View mProgressView;
    private View mLoginFormView;
    private UserLoginTask mUserLoginTask = null;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    public LoginDialogFragment() {

    }

    public static LoginDialogFragment newInstance() {
        LoginDialogFragment fragment = new LoginDialogFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        getDialog().setTitle("Please Login");
        return inflater.inflate(R.layout.activity_login, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setup Login Form
        mEmailEditText = view.findViewById(R.id.email);
        mPasswordEditText = view.findViewById(R.id.password);

        LoginViewModel loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        mEmailEditText.setText(loginViewModel.getEmail().getValue());
        mPasswordEditText.setText(loginViewModel.getPassword().getValue());


        mPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        Button signInButton = view.findViewById(R.id.email_sign_in_button);
        signInButton.setOnClickListener(v -> attemptLogin());

        mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mUserLoginTask != null) {
            return;
        }

        // Reset Errors
        mEmailEditText.setError(null);
        mPasswordEditText.setError(null);

        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password_length));
            focusView = mPasswordEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError(getString(R.string.error_field_required));
            focusView = mEmailEditText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
            focusView = mEmailEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mUserLoginTask = new UserLoginTask(email, password);
            mUserLoginTask.execute();
        }

    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void showProgress(boolean show) {
        int shortAnimateTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimateTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimateTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }




    // Login Class
    public class UserLoginTask {
        private final String TAG = UserLoginTask.class.getSimpleName();
        private String mEmail;
        private String mPassword;


        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        public void execute() {
            APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
            Observable<Response<Business>> businessObservable = apiInterface.doLogin(mEmail, mPassword);
            Disposable disposable = businessObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResult, this::handleError);
            mCompositeDisposable.add(disposable);
        }

        private void handleResult(Response<Business> businessResponse) {
            if (businessResponse.isSuccessful()) {
                Business business = businessResponse.body();
                setBusiness(business);
                saveLoginData(null, null);
                dismiss();
            } else {
                showLoginError(businessResponse);
            }
            httpCallFinished();
        }


        private void showLoginError(Response<Business> businessResponse) {
            try {
                JSONObject errorObject = new JSONObject(businessResponse.errorBody().string());
                if (errorObject.getString("error").equals("invalid_email")) {
                    mEmailEditText.setError(getString(R.string.email_not_found));
                    mEmailEditText.requestFocus();
                } else if (errorObject.getString("error").equals("invalid_password")) {
                    mPasswordEditText.setError(getString(R.string.error_invalid_password));
                    mPasswordEditText.requestFocus();
                } else {
                    Toast.makeText(getContext(), R.string.try_again_error_message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private void setBusiness(Business business) {
            Log.d(TAG, "INSIDE set business");
            TokenModel token = setAndSaveToken(business);
            BusinessModel businessModel = new BusinessModel(
                    business.getData().getId(),
                    business.getData().getSlug(),
                    business.getData().getBusinessName(),
                    token,
                    business.getData().getLogo(),
                    business.getData().getConnectedPos()
            );
            BusinessViewModel businessViewModel = ViewModelProviders.of(getActivity()).get(BusinessViewModel.class);
            businessViewModel.setBusiness(businessModel);
        }

        private TokenModel setAndSaveToken(Business business) {
            Log.d(TAG, "INSIDE set and save token");
            TokenModel token = new TokenModel(business.getData().getToken().getValue(), business.getData().getToken().getExpiry());
            TokenViewModel tokenViewModel = ViewModelProviders.of(getActivity()).get(TokenViewModel.class);
            tokenViewModel.setToken(token, true);
            return token;
        }

        private void handleError(Throwable throwable) {
            httpCallFinished();
            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
        }

        private  void httpCallFinished() {
            mUserLoginTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCompositeDisposable.dispose();
        Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveLoginData(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
    }

    private void saveLoginData(String email, String password) {
        LoginViewModel loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        loginViewModel.setEmail(email);
        loginViewModel.setPassword(password);
    }
}
