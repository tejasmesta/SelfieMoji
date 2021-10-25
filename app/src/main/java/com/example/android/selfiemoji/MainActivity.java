package com.example.android.selfiemoji;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private ImageView mImageView;
    private Button mSelfiemojiButton;
    private FloatingActionButton mShare;
    private FloatingActionButton mSave;
    private FloatingActionButton mClear;
    private TextView mTitleTextView;
    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);
        mSelfiemojiButton = findViewById(R.id.selfiemoji_button);
        mShare = findViewById(R.id.share);
        mSave = findViewById(R.id.save);
        mClear = findViewById(R.id.clear);
        mTitleTextView = findViewById(R.id.title);
        mClear.setImageResource(R.drawable.ic_baseline_clear_24);
        mSave.setImageResource(R.drawable.ic_baseline_save_alt_24);
        mShare.setImageResource(R.drawable.ic_baseline_share_24);
    }

    public void selfiemojiMe(View view)
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);
        }
        else 
        {
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_STORAGE_PERMISSION:
            {
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    launchCamera();
                }
                else
                {
                    Toast.makeText(this,"Please grant the permissions",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void launchCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!=null)
        {
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                mTempPhotoPath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(this,FILE_PROVIDER_AUTHORITY,photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            processAndSetImage();
        } else {
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }

    private void processAndSetImage()
    {
        mSelfiemojiButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mShare.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.VISIBLE);
        mClear.setVisibility(View.VISIBLE);

        mResultsBitmap = BitmapUtils.resamplePic(this,mTempPhotoPath);
        mResultsBitmap = Emojifier.detectFaces(this, mResultsBitmap);
        mImageView.setImageBitmap(mResultsBitmap);
    }

    public void saveMe(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    public void shareMe(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        BitmapUtils.saveImage(this, mResultsBitmap);
        BitmapUtils.shareImage(this, mTempPhotoPath);
    }

    public void clearImage(View view)
    {
        mImageView.setImageResource(0);
        mSelfiemojiButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
        mShare.setVisibility(View.GONE);
        mSave.setVisibility(View.GONE);
        mClear.setVisibility(View.GONE);

        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
    }
}