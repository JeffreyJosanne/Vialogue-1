package com.comp.iitb.vialogue.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.activity.AudioRecordActivity;
import com.comp.iitb.vialogue.coordinators.OnSlideThumbnailClicked;
import com.comp.iitb.vialogue.coordinators.SharedRuntimeContent;
import com.comp.iitb.vialogue.library.Storage;
import com.comp.iitb.vialogue.models.ParseObjects.models.Resources.Image;
import com.comp.iitb.vialogue.models.ParseObjects.models.Slide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by ironstein on 09/03/17.
 */

public class SlideThumbnailsRecyclerViewAdapter extends RecyclerView.Adapter<SlideThumbnailsRecyclerViewAdapter.SlideViewHolder> {

    private Activity mActivity;
    private Context mContext = null;
    private int mCurrentSlidePosition;
    private ArrayList<byte []> mByteArrayList;
    private byte[] mDefaultImageByteArray;
    private OnSlideThumbnailClicked mOnSlideThumbnailClicked;

    private boolean LOAD_SLIDES = false;

    public SlideThumbnailsRecyclerViewAdapter(Activity activity, Context context, int currentSlidePosition, OnSlideThumbnailClicked onSlideThumbnailClicked) {
        mActivity = activity;
        mContext = context;
        mCurrentSlidePosition = currentSlidePosition;
        mOnSlideThumbnailClicked = onSlideThumbnailClicked;
        LOAD_SLIDES = true;
    }

    public SlideThumbnailsRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    public class SlideViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public RelativeLayout audioLayer;

        public SlideViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            audioLayer = (RelativeLayout) view.findViewById(R.id.audio_layer);
        }
    }

    @Override
    public SlideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View slideView = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_thumbnail, parent, false);
        return new SlideViewHolder(slideView);
    }

    @Override
    public void onBindViewHolder(SlideViewHolder slideViewHolder, final int position) {
        final Slide slide = SharedRuntimeContent.getSlideAt(position);

        if(!LOAD_SLIDES) {
            // use only dummy images everywhere
            Glide.with(mContext)
                    .load((new File(Storage.resourceToUri(mContext, R.drawable.app_logo).getPath())).getAbsolutePath())
                    .placeholder(R.drawable.app_logo)
                    .into(slideViewHolder.thumbnail);
        } else {
            // use actual thumbnails from the slides
            Glide.with(mContext)
                    .load(slide.getThumbnailUri(mContext))
//                    .load(slide.getThumbnailUrl(mContext))
                    .centerCrop()
                    .placeholder(R.drawable.app_logo)
                    .into(slideViewHolder.thumbnail);

            if(mCurrentSlidePosition == position) {
                slideViewHolder.thumbnail.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                int padding = mContext.getResources().getDimensionPixelOffset(R.dimen.padding_slide_thumbnails);
                slideViewHolder.thumbnail.setPadding(padding, padding, padding, padding);
            } else {
                slideViewHolder.thumbnail.setPadding(0, 0, 0, 0);
            }

            final int pos = position;
            if(slide.getSlideType() == Slide.SlideType.IMAGE) {
                if(position != mCurrentSlidePosition) {
                    // don't add onclickListener if the slide is the current slide itself
                    // (does not make sense loading this activity again for the same slide)
                    slideViewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnSlideThumbnailClicked.onClicked(slide, position);
                            mCurrentSlidePosition = position;
                            notifyDataSetChanged();
                        }
                    });
                }
                slideViewHolder.thumbnail.setColorFilter(Color.argb(0, 0, 0, 0)); // No Tint
                // audio layer
                if(!((Image) slide.getResource()).hasAudio()) {
                    slideViewHolder.audioLayer.setVisibility(View.VISIBLE);
                } else {
                    slideViewHolder.audioLayer.setVisibility(View.GONE);
                }
            } else {
                slideViewHolder.thumbnail.setColorFilter(Color.argb(200, 0, 0, 0)); // Dark Gray Tint
                slideViewHolder.audioLayer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return SharedRuntimeContent.getNumberOfSlides();
    }

}
