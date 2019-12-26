package com.imageeditor.testing;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.imageeditor.ImageEditorModule;

/**
 * Created by rusfearuth on 10.04.17.
 */

public class TestableImagePickerModule extends ImageEditorModule
{
    public TestableImagePickerModule(ReactApplicationContext reactContext,
                                     @StyleRes int dialogThemeId)
    {
        super(reactContext, dialogThemeId);
    }

    public void setCallback(@NonNull final Callback callback)
    {
        this.callback = callback;
    }

    public void setCameraCaptureUri(@Nullable final Uri uri)
    {
        this.cameraCaptureURI = uri;
    }
}
