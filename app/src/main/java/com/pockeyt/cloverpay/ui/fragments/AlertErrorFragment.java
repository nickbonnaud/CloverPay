package com.pockeyt.cloverpay.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.pockeyt.cloverpay.R;

public class AlertErrorFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.error_retrieve_customers_title)
                .setMessage(R.string.error_retrieve_customers_message)
                .setPositiveButton("OK", null);

        return builder.create();
    }
}
