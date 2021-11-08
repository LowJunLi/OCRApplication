package com.example.ocrapplication;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.List;

public class OcrLoader extends AsyncTaskLoader<List<String>>
{
    private final Bitmap photo;
    private final OpenCVOcr openCVOcr;

    public OcrLoader(@NonNull Context context, Bitmap photo, OpenCVOcr openCVOcr)
    {
        super(context);
        this.photo = photo;
        this.openCVOcr = openCVOcr;
    }

    @Nullable
    @Override
    public List<String> loadInBackground()
    {
        return openCVOcr.processImage(photo);
    }

    @Override
    protected void onStartLoading()
    {
        super.onStartLoading();
        forceLoad();
    }

}
