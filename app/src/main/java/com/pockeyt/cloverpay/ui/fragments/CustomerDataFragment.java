package com.pockeyt.cloverpay.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.CustomerPusherModel;
import com.pockeyt.cloverpay.models.PurchasedItemModel;
import com.pockeyt.cloverpay.models.RecentPostInteractionModel;
import com.pockeyt.cloverpay.models.RecentTransactionModel;
import com.pockeyt.cloverpay.ui.activities.MainActivity;
import com.pockeyt.cloverpay.ui.viewModels.BusinessViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;
import com.pockeyt.cloverpay.utils.DateHelpers;
import com.pockeyt.cloverpay.utils.DisplayHelpers;
import com.pockeyt.cloverpay.utils.ImageLoader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class CustomerDataFragment extends Fragment {
    private static final String TAG = CustomerDataFragment.class.getSimpleName();
    CustomerModel mCustomer;
    BusinessModel mBusiness;
    SelectedCustomerViewModel mSelectedCustomerViewModel;
    BusinessViewModel mBusinessViewModel;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_data, container, false);
        getSelectedCustomerViewModel().getCustomer().observe(this, customer -> {
            mCustomer = customer;
            mBusiness = getBusinessViewModel().getBusiness().getValue();
             setFragmentUI(view);
        });

        setUpdateCustomerWatcher();
        if (mCustomer == null) {
            setFragUINoCustomer(view);
        }
        return view;
    }

    private SelectedCustomerViewModel getSelectedCustomerViewModel() {
        if (mSelectedCustomerViewModel == null) {
            mSelectedCustomerViewModel = ViewModelProviders.of(getActivity()).get(SelectedCustomerViewModel.class);
        }
        return mSelectedCustomerViewModel;
    }

    private BusinessViewModel getBusinessViewModel() {
        if (mBusinessViewModel == null) {
            mBusinessViewModel = ViewModelProviders.of(getActivity()).get(BusinessViewModel.class);
        }
        return mBusinessViewModel;
    }

    private void setFragUINoCustomer(View view) {
        CardView recentTransactionCardView = view.findViewById(R.id.recentTransactionCard);
        recentTransactionCardView.setVisibility(View.GONE);

        CardView lastViewedPostCardView = view.findViewById(R.id.lastViewedPostCard);
        lastViewedPostCardView.setVisibility(View.GONE);
    }

    private void setUpdateCustomerWatcher() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            PublishSubject<CustomerPusherModel> customerPusherSubject = ((MainActivity) activity).getCustomerPusherPubSub();
            Disposable disposable = customerPusherSubject.subscribe(this::updateCustomer);
            mCompositeDisposable.add(disposable);
        }
    }

    private void updateCustomer(CustomerPusherModel customer) {
        if ((mCustomer != null) && (mCustomer.getId() == customer.getCustomer().getId())) {
            getSelectedCustomerViewModel().setCustomer(customer.getCustomer());
        }
    }

    private void setFragmentUI(View view) {
        setRecentTransactionData(mCustomer.getRecentTransaction(), view);
        setRecentPostInteractionData(mCustomer.getRecentPostInteraction(), view);
    }

    private void setRecentPostInteractionData(RecentPostInteractionModel recentPostInteraction, View view) {
        if (recentPostInteraction.getHasRecentPostInteraction()) {
            CardView lastViewedPostCardView = view.findViewById(R.id.lastViewedPostCard);
            lastViewedPostCardView.setVisibility(View.VISIBLE);
            final TextView postCardTop = view.findViewById(R.id.postCardTopTextView);
            postCardTop.setText(mBusiness.getBusinessName());

            RequestCreator creator = ImageLoader.load(getActivity(), mBusiness.getLogoUrl());
            creator.transform(new CropCircleTransformation()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    int imageDimensions = DisplayHelpers.dipToPixels(getActivity(), 40);
                    BitmapDrawable profileImage = new BitmapDrawable(getActivity().getResources(), Bitmap.createScaledBitmap(bitmap, imageDimensions, imageDimensions, true));
                    int h = profileImage.getIntrinsicHeight();
                    int w = profileImage.getIntrinsicWidth();
                    profileImage.setBounds(0, 0, w, h);
                    postCardTop.setCompoundDrawables(profileImage, null, null, null);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

            TextView titleText = view.findViewById(R.id.titleText);
            final TextView messageText = view.findViewById(R.id.messageText);
            if (recentPostInteraction.getEvent()) {
                titleText.setVisibility(View.VISIBLE);
                titleText.setText(recentPostInteraction.getTitle());
                messageText.setText(recentPostInteraction.getBody());

            } else {
                titleText.setVisibility(View.GONE);
                messageText.setText(recentPostInteraction.getMessage());
            }


            final ImageView recentInteractedPostImage = view.findViewById(R.id.recentInteractedPostImage);
            recentInteractedPostImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    recentInteractedPostImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    RequestCreator creatorPostImage = ImageLoader.load(getActivity(), recentPostInteraction.getPostImageUrl());
                    creatorPostImage.resize(recentInteractedPostImage.getWidth(), 0).into(recentInteractedPostImage);
                }
            });

        } else {
            CardView lastViewedPostCardView = view.findViewById(R.id.lastViewedPostCard);
            lastViewedPostCardView.setVisibility(View.GONE);
        }

    }

    private void setRecentTransactionData(RecentTransactionModel recentTransaction, View view) {
        if (recentTransaction.getHasRecentTransaction()) {

            CardView recentTransactionCardView = view.findViewById(R.id.recentTransactionCard);
            recentTransactionCardView.setVisibility(View.VISIBLE);

            TextView businessName = view.findViewById(R.id.businessName);

            businessName.setText(mBusiness.getBusinessName());

            TextView purchaseDate = view.findViewById(R.id.lastPurchaseDate);
            CharSequence relativeDate = DateHelpers.formatToRelative(getActivity(), recentTransaction.getPurchasedOnDate());
            purchaseDate.setText(relativeDate);

            TextView taxAmount = view.findViewById(R.id.taxValue);
            String taxAmountValue = setValueToCurrency((double) recentTransaction.getTax());
            taxAmount.setText(taxAmountValue);

            TextView tipAmount = view.findViewById(R.id.tipValue);
            String tipAmountValue = setValueToCurrency((double) recentTransaction.getTip());
            tipAmount.setText(tipAmountValue);

            TextView totalAmount = view.findViewById(R.id.totalValue);
            String totalAmountValue = setValueToCurrency((double) recentTransaction.getTotal());
            totalAmount.setText(totalAmountValue);

            try {
                setPurchasedItemsRows(recentTransaction, view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            CardView recentTransactionCardView = view.findViewById(R.id.recentTransactionCard);
            recentTransactionCardView.setVisibility(View.GONE);
        }
    }

    private void setPurchasedItemsRows(RecentTransactionModel recentTransaction, View view) throws JSONException {
        TableLayout tableLayout = view.findViewById(R.id.receiptLayout);
        PurchasedItemModel[] purchasedItems = recentTransaction.getPurchasedItems();

        int numberRows = tableLayout.getChildCount();
        if (numberRows > 6) {
            for (int i = numberRows -1; i > 0; i--) {
                int distFromEnd = i - (tableLayout.getChildCount() -1);
                if (Math.abs(distFromEnd) > 3 && i != 0) {
                    tableLayout.removeViewAt(i);
                }
            }
        }

        for (int i = 0; i < purchasedItems.length; i++) {
            PurchasedItemModel purchasedItem = purchasedItems[i];
            TableRow row = new TableRow(getActivity());

            TextView itemNameAndQuantity = new TextView(getActivity());
            int itemPadding = DisplayHelpers.dipToPixels(getActivity(), 3);
            itemNameAndQuantity.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);
            int quantity = purchasedItem.getQuantity();
            String name = purchasedItem.getName();
            String itemNameAndQuantityValue = quantity > 1 ? quantity + "x " + name : name;
            itemNameAndQuantity.setText(itemNameAndQuantityValue);
            itemNameAndQuantity.setLayoutParams(new TableRow.LayoutParams(1));
            itemNameAndQuantity.setTextColor(Color.BLACK);
            row.addView(itemNameAndQuantity);

            TextView itemTotalPrice = new TextView(getActivity());
            itemTotalPrice.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);
            itemTotalPrice.setGravity(Gravity.END);
            itemTotalPrice.setTextColor(Color.BLACK);

            String totalPrice = setValueToCurrency(quantity * (double) purchasedItem.getPrice());
            itemTotalPrice.setText(totalPrice);
            row.addView(itemTotalPrice);
            row.setTag("item");

            tableLayout.addView(row, i + 2);
        }
    }

    private String setValueToCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return format.format(amount / 100);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
