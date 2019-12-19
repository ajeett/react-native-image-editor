package com.imagepicker.photoedit;

public interface PhotoTaken {


    void onPhotoTaken(String path);

    void sendCallback(String path);


}
