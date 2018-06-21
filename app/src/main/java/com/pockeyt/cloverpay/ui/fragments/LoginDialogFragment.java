package com.pockeyt.cloverpay.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.pockeyt.cloverpay.R;

public class LoginDialogFragment extends DialogFragment {
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private View mProgressView;
    private View mLoginFormView;

    public LoginDialogFragment() {

    }

    public static LoginDialogFragment newInstance() {
        LoginDialogFragment fragment = new LoginDialogFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setup Login Form
        mEmailEditText = getActivity().findViewById(R.id.email);
        mPasswordEditText = getActivity().findViewById(R.id.password);

        mPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        Button signInButton = getActivity().findViewById(R.id.email_sign_in_button);
        signInButton.setOnClickListener(v -> attemptLogin());

        mLoginFormView = getActivity().findViewById(R.id.login_form);
        mProgressView = getActivity().findViewById(R.id.login_progress);
    }

    private void attemptLogin() {

    }
}
