package com.pockeyt.cloverpay.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.CustomerPusherModel;
import com.pockeyt.cloverpay.ui.activities.MainActivity;
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
    View mView;
    CustomerModel mCustomer;
    CircularProgressButton mDealButton;
    TextView mDealText;
    CircularProgressButton mLoyaltyButton;
    TextView mLoyaltyText;
    SelectedCustomerViewModel mSelectedCustomerViewModel;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getSelectedCustomerViewModel().getCustomer().observe(this, customer -> {
            mCustomer = customer;
            setFragmentUI();
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


        setUpdatedCustomerWatcher();
        mView = inflater.inflate(R.layout.fragment_customer_pay, container, false);
        if (mCustomer == null) {
            setFragUINoCustomer();
        }
        return mView;
    }

    private SelectedCustomerViewModel getSelectedCustomerViewModel() {
        if (mSelectedCustomerViewModel == null) {
            mSelectedCustomerViewModel = ViewModelProviders.of(getActivity()).get(SelectedCustomerViewModel.class);
        }
        return mSelectedCustomerViewModel;
    }

    private void setUpdatedCustomerWatcher() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            PublishSubject<CustomerPusherModel> customerPusherSubject = ((MainActivity) activity).getCustomerPusherPubSub();
            Disposable disposable = customerPusherSubject.subscribe(this::updateCustomer);
            mCompositeDisposable.add(disposable);
        }
    }


    private void setFragmentUI() {
        mLoyaltyButton = mView.findViewById(R.id.loyaltyButton);
        mLoyaltyText = mView.findViewById(R.id.loyaltyText);
        mDealButton = mView.findViewById(R.id.dealButton);
        mDealText = mView.findViewById(R.id.dealText);
        TextView customerPayName = mView.findViewById(R.id.customerPayName);
        String fullName = mCustomer.getFirstName() + " " + mCustomer.getLastName();
        customerPayName.setText(fullName);

        Button payButton = mView.findViewById(R.id.payButton);
        String buttonText = "Charge " + mCustomer.getFirstName();
        payButton.setText(buttonText);
        payButton.setEnabled(true);

        ImageView customerPayImage = mView.findViewById(R.id.customerPayImage);
        ViewGroup.LayoutParams params = customerPayImage.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        customerPayImage.setLayoutParams(params);

        RequestCreator creator = ImageLoader.load(getActivity(), mCustomer.getLargePhotoUrl());
        creator.transform(new CropCircleTransformation()).into(customerPayImage);

        updateButtonLayout();
        setLoyaltyOnClickListener();
        setDealOnClickListener();
    }

    private void setFragUINoCustomer() {
        mLoyaltyButton = mView.findViewById(R.id.loyaltyButton);
        mLoyaltyText = mView.findViewById(R.id.loyaltyText);
        mDealButton = mView.findViewById(R.id.dealButton);
        mDealText = mView.findViewById(R.id.dealText);
        TextView customerPayName = mView.findViewById(R.id.customerPayName);
        customerPayName.setText("No Customer Selected");
        Button payButton = mView.findViewById(R.id.payButton);
        payButton.setText("Charge Customer");
        payButton.setEnabled(false);

        ImageView customerPayImage = mView.findViewById(R.id.customerPayImage);
        customerPayImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
        ViewGroup.LayoutParams params = customerPayImage.getLayoutParams();
        params.width = DisplayHelpers.dipToPixels(getActivity(), 160);
        customerPayImage.setLayoutParams(params);

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
            default:
                message = getString(R.string.try_again_error_message);
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void updateCustomer(CustomerPusherModel customer) {
        if ((mCustomer != null) && (mCustomer.getId() == customer.getCustomer().getId())) {
            updateView(customer.getType(), customer.getCustomer());
        }
    }

    public void updateView(String type, CustomerModel customer) {
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
                updatePayUI(customer);
                break;
        }
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

    private void updatePayUI(CustomerModel customer) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
