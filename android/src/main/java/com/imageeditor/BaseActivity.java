package com.imageeditor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;


import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ahmed Adel on 09/06/2017.
 */

public class BaseActivity extends AppCompatActivity {

    final static int IMAGE_RESULT = 200;
    protected static final int CAMERA_CODE = 0x0;
    protected static final int GALLERY_INTENT_CALLED = 0x1;
    protected static final int GALLERY_KITKAT_INTENT_CALLED = 0x2;
    protected static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY = 0x3;
    protected static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA = 0x4;
    protected static final int MY_PERMISSIONS_REQUEST_CAMERA = 0x5;
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    File tempFile = null;
    protected Bitmap bitmap;
    protected boolean _taken;
     String selectedImagePath;
    protected String selectedOutputPath;

    Uri captureMediaFile=null;
    private static final String PHOTO_PATH = "PhotoEditor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void openMedia() {

        final CharSequence[] items = {getString(R.string.camera), getString(R.string.gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.upload_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    startCameraActivity();
                    dialog.dismiss();
                } else if (item == 1) {
                    openGallery();
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Method to show user msg material menu
    public void showMenu(final int caller) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.access_media_permissions_msg));
        builder.setPositiveButton(getString(R.string.continue_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (caller == 1) {
                    ActivityCompat.requestPermissions(BaseActivity.this,
                            new String[]{Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA);

//                    ActivityCompat.requestPermissions(BaseActivity.this,
//                            new String[]{android.Manifest.permission.},
//                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA);
                } else {
                    ActivityCompat.requestPermissions(BaseActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    , Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.not_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(BaseActivity.this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();

    }


    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();
        Log.e("outputFileUri",outputFileUri+"");

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }



    // startCameraActivity() method fires once user shows camera option and CAMERA_CODE is an requestCode that will be used
    public void startCameraActivity() {

        int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                tempFile = File.createTempFile("image", ".png", new File(this.getCacheDir().getPath()));
                 captureMediaFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureMediaFile);

                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivityForResult(intent, IMAGE_RESULT);
//            startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
        } else {
            showMenu(1);
        }
    }

    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }


    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }


    /**
     * Create a file for saving an image
     */
    protected Uri getOutputMediaFile() {

        if (isSDCARDMounted()) {
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PHOTO_PATH);
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MediaAbstractActivity", getString(R.string.directory_create_fail));
                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            selectedOutputPath = mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg";
            Log.d("MediaAbstractActivity", "selected camera path "
                    + selectedOutputPath);
            mediaFile = new File(selectedOutputPath);
            return Uri.fromFile(mediaFile);
        } else {
            return null;
        }
    }

    protected boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);

    }

    // openGallery() method fires once user shows gallery option and GALLERY_INTENT_CALLED and GALLERY_KITKAT_INTENT_CALLED
    // are requestCode(s) that will be used in ProfileMediaSherlockFragmetActivity
    public void openGallery() {
        int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (!isKitKat) {
                Intent intent = new Intent();
                intent.setType("image/jpeg");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(intent, getString(R.string.upload_picker_title)),
                        GALLERY_INTENT_CALLED);
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
            }
        } else {
            showMenu(2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY: {
                // If request is cancelled, the result arrays are empty.
                int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    if (!isKitKat) {
                        Intent intent = new Intent();
                        intent.setType("image/jpeg");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(intent, getString(R.string.upload_picker_title)),
                                GALLERY_INTENT_CALLED);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/jpeg");
                        startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        tempFile = File.createTempFile("image", ".png", new File(Environment.DIRECTORY_PICTURES));
                        Uri captureMediaFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureMediaFile);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(intent, IMAGE_RESULT);

                } else {
                    Toast.makeText(this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_CAMERA:{
                // If request is cancelled, the result arrays are empty.
                int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                        Manifest.permission.CAMERA);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {


                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        tempFile = File.createTempFile("image", ".png", new File(Environment.DIRECTORY_PICTURES));
                        Uri captureMediaFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureMediaFile);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(intent, IMAGE_RESULT);





//                    startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);

                } else {
                    Toast.makeText(this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected String getPath(final Uri uri) {
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (GalleryUtils.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (GalleryUtils.isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return GalleryUtils.getDataColumn(this, contentUri, null, null);
            }
            // MediaProvider
            else if (GalleryUtils.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return GalleryUtils.getDataColumn(this, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return GalleryUtils.getDataColumn(this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    @Override
    protected void onDestroy() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
        super.onDestroy();
    }



}
