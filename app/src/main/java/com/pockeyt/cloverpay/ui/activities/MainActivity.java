package com.pockeyt.cloverpay.ui.activities;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.handlers.CustomerHandler;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.CustomerPusherModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.ui.fragments.CustomerGridPagerFragment;
import com.pockeyt.cloverpay.ui.fragments.CustomerListFragment;
import com.pockeyt.cloverpay.ui.fragments.CustomerViewPagerFragment;
import com.pockeyt.cloverpay.ui.fragments.LoginDialogFragment;
import com.pockeyt.cloverpay.ui.viewModels.BusinessViewModel;
import com.pockeyt.cloverpay.ui.viewModels.CustomersViewModel;
import com.pockeyt.cloverpay.ui.viewModels.MainActivityViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;
import com.pockeyt.cloverpay.ui.viewModels.TokenViewModel;
import com.pockeyt.cloverpay.utils.CloverTenderConnecter;
import com.pockeyt.cloverpay.utils.Interfaces;
import com.pockeyt.cloverpay.utils.PusherConnector;
import com.pusher.client.channel.PrivateChannelEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements Interfaces.OnListCustomerSelectedInterface, DialogInterface.OnDismissListener {
    public static final String CUSTOMER_LIST_FRAGMENT = "customer_list_fragment";
    public static final String CUSTOMER_VIEWPAGER_FRAGMENT = "customer_viewpager_fragment";
    public static final String CUSTOMER_GRID_PAGER_FRAGMENT = "customer_grid_pager_fragment";
    public static final String LOGIN_DIALOG_FRAGMENT = "login_dialog_fragment";
    private static final String TAG = MainActivity.class.getSimpleName();

    private PublishSubject<CustomerPusherModel> mCustomerPusherSubject;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private CloverTenderConnecter mCloverTenderConnecter;
    private boolean mIsTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsTablet = getResources().getBoolean(R.bool.is_tablet);

        checkShouldShowLogin();

        mCustomerPusherSubject = PublishSubject.create();
//        mCloverTenderConnecter = new CloverTenderConnecter(this);
//        mCloverTenderConnecter.init();
    }

    private void checkShouldShowLogin() {
        TokenViewModel tokenViewModel = ViewModelProviders.of(this).get(TokenViewModel.class);
        TokenModel token = tokenViewModel.getToken().getValue();
        if (token.getValue().equals(getString(R.string.no_token_in_storage_value)) || token.getExpiry() < (System.currentTimeMillis() / 1000L)) {
            showLoginDialog();
        } else {
            getBusiness();
        }
    }

    private void showLoginDialog() {
        LoginDialogFragment savedFragment = (LoginDialogFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_DIALOG_FRAGMENT);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedFragment == null) {
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
            loginDialogFragment.show(fragmentManager, LOGIN_DIALOG_FRAGMENT);
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        getBusiness();
    }

    private void getBusiness() {
        BusinessViewModel businessViewModel = ViewModelProviders.of(this).get(BusinessViewModel.class);
        businessViewModel.getBusiness().observe(this, business -> {
            TokenViewModel tokenViewModel = ViewModelProviders.of(this).get(TokenViewModel.class);
            tokenViewModel.setToken(business.getToken(), true);
            if (!mIsTablet) {
                setCustomerListFragment();
            } else {
                setCustomerGridPagerFragment();
            }
            connectToPusher();
        });
    }

    private void setCustomerGridPagerFragment() {
        CustomerGridPagerFragment savedFragment = (CustomerGridPagerFragment) getSupportFragmentManager().findFragmentByTag(CUSTOMER_GRID_PAGER_FRAGMENT);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (savedFragment == null) {
            CustomerGridPagerFragment customerGridPagerFragment = new CustomerGridPagerFragment();
            if (fragmentManager.findFragmentByTag(CUSTOMER_LIST_FRAGMENT) != null || fragmentManager.findFragmentByTag(CUSTOMER_VIEWPAGER_FRAGMENT) != null) {
                fragmentTransaction.replace(R.id.placeHolder, customerGridPagerFragment, CUSTOMER_GRID_PAGER_FRAGMENT);
            } else {
                fragmentTransaction.add(R.id.placeHolder, customerGridPagerFragment, CUSTOMER_GRID_PAGER_FRAGMENT);
            }

        } else {
            fragmentTransaction.replace(R.id.placeHolder, savedFragment, CUSTOMER_GRID_PAGER_FRAGMENT);
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

    private void setCustomerListFragment() {
        CustomerListFragment savedFragment = (CustomerListFragment) getSupportFragmentManager().findFragmentByTag(CUSTOMER_LIST_FRAGMENT);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment customerGridPagerFragment = fragmentManager.findFragmentByTag(CUSTOMER_GRID_PAGER_FRAGMENT);
        if (savedFragment == null) {
            CustomerListFragment customerListFragment = new CustomerListFragment();
            if (customerGridPagerFragment != null) {
                fragmentTransaction.replace(R.id.placeHolder, customerListFragment, CUSTOMER_LIST_FRAGMENT);
            } else {
                fragmentTransaction.add(R.id.placeHolder, customerListFragment, CUSTOMER_LIST_FRAGMENT);
            }

        } else if (customerGridPagerFragment != null) {
            Log.d(TAG, "CUSTOMER GRIDPAGER FRAGMENT IS NOT NULL");
            fragmentTransaction.replace(R.id.placeHolder, savedFragment, CUSTOMER_LIST_FRAGMENT);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onListCustomerSelected(CustomerModel customer) {
        SelectedCustomerViewModel selectedCustomerViewModel = ViewModelProviders.of(this).get(SelectedCustomerViewModel.class);
        selectedCustomerViewModel.setCustomer(customer);
        CustomerViewPagerFragment fragment = new CustomerViewPagerFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.placeHolder, fragment, CUSTOMER_VIEWPAGER_FRAGMENT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showError(String errorMessage) {
        String formattedMessage;
        switch (errorMessage) {
            case "token_expired":
                formattedMessage = "Your login credentials have expired. Please login.";
                break;
            case "token_invalid":
                formattedMessage = "Your login credentials have become invalid. Please login.";
                break;
            case "token_absent":
                formattedMessage = "Your login credentials have been deleted. Please login.";
                break;
            case "user_not_found":
                formattedMessage = "Oops! Something went wrong. Please re-verify your login.";
                break;
            default:
                formattedMessage = "Oops! Something went wrong. Please re-verify your login.";
        }
        Toast.makeText(this, formattedMessage, Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private PrivateChannelEventListener privateChannelEventListener = new PrivateChannelEventListener() {
        @Override
        public void onAuthenticationFailure(String s, Exception e) {
            Log.e(TAG, e.getMessage());
        }

        @Override
        public void onSubscriptionSucceeded(String s) {
            Log.d(TAG, s);
        }

        @Override
        public void onEvent(String channel, String event, String data) {
            Log.d(TAG, data);
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject dataBody = jsonObject.getJSONObject("data");
                handlePusherData(dataBody);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void handlePusherData(JSONObject dataBody) throws JSONException {
        String type = dataBody.getString("type");
        CustomerModel customer = CustomerHandler.setCustomer(dataBody.getJSONObject("data"));
        CustomerPusherModel customerPusher = new CustomerPusherModel(type, customer);
        String toastMessage;
        switch (type) {
            case "deal_redeemed":
                toastMessage = customer.getFirstName() + " has accepted your request to redeem their deal!";
                break;
            case "loyalty_redeemed":
                toastMessage = customer.getFirstName() + " has accepted your request to redeem their loyalty reward!";
                break;
            case "redeem_later_deal":
                toastMessage = customer.getFirstName() + " wishes to redeem their deal at a later time.";
                break;
            case "wrong_deal":
                toastMessage = customer.getFirstName() + " claims they did not purchase this deal.";
                break;
            case "redeem_later_reward":
                toastMessage = customer.getFirstName() + " wishes to redeem their loyalty reward at a later time.";
                break;
            case "not_earned_reward":
                toastMessage = customer.getFirstName() + " claims they have not earned this loyalty reward.";
                break;
            case "wrong_bill":
                toastMessage = customer.getFirstName() + " claims the bill they were sent was the wrong bill.";
                break;
            case "error_bill":
                toastMessage = customer.getFirstName() + " claims their is an error with their bill.";
                break;
            default:
                toastMessage = "Oops! An error occurred";
        }
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
//            setCustomersViewModel(customer);
            setCustomerPusherPubSub(customerPusher);
        });
    }

    private void setCustomersViewModel(CustomerModel customer) {
        CustomersViewModel customersViewModel = ViewModelProviders.of(this).get(CustomersViewModel.class);
        customersViewModel.getCustomers().observe(this, customers -> {
            Boolean customerFound = false;
            for (int i = 0; i < customers.length; i++) {
                if (customers[i].getId() == customer.getId()) {
                    customerFound = true;
                    customers[i] = customer;
                }
            }
            if (customerFound) {
                customersViewModel.setCustomers(customers);
            }
        });
    }

    public PublishSubject getCustomerPusherPubSub() {
        return mCustomerPusherSubject;
    }


    public void setCustomerPusherPubSub(CustomerPusherModel customer) {
        mCustomerPusherSubject.onNext(customer);
    }


    private void connectToPusher() {
        PusherConnector pusherConnector = new PusherConnector(this);
        if (!pusherConnector.getIsPusherConnected()) {
            pusherConnector.connectToPusher();
            bindToPusher();
        }
    }

    private void bindToPusher() {
        PusherConnector pusherConnector = new PusherConnector(this);
        if (pusherConnector.getIsPusherConnected()) {
            pusherConnector.getPusherChannel().bind(pusherConnector.getPusherEvent(), privateChannelEventListener);
        }
    }

    private void unbindToPusher() {
        PusherConnector pusherConnector = new PusherConnector(this);
        if (pusherConnector.getIsPusherConnected()) {
            pusherConnector.getPusherChannel().unbind(pusherConnector.getPusherEvent(), privateChannelEventListener);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindToPusher();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindToPusher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
//        mCloverTenderConnecter.destroy();
        unbindToPusher();
    }

    @Override
    public void onBackPressed() {
        CustomerViewPagerFragment fragment = (CustomerViewPagerFragment) getSupportFragmentManager().findFragmentByTag(CUSTOMER_VIEWPAGER_FRAGMENT);
        if (fragment != null && fragment.isVisible()) {
            SelectedCustomerViewModel selectedCustomerViewModel = ViewModelProviders.of(this).get(SelectedCustomerViewModel.class);
            selectedCustomerViewModel.resetSelectedCustomer();
        }
        super.onBackPressed();
    }
}
