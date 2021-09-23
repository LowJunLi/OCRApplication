package com.example.ocrapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class OcrLoader extends AsyncTaskLoader<ArrayList<LogbookRow>>
{
    private Bitmap photo;

    public OcrLoader(Context context, Bitmap photo)
    {
        super(context);
        this.photo = photo;
    }

    @Nullable
    @Override
    public ArrayList<LogbookRow> loadInBackground()
    {
        return Ocr.getOcrResult(photo);
    }

    @Override
    protected void onStartLoading()
    {
        super.onStartLoading();
        forceLoad();
    }


}
