package com.pockeyt.cloverpay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.ui.fragments.CustomerGridPagerFragment;
import com.pockeyt.cloverpay.utils.ImageLoader;
import com.pockeyt.cloverpay.utils.Interfaces;
import com.squareup.picasso.RequestCreator;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class CustomerGridAdapter extends RecyclerView.Adapter {
    private static final String TAG = CustomerGridAdapter.class.getSimpleName();
    private CustomerModel[] mCustomers;
    private Context mContext;
    private Interfaces.OnGridCustomerSelectedInterface mListener;
    private CustomerGridPagerFragment mCustomerGridPagerFragment;

    public CustomerGridAdapter(CustomerModel[] customers, CustomerGridPagerFragment customerGridPagerFragment) {
        mCustomers = customers;
        mCustomerGridPagerFragment = customerGridPagerFragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        mListener = mCustomerGridPagerFragment;
        View view = LayoutInflater.from(mContext).inflate(R.layout.customer_grid_item, parent, false);
        return new CustomerGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CustomerGridViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mCustomers.length;
    }

    private class CustomerGridViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCustomerName;
        private ImageView mCustomerImage;
        private int mIndex;

        public CustomerGridViewHolder(View itemView) {
            super(itemView);
            mCustomerName = itemView.findViewById(R.id.customerGridName);
            mCustomerImage = itemView.findViewById(R.id.customerGridImage);

            itemView.setOnClickListener(this);
        }

        public void bindView(int position) {
            mIndex = position;
            setCustomerName();
            setCustomerImage();
        }

        private void setCustomerName() {
            String customerName = mCustomers[mIndex].getFirstName() + " " + mCustomers[mIndex].getLastName();
            mCustomerName.setText(customerName);
        }

        private void setCustomerImage() {
            RequestCreator creator = ImageLoader.load(mContext, mCustomers[mIndex].getPhotoUrl());
            creator.transform(new CropCircleTransformation()).into(mCustomerImage);
        }

        @Override
        public void onClick(View v) {
            mListener.onGridCustomerSelected(mCustomers[mIndex]);
        }
    }
}
