# Deprecated

CameraView is deprecated. No more development will be taking place.

Use [Jetpack CameraX](https://developer.android.com/jetpack/androidx/releases/camerax) instead.

# CameraView

This is not an official Google product.

CameraView aims to help Android developers easily integrate Camera features.

Requires API Level 9. The library uses Camera 1 API on API Level 9-20 and Camera2 on 21 and above.

| API Level | Camera API | Preview View |
|:---------:|------------|--------------|
| 9-13      | Camera1    | SurfaceView  |
| 14-20     | Camera1    | TextureView  |
| 21-23     | Camera2    | TextureView  |
| 24        | Camera2    | SurfaceView  |

## Features

- Camera preview by placing it in a layout XML (and calling the start method)
- Configuration by attributes
  - Aspect ratio (app:aspectRatio)
  - Auto-focus (app:autoFocus)
  - Flash (app:flash)

## Usage

```xml
<com.google.android.cameraview.CameraView
    android:id="@+id/camera"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:keepScreenOn="true"
    android:adjustViewBounds="true"
    app:autoFocus="true"
    app:aspectRatio="4:3"
    app:facing="back"
    app:flash="auto"/>
```

```java
    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }
```

You can see a complete usage in the demo app.

## Contribution

See [CONTRIBUTING.md](/CONTRIBUTING.md).




CameraX
预览画面旋转问题：

Preview#setTargetRotation API应该可以工作：https://developer.android.com/reference/androidx/camera/core/Preview#setTargetRotation(int)
要使此API工作，您的PreviewView必须处于COMPATIBLE模式：

PreviewView#setImplementationMode(ImplementationMode.COMPATIBLE);
您可以在GitHub上找到完整的示例：https://github.com/androidx/androidx/blob/androidx-main/camera/integration-tests/viewtestapp/src/main/java/androidx/camera/integration/view/PreviewViewFragment.java
我上传了一个预览旋转的屏幕记录。请看一下，如果这不是你想要的结果，请告诉我：https://github.com/xizhang/public-files/blob/main/stackoverflow74798791/preview_rotation.mp4
您也可以下载APK并自行测试：https://github.com/xizhang/public-files/blob/main/stackoverflow74798791/camera-testapp-view-debug.apk