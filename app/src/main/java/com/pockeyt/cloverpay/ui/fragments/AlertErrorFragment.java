package com.pockeyt.cloverpay.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.pockeyt.cloverpay.R;

public class AlertErrorFragment extends DialogFragment {

    public AlertErrorFragment(){}

    public static AlertErrorFragment newInstance() {
        AlertErrorFragment fragment = new AlertErrorFragment();
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String title = getArguments().getString("title");
        String message = getArguments().getString("message");

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Charge Anyway", this::chargeCustomer)
                .setNegativeButton("Cancel", this::cancel);

        return builder.create();
    }

    private void cancel(DialogInterface dialogInterface, int i) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("charge_customer", false);
        Intent intent = new Intent().putExtras(bundle);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }

    private void chargeCustomer(DialogInterface dialogInterface, int i) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("charge_customer", true);
        Intent intent = new Intent().putExtras(bundle);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
