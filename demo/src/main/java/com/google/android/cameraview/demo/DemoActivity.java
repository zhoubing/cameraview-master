/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview.demo;

import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Surface;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraFilter;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class DemoActivity extends FragmentActivity {
    private ProcessCameraProvider cameraProvider;
    private Preview previewUseCase = null;
    private PreviewView mPreviewView;

    private CameraSelector cameraSelector;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private ImageAnalysis analysisUseCase;
    private int count = 0;
    private long lastTime = 0L;
    private long lastShowTime = 0L;
    private long maxFrameMs = 0L;
    private long minFrameMs = Long.MAX_VALUE;
    private Camera camera;

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mPreviewView = findViewById(R.id.preview_view);

        ListenableFuture<ProcessCameraProvider> processCameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        processCameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = processCameraProviderListenableFuture.get();
                    Preview preview = new Preview.Builder().setTargetRotation(Surface.ROTATION_180).build();
                    //
                    mPreviewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
                    //创建图像捕捉
                    ImageCapture mImageCapture = new ImageCapture.Builder().build();
                    CameraSelector cameraSelector = new CameraSelector.Builder().addCameraFilter(
                            new CameraFilter() {
                                @NonNull
                                @Override
                                public List<CameraInfo> filter(
                                        @NonNull List<CameraInfo> cameraInfos) {
                                    return cameraInfos;
                                }
                            }).requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    try {
                        cameraProvider.unbindAll();
                        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                        mPreviewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
                        //请注意,这里新增了一个ImageCapture
                        Camera camera = cameraProvider.bindToLifecycle(DemoActivity.this, cameraSelector, preview, mImageCapture);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            /*
            这一步是绑定预览界面，如果不需要预览界面，这一步克注释掉
            CameraX优势体验之一：预览界面可以根据开发者需求去取舍，而Camera1和Camera2则必须要预览界面
            */
            bindPreviewUseCase();
            // 这一步是绑定相机预览数据，可以获得相机每一帧的数据
            bindAnalysisUseCase();
        }
    }

    /**
     * 绑定预览界面。不需要预览界面可以不调用
     * */
    private void bindPreviewUseCase() {
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        builder.setTargetRotation(Surface.ROTATION_180).setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                previewUseCase
        );
    }

    private void bindAnalysisUseCase() {
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        ImageAnalysis.Builder builder = new ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9);
        analysisUseCase = builder.build();
        analysisUseCase.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        count += 1;
//                        long currentTime = SystemClock.elapsedRealtime();
//                        long d = currentTime - lastShowTime;
//                        maxFrameMs = maxFrameMs.coerceAtLeast(d);
//                        minFrameMs = minFrameMs.coerceAtMost(d);
//                        if ((currentTime - lastTime) >= 1000){
//                            lastTime = currentTime;
//                            count = 0;
//                            maxFrameMs = 0;
//                            minFrameMs = Long.MAX_VALUE;
//                        }
//                        lastShowTime = currentTime;

                        //必须close,相机才会下发下一帧数据,否则会一直阻塞相机下发数据
                        imageProxy.close();
                    }
                }
        );
        camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                analysisUseCase
        );
    }
}
