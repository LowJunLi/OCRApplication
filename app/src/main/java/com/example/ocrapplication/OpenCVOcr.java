package com.example.ocrapplication;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCVOcr
{
    private Mat originalImage, processedImage;

    public OpenCVOcr(@NonNull Bitmap img)
    {
        //bitmapToMat only accepts certain bitmap types, so this line ensure the bitmap is of type ARGB_8888
        Bitmap bmp32 = img.copy(Bitmap.Config.ARGB_8888, true);
        //convert bitmap to OpenCV Mat
        Utils.bitmapToMat(bmp32, originalImage);
    }

    public void processImage()
    {
        preprocessing();
    }

    private void preprocessing()
    {
        //Applying GaussianBlur on the Image
        Imgproc.GaussianBlur(originalImage, processedImage, new Size(15, 15), 0);
    }
}
