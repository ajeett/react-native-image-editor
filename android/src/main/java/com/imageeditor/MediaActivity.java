package com.imageeditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import androidx.annotation.RequiresApi;

import com.imageeditor.photoedit.PhotoTaken;


import java.io.File;

/**
 * Created by Ahmed Adel on 09/06/2017.
 */

public abstract class MediaActivity extends BaseActivity implements PhotoTaken {

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case RESULT_OK:
                Log.e("requestCode",requestCode+"");
                Log.e("requestCode",data+"");
                if (requestCode == GALLERY_INTENT_CALLED || requestCode == CAMERA_CODE
                        || requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                    if (requestCode == GALLERY_INTENT_CALLED) {
                        selectedImageUri = data.getData();
                        selectedImagePath = getPath(selectedImageUri);
                    } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                        selectedImageUri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        // Check for the freshest data.
                        if (selectedImageUri != null) {
                            getContentResolver().takePersistableUriPermission(
                                    selectedImageUri, takeFlags);
                            selectedImagePath = getPath(selectedImageUri);
                        }
                    }




                    else {
                        selectedImagePath = selectedOutputPath;
                    }

                    if (UtilFunctions.stringIsNotEmpty(selectedImagePath)) {
                        // decode image size
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(selectedImagePath, o);
                        // Find the correct scale value. It should be the power of
                        // 2.
                        int width_tmp = o.outWidth, height_tmp = o.outHeight;
                        Log.d("MediaActivity", "MediaActivity : image size : "
                                + width_tmp + " ; " + height_tmp);
                        final int MAX_SIZE = getResources().getDimensionPixelSize(
                                R.dimen.image_loader_post_width);
                        int scale = 1;
                        // while (true) {
                        // if (width_tmp / 2 < MAX_SIZE
                        // || height_tmp / 2 < MAX_SIZE)
                        // break;
                        // width_tmp /= 2;
                        // height_tmp /= 2;
                        // scale *= 2;
                        // }
                        if (height_tmp > MAX_SIZE || width_tmp > MAX_SIZE) {
                            if (width_tmp > height_tmp) {
                                scale = Math.round((float) height_tmp
                                        / (float) MAX_SIZE);
                            } else {
                                scale = Math.round((float) width_tmp
                                        / (float) MAX_SIZE);
                            }
                        }
                        Log.d("MediaActivity", "MediaActivity : scaling image by factor : " + scale);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = scale;
                        bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                        _taken = true;
                        onPhotoTaken(selectedImagePath);
                        System.gc();
                    }

                }

//                if (data != null && data.getExtras() != null) {
//                    FileOutputStream out = null;
//                    try {
//                        Bundle extras = data.getExtras();
//                        Bitmap imageBitmap = (Bitmap) extras.get("data");
//                        String mediaPath = File.createTempFile("image", ".png", new File(SystemUtility.getTempMediaDirectory(this))).getAbsolutePath();
//                        out = new FileOutputStream(mediaPath);
//                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                        cropImage(Uri.fromFile(new File(mediaPath)));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            if (out != null) {
//                                out.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }

                else if (requestCode == 200) {

                    selectedImageUri = captureMediaFile;

                    selectedImagePath = new File(this.getCacheDir(),selectedImageUri.getLastPathSegment()).getAbsolutePath();


                    if (UtilFunctions.stringIsNotEmpty(selectedImagePath)) {
                        // decode image size
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(selectedImagePath, o);
                        // Find the correct scale value. It should be the power of
                        // 2.
                        int width_tmp = o.outWidth, height_tmp = o.outHeight;
                        Log.d("MediaActivity", "MediaActivity : image size : "
                                + width_tmp + " ; " + height_tmp);
                        final int MAX_SIZE = getResources().getDimensionPixelSize(
                                R.dimen.image_loader_post_width);
                        int scale = 1;
                        // while (true) {
                        // if (width_tmp / 2 < MAX_SIZE
                        // || height_tmp / 2 < MAX_SIZE)
                        // break;
                        // width_tmp /= 2;
                        // height_tmp /= 2;
                        // scale *= 2;
                        // }
                        if (height_tmp > MAX_SIZE || width_tmp > MAX_SIZE) {
                            if (width_tmp > height_tmp) {
                                scale = Math.round((float) height_tmp
                                        / (float) MAX_SIZE);
                            } else {
                                scale = Math.round((float) width_tmp
                                        / (float) MAX_SIZE);
                            }
                        }
                        Log.d("MediaActivity", "MediaActivity : scaling image by factor : " + scale);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = scale;
                        bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                        _taken = true;
                        onPhotoTaken(selectedImagePath);
                        System.gc();
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getTempMediaDirectory(Context context){
        String state = Environment.getExternalStorageState();
        File dir = null;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            dir = context.getExternalCacheDir();
        }else{
            dir = context.getCacheDir();
        }

        if (dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        if (dir.exists() && dir.isDirectory()){
            return dir.getAbsolutePath();
        }
        return null;
    }


}

