package com.pockeyt.cloverpay.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.utils.DisplayHelpers;
import com.pockeyt.cloverpay.utils.ImageLoader;
import com.pockeyt.cloverpay.utils.Interfaces;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class CustomerListAdapter extends RecyclerView.Adapter {
    private final Interfaces.OnListCustomerSelectedInterface mListener;
    private CustomerModel[] mCustomers;
    private Context mContext;

    public CustomerListAdapter(CustomerModel[] customers, Interfaces.OnListCustomerSelectedInterface listener) {
        mCustomers = customers;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.customer_list_item, parent, false);
        return new CustomerListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CustomerListViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mCustomers.length;
    }

    private class CustomerListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCustomerRow;
        private int mIndex;

        public CustomerListViewHolder(View itemView) {
            super(itemView);
            mCustomerRow = itemView.findViewById(R.id.customerRow);

            itemView.setOnClickListener(this);
        }

        public void bindView(int position) {
            mIndex = position;
            setCustomerName();
            setCustomerImage();
        }


        @Override
        public void onClick(View v) {
            mListener.onListCustomerSelected(mCustomers[mIndex]);
        }

        private void setCustomerName() {
            String customerName = mCustomers[mIndex].getFirstName() + " " + mCustomers[mIndex].getLastName();
            mCustomerRow.setText(customerName);
        }

        private void setCustomerImage() {
            RequestCreator creator = ImageLoader.load(mContext, mCustomers[mIndex].getPhotoUrl());
            creator.transform(new CropCircleTransformation()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    int imageDimensions = DisplayHelpers.dipToPixels(mContext, 70);
                    BitmapDrawable customerImage = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, imageDimensions, imageDimensions, true));
                    int h = customerImage.getIntrinsicHeight();
                    int w = customerImage.getIntrinsicWidth();
                    customerImage.setBounds(0,0, w, h);
                    mCustomerRow.setCompoundDrawables(customerImage, null, null, null);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }

    }
}
