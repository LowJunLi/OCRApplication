package com.example.ocrapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;

public class Ocr
{
    public static ArrayList<LogbookRow> getOcrResult(Bitmap photo)
    {
        InputImage image = InputImage.fromBitmap(photo, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        ArrayList<LogbookRow> book = new ArrayList<>();

        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>()
        {
            @Override
            public void onSuccess(Text visionText)
            {
                processTextBlock(book, visionText);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {

            }
        });

        return book;
    }


    public static void processTextBlock(ArrayList<LogbookRow> book, Text result)
    {
        /*String resultText = result.getText();
        for (Text.TextBlock block : result.getTextBlocks())
        {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines())
            {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (Text.Element element : line.getElements())
                {
                    String elementText = element.getText();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }*/
        for (Text.TextBlock block : result.getTextBlocks())
        {
            String blockText = block.getText();
        }
    }

    private TextRecognizer getTextRecognizer()
    {
        // [START mlkit_local_doc_recognizer]
        TextRecognizer detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        // [END mlkit_local_doc_recognizer]

        return detector;
    }
}
