package com.imageeditor.photoedit;

public interface PhotoTaken {


    void onPhotoTaken(String path);

    void sendCallback(String path);


}
