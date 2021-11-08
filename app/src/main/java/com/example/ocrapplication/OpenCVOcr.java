package com.example.ocrapplication;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.rectangle;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenCVOcr
{
    static
    {
        if (!OpenCVLoader.initDebug())
        {
            Log.d("Check", "Unable to load OpenCV");
        }
        else
        {
            Log.d("Check", "OpenCV loaded");
        }
    }

    private Mat originalImage;

    public OpenCVOcr()
    {

    }

    /**
     * @return bitmap of the original image with green rectangle (after OCR)
     * @see <a href="https://stackoverflow.com/questions/39957955/how-to-convert-the-mat-object-to-a-bitmap-while-perserving-the-color"
     * How to convert the Mat object to a Bitmap while perserving the color?</a>
     */
    public Bitmap getProcessedImage()
    {
        Bitmap processedImageBitmap = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.RGB_565);
        //convert Mat to bitmap
        Utils.matToBitmap(originalImage, processedImageBitmap);
        return processedImageBitmap;
    }

    /**
     * @see <a href="https://answers.opencv.org/question/63847/how-to-extract-tables-from-an-image/"
     * How to extract tables from an image?</a>
     */
    public List<String> processImage(Bitmap photo)
    {
        originalImage = new Mat();
        //bitmapToMat only accepts certain bitmap types, so this line ensure the bitmap is of type ARGB_8888
        Bitmap bmp32 = photo.copy(Bitmap.Config.ARGB_8888, true);
        //convert bitmap to OpenCV Mat
        Utils.bitmapToMat(bmp32, originalImage);

        Mat grayImage = new Mat();
        //ARGB image has 4 channels
        // Transform source image to gray if it is not
        if (originalImage.channels() > 3)
        {
            cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        }
        else
        {
            grayImage = originalImage.clone();
        }

        // Apply adaptiveThreshold at the bitwise_not of gray
        Mat bitwiseNotGrayImage = new Mat();
        Core.bitwise_not(grayImage, bitwiseNotGrayImage);
        Imgproc.adaptiveThreshold(bitwiseNotGrayImage, bitwiseNotGrayImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);

        // Create the images that will use to extract the horizontal and vertical lines
        Mat verticalLine = bitwiseNotGrayImage.clone();
        Mat horizontalLine = bitwiseNotGrayImage.clone();

        int scale = 100; // modify this variable in order to increase/decrease the amount of lines to be detected
        // Specify size on horizontal axis
        int horizontalSize = horizontalLine.cols() / scale;
        // Create structure element for extracting horizontal lines through morphology operations
        Mat horizontalStructure = getStructuringElement(MORPH_RECT, new Size(horizontalSize, 1));

        // Apply morphology operations
        erode(horizontalLine, horizontalLine, horizontalStructure, new Point(-1, -1));
        dilate(horizontalLine, horizontalLine, horizontalStructure, new Point(-1, -1));
        //    dilate(horizontal, horizontal, horizontalStructure, Point(-1, -1)); // expand horizontal lines

        // Specify size on vertical axis
        int verticalSize = verticalLine.rows() / scale;

        // Create structure element for extracting vertical lines through morphology operations
        Mat verticalStructure = getStructuringElement(MORPH_RECT, new Size(1, verticalSize));

        // Apply morphology operations
        erode(verticalLine, verticalLine, verticalStructure, new Point(-1, -1));
        dilate(verticalLine, verticalLine, verticalStructure, new Point(-1, -1));
        //    dilate(vertical, vertical, verticalStructure, Point(-1, -1)); // expand vertical lines

        // create a mask which includes the tables
        Mat grid = new Mat();
        Core.add(horizontalLine, verticalLine, grid);


        // find the joints between the lines of the tables, we will use this information in order to discriminate
        // tables from pictures (tables will contain more than 4 joints while a picture only 4 (i.e. at the corners))
        Mat joint = new Mat();
        Core.bitwise_and(horizontalLine, verticalLine, joint);

        // Find external contours from the mask, which most probably will belong to tables or to images
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(grid, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        List<MatOfPoint2f> newContours = new ArrayList<>();
        for (MatOfPoint point : contours)
        {
            MatOfPoint2f newPoint = new MatOfPoint2f(point.toArray());
            newContours.add(newPoint);
        }
        MatOfPoint2f[] contoursPoly = new MatOfPoint2f[contours.size()];
        Arrays.fill(contoursPoly, new MatOfPoint2f()); //initialize array
        Rect[] boundRect = new Rect[contours.size()];
        Arrays.fill(boundRect, new Rect()); //initialize array
        List<Mat> rois = new ArrayList<>();
        Log.d("Check contours", "The contours size is " + contours.size());

        for (int i = 0; i < contours.size(); i++)
        {
            // find the area of each contour
            double area = contourArea(contours.get(i));
            //        // filter individual lines of blobs that might exist and they do not represent a table
            if (area < 1000) // value is randomly chosen, you will need to find that by yourself with trial and error procedure
                continue;

            approxPolyDP(newContours.get(i), contoursPoly[i], 3, true);
            boundRect[i] = boundingRect(contoursPoly[i]);

            /*
             * Reject rect with less than 32 height or width because MLKit OCR can only accept image with minimum 32 height and width
             */
            if(boundRect[i].height < 32 || boundRect[i].width < 32)
                continue;

            rois.add(new Mat(originalImage, boundRect[i]).clone());

            //drawContours(originalImage, contours, i, new Scalar(0, 0, 255), 2, 8, hierarchy, 0, new Point());
            rectangle(originalImage, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 255, 0), 2, 8, 0);
        }

        Log.d("Check rois", "The rois size is " + rois.size());


        List<String> data = new ArrayList<>(rois.size());
        for (int i = 0; i < rois.size(); ++i)
        {
            Bitmap imagePart = Bitmap.createBitmap(rois.get(i).cols(), rois.get(i).rows(), Bitmap.Config.RGB_565);
            //convert Mat to bitmap
            Utils.matToBitmap(rois.get(i), imagePart);

            /* Now you can do whatever post process you want
             * with the data within the rectangles/tables. */
            InputImage image = InputImage.fromBitmap(imagePart, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            try
            {
                Tasks.await(recognizer.process(image).addOnSuccessListener(visionText ->
                {
                    Log.i("Check OCR", "OCR success");
                    data.add(visionText.getText());
                }).addOnFailureListener(ex ->
                {
                    Log.e("Check OCR", "OCR fail because " + ex.getMessage());
                }));
            }
            catch (ExecutionException | InterruptedException ex)
            {
                Log.e("Check Async", "OCR fail because " + ex.getMessage());
            }

        }
        return data;
    }


}
