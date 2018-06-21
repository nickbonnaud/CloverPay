package com.pockeyt.cloverpay.utils;


import android.content.Context;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class ImageLoader {

    public static RequestCreator load(Context context, String imageUrl) {
        RequestCreator picasso = Picasso.with(context).load(imageUrl);
        return picasso;
    }
}
