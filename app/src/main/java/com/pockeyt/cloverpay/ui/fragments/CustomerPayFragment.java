package com.pockeyt.cloverpay.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.v1.Intents;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.models.CloverTransactionModel;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.CustomerPubSubModel;
import com.pockeyt.cloverpay.models.PockeytTransactionModel;
import com.pockeyt.cloverpay.ui.activities.MainActivity;
import com.pockeyt.cloverpay.ui.viewModels.CloverTransactionViewModel;
import com.pockeyt.cloverpay.ui.viewModels.PockeytTransactionViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;
import com.pockeyt.cloverpay.utils.DisplayHelpers;
import com.pockeyt.cloverpay.utils.ImageLoader;
import com.squareup.picasso.RequestCreator;

import org.json.JSONObject;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import retrofit2.Response;

public class CustomerPayFragment extends Fragment {
    private static final String TAG = CustomerPayFragment.class.getSimpleName();
    public static final String ALERT_ERROR_FRAGMENT = "alert_error_fragment";
    public static final int ALERT_ERROR_CODE = 31;
    View mView;
    CustomerModel mCustomer;
    CircularProgressButton mDealButton;
    CircularProgressButton mLoyaltyButton;
    Button mPayButton;
    TextView mDealText;
    TextView mLoyaltyText;
    SelectedCustomerViewModel mSelectedCustomerViewModel;
    CloverTransactionModel mCloverTransactionModel;
    PockeytTransactionModel mPockeytTransactionModel;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getSelectedCustomerViewModel().getCustomer().observe(this, customer -> {
            mCustomer = customer;
            if (mCustomer != null) {
                setFragmentUI();
            } else {
                setFragUINoCustomer();
            }

        });

        getSelectedCustomerViewModel().getIsLoadingDeal().observe(this, isLoadingDeal -> {
            if (isLoadingDeal) {
                startDealButtonAnimation();
            }
        });

        getSelectedCustomerViewModel().getIsLoadingLoyalty().observe(this, isLoadingLoyalty -> {
            if (isLoadingLoyalty) {
                startLoyaltyButtonAnimation();
            }
        });


        mView = inflater.inflate(R.layout.fragment_customer_pay, container, false);
        if (mCustomer == null) {
            setFragUINoCustomer();
        }
        setCustomerPubSub();
        return mView;
    }

    private SelectedCustomerViewModel getSelectedCustomerViewModel() {
        if (mSelectedCustomerViewModel == null) {
            mSelectedCustomerViewModel = ViewModelProviders.of(getActivity()).get(SelectedCustomerViewModel.class);
        }
        return mSelectedCustomerViewModel;
    }

    private void setFragmentUI() {
        mLoyaltyButton = mView.findViewById(R.id.loyaltyButton);
        mLoyaltyText = mView.findViewById(R.id.loyaltyText);
        mDealButton = mView.findViewById(R.id.dealButton);
        mDealText = mView.findViewById(R.id.dealText);
        TextView customerPayName = mView.findViewById(R.id.customerPayName);
        String fullName = mCustomer.getFirstName() + " " + mCustomer.getLastName();
        customerPayName.setText(fullName);

        mPayButton = mView.findViewById(R.id.payButton);
        String buttonText = "Charge " + mCustomer.getFirstName();
        mPayButton.setText(buttonText);
        mPayButton.setEnabled(true);
        mPayButton.setAlpha(1f);

        ImageView customerPayImage = mView.findViewById(R.id.customerPayImage);
        ViewGroup.LayoutParams params = customerPayImage.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        customerPayImage.setLayoutParams(params);

        RequestCreator creator = ImageLoader.load(getActivity(), mCustomer.getLargePhotoUrl());
        creator.transform(new CropCircleTransformation()).into(customerPayImage);

        updateButtonLayout();
        setLoyaltyOnClickListener();
        setDealOnClickListener();
        setPayOnClickListener();
    }

    private void setFragUINoCustomer() {
        mLoyaltyButton = mView.findViewById(R.id.loyaltyButton);
        mLoyaltyText = mView.findViewById(R.id.loyaltyText);
        mDealButton = mView.findViewById(R.id.dealButton);
        mDealText = mView.findViewById(R.id.dealText);
        ImageView customerPayImage = mView.findViewById(R.id.customerPayImage);
        customerPayImage.setImageResource(android.R.color.transparent);
        TextView customerPayName = mView.findViewById(R.id.customerPayName);
        customerPayName.setText("No Customer Selected");
        mPayButton = mView.findViewById(R.id.payButton);
        mPayButton.setText("Charge Customer");
        mPayButton.setEnabled(false);
        mPayButton.setAlpha(0.7f);

        mLoyaltyButton.setVisibility(View.GONE);
        mLoyaltyText.setVisibility(View.GONE);
        mDealButton.setVisibility(View.GONE);
        mDealText.setVisibility(View.GONE);
    }


    private void updateButtonLayout() {
        if (mCustomer.getLoyaltyCard().getHasReward()) {
            mLoyaltyButton.setVisibility(View.VISIBLE);
            mLoyaltyText.setVisibility(View.VISIBLE);
            String loyaltyTextString = mCustomer.getFirstName() + " has earned a " + mCustomer.getLoyaltyCard().getLoyaltyReward().toLowerCase();
            mLoyaltyText.setText(loyaltyTextString);
            mLoyaltyText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_loyalty_black_24dp, 0, 0, 0);
            if (!mCustomer.getDeal().getHasDeal()) {
                ConstraintSet set = new ConstraintSet();
                ConstraintLayout constraintLayout = mView.findViewById(R.id.payLayout);
                set.clone(constraintLayout);
                int constrainMarginText = DisplayHelpers.dipToPixels(getActivity(), 8);
                int constraintMarginButton = DisplayHelpers.dipToPixels(getActivity(), 32);

                set.connect(R.id.loyaltyText, ConstraintSet.TOP, R.id.payButton, ConstraintSet.BOTTOM, constrainMarginText);
                set.connect(R.id.loyaltyButton, ConstraintSet.TOP, R.id.loyaltyText, ConstraintSet.BOTTOM, constraintMarginButton);

                set.applyTo(constraintLayout);
            }
        } else {
            mLoyaltyButton.setVisibility(View.GONE);
            mLoyaltyText.setVisibility(View.GONE);
        }

        if (mCustomer.getDeal().getHasDeal()) {
            mDealButton.setVisibility(View.VISIBLE);
            String dealTextString = mCustomer.getFirstName() + " can redeem a " + mCustomer.getDeal().getDealItem().toLowerCase();
            mDealText.setText(dealTextString);
            mDealText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_black_24dp, 0, 0, 0);
            mDealText.setVisibility(View.VISIBLE);
            if (!mCustomer.getLoyaltyCard().getHasReward()) {
                ConstraintSet set = new ConstraintSet();
                ConstraintLayout constraintLayout = mView.findViewById(R.id.payLayout);
                set.clone(constraintLayout);
                int constrainMarginText = DisplayHelpers.dipToPixels(getActivity(), 8);
                int constraintMarginButton = DisplayHelpers.dipToPixels(getActivity(), 32);

                set.connect(R.id.dealText, ConstraintSet.TOP, R.id.payButton, ConstraintSet.BOTTOM, constrainMarginText);
                set.connect(R.id.dealButton, ConstraintSet.TOP, R.id.dealText, ConstraintSet.BOTTOM, constraintMarginButton);

                set.applyTo(constraintLayout);
            }
        } else {
            mDealButton.setVisibility(View.GONE);
            mDealText.setVisibility(View.GONE);
        }
    }

    private void setDealOnClickListener() {
        mDealButton.setOnClickListener(v -> {
            getSelectedCustomerViewModel().setIsLoadingDeal(true);
            startDealButtonAnimation();
            redeemDeal();
        });
    }

    private void startDealButtonAnimation() {
        mDealButton.startAnimation();
        String dealText = "Waiting for " + mCustomer.getFirstName() + " to accept redeem Deal offer.";
        mDealText.setText(dealText);
        mDealText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send_black_24dp, 0, 0, 0);
    }

    public void redeemDeal() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<JSONObject>> redeemDealObservable = apiInterface.doRequestRedeemDeal(mCustomer.getDeal().getDealId(), "PATCH");
        Disposable disposable = redeemDealObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleDealResult, this::handleDealError);
        mCompositeDisposable.add(disposable);
    }


    private void handleDealError(Throwable throwable) {
        errorRevertDealButton();
        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void handleDealResult(Response response) {
        if (response.isSuccessful()) {
            String message = "Notification Sent! Waiting " + mCustomer.getFirstName() + " approval.";
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        } else {
            try {
                errorRevertDealButton();
                JSONObject errorObject = new JSONObject(response.errorBody().string());
                String errorMsg = errorObject.getString("error");
                showError(errorMsg);
            } catch (Exception e) {
                errorRevertDealButton();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void errorRevertDealButton() {
        getSelectedCustomerViewModel().setIsLoadingDeal(false);
        if (mDealButton.isAnimating()) {
            mDealButton.revertAnimation();
        }
        mDealText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_black_24dp, 0, 0, 0);
    }

    private void setLoyaltyOnClickListener() {
        mLoyaltyButton.setOnClickListener(v -> {
            getSelectedCustomerViewModel().setIsLoadingLoyalty(true);
            startLoyaltyButtonAnimation();
            redeemLoyalty();
        });
    }

    private void startLoyaltyButtonAnimation() {
        mLoyaltyButton.startAnimation();
        String loyaltyText = "Waiting for " + mCustomer.getFirstName() + " to accept redeem Reward offer.";
        mLoyaltyText.setText(loyaltyText);
        mLoyaltyText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send_black_24dp, 0, 0, 0);
    }

    public void redeemLoyalty() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<JSONObject>> redeemLoyaltyObservable = apiInterface.doRequestRedeemLoyalty(mCustomer.getLoyaltyCard().getLoyaltyCardId(), "PATCH");
        Disposable disposable = redeemLoyaltyObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleLoyaltyResult, this::handleLoyaltyError);
        mCompositeDisposable.add(disposable);
    }

    private void handleLoyaltyResult(Response response) {
        if (response.isSuccessful()) {
            String message = "Notification Sent! Waiting " + mCustomer.getFirstName() + " approval.";
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        } else {
            try {
                errorRevertLoyaltyButton();
                JSONObject errorObject = new JSONObject(response.errorBody().string());
                String errorMsg = errorObject.getString("error");
                showError(errorMsg);
            } catch (Exception e) {
                errorRevertLoyaltyButton();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleLoyaltyError(Throwable throwable) {
        errorRevertLoyaltyButton();
        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void errorRevertLoyaltyButton() {
        getSelectedCustomerViewModel().setIsLoadingLoyalty(false);
        if (mLoyaltyButton.isAnimating()) {
            mLoyaltyButton.revertAnimation();
        }
        mLoyaltyText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_loyalty_black_24dp, 0, 0, 0);
    }

    private void setPayOnClickListener() {
        mPayButton.setOnClickListener(v -> {
            if (((MainActivity) getActivity()).getDidStartFromRegister()) {
                mPayButton.setEnabled(false);
                setViewModels();
                if (checkCustomerOwnsTransaction()) {
                    Log.d(TAG, "Customer owns transaction");
                    sendPaymentRequestToCustomer();
                } else {
                    Log.d(TAG, "customer does not own transaction");
                    showAlertErrorDialog();
                }
            } else {
                Toast.makeText(getContext(), "No transaction from Clover Register. Please return to the Register and select the Pockeyt Pay button when settling a transaction.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAlertErrorDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("title", "Warning! Is this the correct customer?");
        bundle.putString("message", "Please ensure you have selected the correct customer. Our records indicate " + mCustomer.getFirstName() + " does not own this transaction.");

        AlertErrorFragment savedFragment = (AlertErrorFragment) getActivity().getSupportFragmentManager().findFragmentByTag(ALERT_ERROR_FRAGMENT);
        if (savedFragment == null) {
            AlertErrorFragment alertErrorFragment = AlertErrorFragment.newInstance();
            alertErrorFragment.setArguments(bundle);
            alertErrorFragment.setTargetFragment(this, ALERT_ERROR_CODE);
            alertErrorFragment.show(getFragmentManager(), ALERT_ERROR_FRAGMENT);
        }
    }

    private void setViewModels() {
        CloverTransactionViewModel cloverTransactionViewModel = ViewModelProviders.of(getActivity()).get(CloverTransactionViewModel.class);
        mCloverTransactionModel = cloverTransactionViewModel.getCloverTransaction().getValue();
        PockeytTransactionViewModel pockeytTransactionViewModel = ViewModelProviders.of(getActivity()).get(PockeytTransactionViewModel.class);
        mPockeytTransactionModel = pockeytTransactionViewModel.getpockeytTransaction(mCloverTransactionModel.getOrderId()).getValue();
    }

    private void sendPaymentRequestToCustomer() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Integer pockeytTransactionId =  mPockeytTransactionModel == null ? null : mPockeytTransactionModel.getId();

        if (pockeytTransactionId == null) {
            Log.d(TAG, "Pockeyt transaction id is null");
        } else {
            Log.d(TAG, "Pockeyt transaction id is not null");
        }

        Observable<Response<JSONObject>> sendPayRequestObservable = apiInterface.doRequestPostTransaction("clover", mCloverTransactionModel.getOrderId(), mCustomer.getId(), mCloverTransactionModel.getAmount(), mCloverTransactionModel.getTaxAmount(), mCloverTransactionModel.getEmployeeId(), pockeytTransactionId);
        Disposable disposable = sendPayRequestObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlePayResult, this::handlePayError);
        mCompositeDisposable.add(disposable);
    }

    private void handlePayResult(Response response) {
        if (response.isSuccessful()) {
            Intent intent = new Intent(Intents.ACTION_START_REGISTER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getActivity().startActivity(intent);
        } else {
            try {
                JSONObject errorObject = new JSONObject(response.errorBody().string());
                String errorMsg = errorObject.getString("error");
                showError(errorMsg);
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handlePayError(Throwable throwable) {
        mPayButton.setEnabled(true);
        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private boolean checkCustomerOwnsTransaction() {
        if (mPockeytTransactionModel == null) {
            return true;
        }
        return mPockeytTransactionModel.getCustomerId() == mSelectedCustomerViewModel.getCustomer().getValue().getId();
    }

    private void showError(String errorMsg) {
        String message;
        switch (errorMsg) {
            case "unable_to_find_deal":
                message = "Unable to redeem deal. Customer's deal not found.";
                break;
            case "deal_already_redeemed":
                message = "Deal has already been redeemed!";
                break;
            case "deal_not_owned_by_business":
                message = "Permission denied. Deal is redeemable at a different business.";
                break;
            case "unable_to_find_loyalty_card'":
                message = "Unable to redeem loyalty reward. Customer's loyalty card not found.";
                break;
            case "no_rewards_to_redeem":
                message = "Customer has no loyalty rewards to redeem";
                break;
            case "loyalty_card_not_owned_by_business":
                message = "Permission denied. Loyalty reward is redeemable at a different business.";
                break;
            case "unable_to_charge_customer":
                message = "Oops! Something happened. Please try again.";
                break;
            default:
                message = getString(R.string.try_again_error_message);
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void setCustomerPubSub() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            PublishSubject<CustomerPubSubModel> customerPusherSubject = ((MainActivity) activity).getCustomerPubSub();
            Disposable disposable = customerPusherSubject.subscribe(this::updateCustomer);
            mCompositeDisposable.add(disposable);
        }
    }

    private void updateCustomer(CustomerPubSubModel customer) {
        if ((mCustomer != null) && (mCustomer.getId() == customer.getCustomer().getId())) {
            updateView(customer.getType(), customer.getCustomer());
        }
    }

    public void updateView(String type, CustomerModel customer) {
        Log.d(TAG, type);
        switch (type) {
            case "deal_redeemed":
            case "redeem_later_deal":
            case "wrong_deal":
                updateDealUI(customer, type);
                break;
            case "loyalty_redeemed":
            case "redeem_later_reward":
            case "not_earned_reward":
                updateRewardUI(customer, type);
                break;
            case "wrong_bill":
            case "error_bill":
                updatePayUIError(customer);
                break;
            case "customer_exit_paid":
                removeCustomer();
                break;
        }
    }

    private void removeCustomer() {
        getSelectedCustomerViewModel().setCustomer(null);
    }

    private void updateDealUI(CustomerModel customer, String type) {
        getSelectedCustomerViewModel().setIsLoadingDeal(false);
        if (mDealButton.isAnimating()) {
            if (type.equals("deal_redeemed")) {
                mDealButton.doneLoadingAnimation(R.color.colorPrimary, BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_48dp));
            }
            new Handler().postDelayed(() -> {
                if (customer.getDeal().getHasDeal()) {
                    mDealButton.revertAnimation();
                }
                getSelectedCustomerViewModel().setCustomer(customer);
            }, 1000);
        } else {
            getSelectedCustomerViewModel().setCustomer(customer);
        }
    }

    private void updateRewardUI(CustomerModel customer, String type) {
        getSelectedCustomerViewModel().setIsLoadingLoyalty(false);
        if (mLoyaltyButton.isAnimating()) {
            if (type.equals("loyalty_redeemed")) {
                mLoyaltyButton.doneLoadingAnimation(R.color.colorPrimary, BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_48dp));
            }
            new Handler().postDelayed(() -> {
                if (customer.getLoyaltyCard().getHasReward()) {
                    mLoyaltyButton.revertAnimation();
                }
                getSelectedCustomerViewModel().setCustomer(customer);
            }, 1000);
        } else {
            getSelectedCustomerViewModel().setCustomer(customer);
        }

    }

    private void updatePayUIError(CustomerModel customer) {
        getSelectedCustomerViewModel().setCustomer(customer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ALERT_ERROR_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getExtras().containsKey("charge_customer")) {
                    if (data.getExtras().getBoolean("charge_customer")) {
                        sendPaymentRequestToCustomer();
                    }
                }
            }
        }
    }
}
