package com.pockeyt.cloverpay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.utils.TokenHandler;


public class MainEmptyActivity extends AppCompatActivity {
    private static final String TAG = MainEmptyActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenHandler tokenHandler = new TokenHandler();
        TokenModel token = tokenHandler.getToken();
        Intent intent;

        if (token.getValue().equals(this.getString(R.string.no_token_in_storage_value)) || token.getExpiry() < (System.currentTimeMillis() / 1000L)) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra(getString(R.string.get_business_data_enter_main), true);
        }
        startActivity(intent);
        finish();
    }
}


