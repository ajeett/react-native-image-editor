package com.imageeditor.photoedit;

public interface PhotoTaken {


    void onPhotoTaken(String path,String savePath);

    void sendCallback(String path);


}
