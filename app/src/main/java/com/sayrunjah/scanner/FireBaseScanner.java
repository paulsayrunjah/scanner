package com.sayrunjah.scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FireBaseScanner {

    FirebaseVisionBarcodeDetectorOptions options;
    FirebaseVisionBarcodeDetector detector;
    String resultCode;

    public FireBaseScanner(Context context){
        FirebaseApp.initializeApp(context);
        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_AZTEC, FirebaseVisionBarcode.FORMAT_PDF417)
                        .build();

        detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);
    }

    public String scanCode(final Bitmap  byteArray) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(byteArray);



        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(barcodes -> {

                    if(barcodes.size() > 0){
                        try {
                            detector.close();
                            resultCode = barcodes.get(0).getRawValue();
                            /*taskBarcode.OnReceived(barcodes.get(0).getRawValue());*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return;
                    }
                    byteArray.recycle();
                })
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    // ...
                    Log.d("Barcode", "Faillll");
                });

        return resultCode;


    }

    public interface TaskBarcode{
        void OnReceived(String barcode);
    }
}
