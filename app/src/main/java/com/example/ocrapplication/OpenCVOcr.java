package com.example.ocrapplication;


import static org.opencv.core.CvType.CV_32S;
import static org.opencv.imgproc.Imgproc.CC_STAT_HEIGHT;
import static org.opencv.imgproc.Imgproc.CC_STAT_LEFT;
import static org.opencv.imgproc.Imgproc.CC_STAT_TOP;
import static org.opencv.imgproc.Imgproc.CC_STAT_WIDTH;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.connectedComponentsWithStats;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.rectangle;

import android.graphics.Bitmap;
import android.util.Log;


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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    private Mat originalImage, grid;

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
        Mat image = grid;
        Bitmap processedImageBitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);
        //convert Mat to bitmap
        Utils.matToBitmap(image, processedImageBitmap);
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

        //Increasing the contrast of the image
        //originalImage.convertTo(originalImage, -1, 3, 0);

        //Applying GaussianBlur on the Image (blur the image to reduce noise)
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(15, 15), 0);

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
        erode(horizontalLine, horizontalLine, horizontalStructure, new Point(-1, -1), 3);
        dilate(horizontalLine, horizontalLine, horizontalStructure, new Point(-1, -1), 3);
        //    dilate(horizontal, horizontal, horizontalStructure, Point(-1, -1)); // expand horizontal lines

        // Specify size on vertical axis
        int verticalSize = verticalLine.rows() / scale;

        // Create structure element for extracting vertical lines through morphology operations
        Mat verticalStructure = getStructuringElement(MORPH_RECT, new Size(1, verticalSize));

        // Apply morphology operations
        erode(verticalLine, verticalLine, verticalStructure, new Point(-1, -1), 3);
        dilate(verticalLine, verticalLine, verticalStructure, new Point(-1, -1), 3);
        //    dilate(vertical, vertical, verticalStructure, Point(-1, -1)); // expand vertical lines

        // create a mask which includes the tables
        grid = new Mat();
        Core.add(horizontalLine, verticalLine, grid);

        // find the joints between the lines of the tables, we will use this information in order to discriminate
        // tables from pictures (tables will contain more than 4 joints while a picture only 4 (i.e. at the corners))
        Mat joint = new Mat();
        Core.bitwise_and(horizontalLine, verticalLine, joint);

        //end of image processing

        // Find external contours from the mask, which most probably will belong to tables or to images
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(grid, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        int indexToMaxArea = 0;
        double maxarea = 0;
        //find the blob with maximum area
        for (int i = 0; i < contours.size(); i++)
        {
            double area;
            area = contourArea(contours.get(i), false);
            if (area > 50)
            {
                if (area > maxarea)
                {
                    maxarea = area;
                    indexToMaxArea = i;
                }
            }
        }

        List<Rect> boundRect = new ArrayList<>(contours.size());
        double totalHeight = 0;
        double totalWidth = 0;

        List<Rect> boxes = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++)
        {
            boundRect.add(boundingRect(contours.get(i)));

            if (i == indexToMaxArea)
            {
                continue;
            }

            int x = boundRect.get(i).x;
            int y = boundRect.get(i).y;
            int w = boundRect.get(i).width;
            int h = boundRect.get(i).height;

            /*
             * Reject rect with less than 32 height or width because MLKit OCR can only accept image with minimum 32 height and width
             */
            if (h < 80 || w < 80)
            {
                continue;
            }
            totalHeight += h;
            totalWidth += w;
            rectangle(originalImage, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 2, 8, 0);
            boxes.add(new Rect(x, y, w, h));
        }

        if (boxes.size() == 0)
        {
            return null; //no text is detected
        }

        double meanHeight = totalHeight / boxes.size();
        double meanWidth = totalWidth / boxes.size();

        Collections.sort(boxes, new Comparator<Rect>()
        {
            @Override
            public int compare(Rect o1, Rect o2)
            {
                double compareY = Math.abs(o1.tl().y - o2.tl().y);
                if (compareY > meanHeight / 2)//if y axis difference is large, then sort by y axis
                {
                    return Double.compare(o1.tl().y, o2.tl().y);
                }
                else
                {
                    return Double.compare(o1.tl().x, o2.tl().x);
                }
            }
        });

        final int colNum = 6;

        //Rect previous = boxes.get(0);
        List<List<String>> data = new ArrayList<>();

        List<String> singleRow = new ArrayList<>(colNum);
        for (int i = 0; i < boxes.size(); i++)
        {
            Mat temp = cropRect(boxes.get(i));
            String text = recognizeText(temp);

            //if the next box follow a date format, mean it is new row
            if (checkDateFormat(text))
            {
                if (singleRow.size() != 0)
                {
                    while (singleRow.size() != 6)
                    {
                        singleRow.add("");
                    }
                    data.add(singleRow);
                    singleRow = new ArrayList<>(); //use different address
                }
                singleRow.add(text);
                //previous = boxes.get(i);
            }
            else //same row, next column
            {
                singleRow.add(text);
                //previous = boxes.get(i);
                if (singleRow.size() == colNum)//exceed column number
                {
                    data.add(singleRow);
                    singleRow = new ArrayList<>();
                }
            }
        }

        if (singleRow.size() != 0)
        {
            data.add(singleRow);
        }

        List<String> temp = new ArrayList<>();
        for (List<String> row : data)
        {
            temp.addAll(row);
        }
        return temp;

    }


    private Mat cropRect(Rect rect_min)
    {
        Mat temp = originalImage.clone();
        return temp.submat(rect_min);
    }

    private String recognizeText(Mat img)
    {
        Bitmap imagePart = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.RGB_565);
        //convert Mat to bitmap
        Utils.matToBitmap(img, imagePart);

        /* Now you can do whatever post process you want
         * with the data within the rectangles/tables. */
        InputImage image = InputImage.fromBitmap(imagePart, 0);
        String text = "";
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        try
        {
            Task<Text> result = recognizer.process(image).addOnSuccessListener(visionText ->
            {
                Log.i("Check OCR", "OCR success");

            }).addOnFailureListener(ex ->
            {
                Log.e("Check OCR", "OCR fail because " + ex.getMessage());
            });

            Text detectedText = Tasks.await(result);
            text = detectedText.getText();
        }
        catch (ExecutionException | InterruptedException ex)
        {
            Log.e("Check Async", "OCR fail because " + ex.getMessage());
        }

        return text;
    }

    public boolean checkDateFormat(String text)
    {
        int numOfDash = 0;
        //return text.matches("^([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})$");
        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) == '-')
            {
                numOfDash++;
            }
        }

        return (numOfDash >= 2); //if a string has 2 dashes, it is a date format
    }
}
