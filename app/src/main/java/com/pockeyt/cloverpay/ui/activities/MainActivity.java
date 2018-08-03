package com.pockeyt.cloverpay.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.handlers.NotificationHandler;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.CloverTransactionModel;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.CustomerPubSubModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.ui.fragments.CustomerGridPagerFragment;
import com.pockeyt.cloverpay.ui.fragments.CustomerListFragment;
import com.pockeyt.cloverpay.ui.fragments.CustomerViewPagerFragment;
import com.pockeyt.cloverpay.ui.fragments.LoginDialogFragment;
import com.pockeyt.cloverpay.ui.viewModels.BusinessViewModel;
import com.pockeyt.cloverpay.ui.viewModels.CloverTransactionViewModel;
import com.pockeyt.cloverpay.ui.viewModels.CustomersViewModel;
import com.pockeyt.cloverpay.ui.viewModels.PockeytTransactionViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;
import com.pockeyt.cloverpay.ui.viewModels.TokenViewModel;
import com.pockeyt.cloverpay.utils.CloverTenderConnecter;
import com.pockeyt.cloverpay.utils.Interfaces;
import com.pockeyt.cloverpay.utils.PusherService;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements Interfaces.OnListCustomerSelectedInterface, DialogInterface.OnDismissListener {
    public static final String CUSTOMER_LIST_FRAGMENT = "customer_list_fragment";
    public static final String CUSTOMER_VIEWPAGER_FRAGMENT = "customer_viewpager_fragment";
    public static final String CUSTOMER_GRID_PAGER_FRAGMENT = "customer_grid_pager_fragment";
    public static final String LOGIN_DIALOG_FRAGMENT = "login_dialog_fragment";
    public static final String KEY_BUSINESS_SLUG = "key_business_slug";
    public static final String KEY_BUSINESS_TOKEN = "key_business_token";

    private static final String TAG = MainActivity.class.getSimpleName();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private CloverTenderConnecter mCloverTenderConnecter;
    private boolean mIsTablet;
    private NotificationBroadcastReceiver mNotificationBroadcastReceiver;
    private PublishSubject<CustomerPubSubModel> mCustomerPubSub;
    private PublishSubject<BusinessModel> mAuthReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createPubSubs();
        setAuthReadyPubSub();
        mCloverTenderConnecter = new CloverTenderConnecter(this);
        mCloverTenderConnecter.init();

        mIsTablet = getResources().getBoolean(R.bool.is_tablet);
        checkShouldShowLogin();

        setResult(RESULT_CANCELED);
    }

    private void createPubSubs() {
        mAuthReady = PublishSubject.create();
        mCustomerPubSub = PublishSubject.create();
    }

    private void setAuthReadyPubSub() {
        mAuthReady.subscribe(this::init);
    }

    private void init(BusinessModel business) {
        getCloverIntent();

        // Fix this so that if token does not match
        if (business.getConnectedPos() == null || !business.getConnectedPos().equals("clover")) {
            mCloverTenderConnecter.getAccountData();
        }

        if (!mIsTablet) {
            setCustomerListFragment();
        } else {
            setCustomerGridPagerFragment();
        }
        connectToPusher(business);
    }

    private void getCloverIntent() {
        Intent cloverTenderIntent = getIntent();
        if (cloverTenderIntent != null && cloverTenderIntent.getAction().equals("clover.intent.action.MERCHANT_TENDER")) {
            setSelectedCustomerFromIntent(cloverTenderIntent);
        }
    }

    public PublishSubject<CustomerPubSubModel> getCustomerPubSub() {
        return mCustomerPubSub;
    }

    private void setSelectedCustomerFromIntent(Intent cloverTenderIntent) {
        CloverTransactionModel cloverTransaction = mCloverTenderConnecter.setCloverTransaction(cloverTenderIntent);
        CloverTransactionViewModel cloverTransactionViewModel = ViewModelProviders.of(this).get(CloverTransactionViewModel.class);
        cloverTransactionViewModel.setCloverTransaction(cloverTransaction);

        CustomersViewModel customersViewModel = ViewModelProviders.of(this).get(CustomersViewModel.class);
        customersViewModel.getCustomers().observe(this, customers -> {
            PockeytTransactionViewModel pockeytTransactionViewModel = ViewModelProviders.of(this).get(PockeytTransactionViewModel.class);
            pockeytTransactionViewModel.getpockeytTransaction(cloverTransaction.getOrderId()).observe(this, pockeytTransaction -> {
                for (int i = 0; i < customers.size(); i++) {
                    if (customers.get(i).getId() == pockeytTransaction.getCustomerId()) {
                        if (pockeytTransaction.getTax() != cloverTransaction.getTaxAmount() || pockeytTransaction.getTotal() != cloverTransaction.getAmount()) {
                            pockeytTransactionViewModel.fetchPockeytTransaction(cloverTransaction.getOrderId());
                        }
                        SelectedCustomerViewModel selectedCustomerViewModel = ViewModelProviders.of(this).get(SelectedCustomerViewModel.class);
                        selectedCustomerViewModel.setCustomer(customers.get(i));
                        break;
                    }
                }
            });
        });

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
            mAuthReady.onNext(business);
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



    private void connectToPusher(BusinessModel business) {
        Intent intent = new Intent(MainActivity.this, PusherService.class);
        intent.putExtra(KEY_BUSINESS_SLUG, business.getSlug());
        intent.putExtra(KEY_BUSINESS_TOKEN, business.getToken().getValue());
        startService(intent);
    }

    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            CustomerModel customer = bundle.getParcelable(NotificationHandler.KEY_CUSTOMER_BROADCAST);
            String type = bundle.getString(NotificationHandler.KEY_TYPE_BROADCAST);
            CustomerPubSubModel customerPubSubModel = new CustomerPubSubModel(type, customer);
            mCustomerPubSub.onNext(customerPubSubModel);
            setCustomersViewModel(customer, type);
        }
    }

    private void setCustomersViewModel(CustomerModel customer, String type) {
        CustomersViewModel customersViewModel = ViewModelProviders.of(this).get(CustomersViewModel.class);
        List<CustomerModel> customers = customersViewModel.getCustomers().getValue();
        Boolean customerFound = false;
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == customer.getId()) {
                customerFound = true;
                if (type.equals("customer_exit_paid")) {
                    customers.remove(i);
                } else {
                    customers.set(i, customer);
                }
                break;
            }
        }
        if (!customerFound && !type.equals("customer_exit_paid")) {
           customers.add(customer);
        }
        customersViewModel.setCustomers(customers);
    }

    @Override
    protected void onResume() {
        mNotificationBroadcastReceiver = new NotificationBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter(NotificationHandler.NOTIFICATION_BROADCAST_CUSTOMER_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationBroadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mNotificationBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationBroadcastReceiver);
        }
        mNotificationBroadcastReceiver = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
        mCloverTenderConnecter.destroy();
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
