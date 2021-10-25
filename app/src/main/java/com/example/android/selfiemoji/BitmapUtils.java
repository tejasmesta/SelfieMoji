package com.example.android.selfiemoji;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;

public class BitmapUtils {
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    static Bitmap resamplePic(Context context,String imagePath)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetHeight = metrics.heightPixels;
        int targetWidth = metrics.widthPixels;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath,bmOptions);
        int photoHeight = bmOptions.outHeight;
        int photoWidth = bmOptions.outWidth;

        int scaleFactor = Math.min(photoWidth/targetWidth,photoHeight/targetHeight);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    static File createTempImageFile(Context context) throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    static boolean deleteImageFile(Context context,String imagePath)
    {
        File imageFile = new File(imagePath);
        boolean deleted = imageFile.delete();
        if (!deleted) {
            String errorMessage = "Failed to Delete";
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }
        return deleted;
    }

    private static void galleryAddPic(Context context,String imagePath)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    static String saveImage(Context context, Bitmap image) {
        String savedImagePath = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/SelfieMoji");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            galleryAddPic(context, savedImagePath);
            String savedMessage = "saved";
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show();
        }
        return savedImagePath;
    }

    static void shareImage(Context context, String imagePath) {
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoURI = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        context.startActivity(shareIntent);
    }
}
