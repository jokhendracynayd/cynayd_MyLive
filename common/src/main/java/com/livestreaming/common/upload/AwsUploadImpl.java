package com.livestreaming.common.upload;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.utils.StringUtil;

import java.io.File;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * 亚马逊存储
 * Created by Sky.L on 2020-12-22
 */
public class AwsUploadImpl implements UploadStrategy {

    private static final String TAG = "awsUploadImpl";
    private List<UploadBean> mList;
    private int mIndex;
    private boolean mNeedCompress;
    private UploadCallback mUploadCallback;
    private Luban.Builder mLubanBuilder;
    private TransferUtility mTransferUtility;
    private String mBucketName;
    private TransferListener mTransferListener;
    private String mPrefix;
    private AmazonS3Client s3Client;
    private Handler mainHandler;

    public AwsUploadImpl(String key, String secretKey, String endPoint, String bucketName, String prefix) {
        // Use the passed bucketName instead of hardcoding it
        mBucketName = bucketName;
        mPrefix = prefix + "_";
        AwsTransferUtil awsTransferUtil = new AwsTransferUtil(CommonAppContext.getInstance(), key, secretKey, endPoint);
        mTransferUtility = awsTransferUtil.getMTransferUtility();
        s3Client = awsTransferUtil.getS3Client();
        mainHandler = new Handler(Looper.getMainLooper());

        // Simplified TransferListener implementation
        mTransferListener = new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                handleStateChange(state);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.e(TAG, "onProgressChanged-----上传进度---->");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "onError-----上传失败---->", ex);
            }
        };
    }

    private void handleStateChange(TransferState state) {
        if (TransferState.COMPLETED == state) {
            if (mList == null || mList.isEmpty()) {
                finishUpload(false);
                return;
            }

            UploadBean uploadBean = mList.get(mIndex);
            uploadBean.setSuccess(true);
            uploadBean.setRemoteFileName(StringUtil.contact(mPrefix, uploadBean.getRemoteFileName()));

            // Handle image compression after upload
            if (uploadBean.getType() == UploadBean.IMG && mNeedCompress) {
                handleImageCompression(uploadBean);
            }
            mIndex++;
            if (mIndex < mList.size()) {
                uploadNext();
            } else {
                finishUpload(true);
            }
        }
    }

    private void handleImageCompression(UploadBean uploadBean) {
        File compressedFile = uploadBean.getCompressFile();
        if (compressedFile != null && compressedFile.exists()) {
            File originFile = uploadBean.getOriginFile();
            if (originFile != null && !compressedFile.getAbsolutePath().equals(originFile.getAbsolutePath())) {
                compressedFile.delete();  // Delete the compressed image after upload
            }
        }
    }

    private void finishUpload(boolean success) {
        if (mUploadCallback != null) {
            mUploadCallback.onFinish(mList, success);
        }
    }

    @Override
    public void upload(List<UploadBean> list, boolean needCompress, UploadCallback callback) {
        if (callback == null) {
            return;
        }
        if (list == null || list.size() == 0) {
            callback.onFinish(list, false);
            return;
        }
        boolean hasFile = false;
        for (UploadBean bean : list) {
            if (bean.getOriginFile() != null) {
                hasFile = true;
                break;
            }
        }
        if (!hasFile) {
            callback.onFinish(list, true);
            return;
        }
        mList = list;
        mNeedCompress = needCompress;
        mUploadCallback = callback;
        mIndex = 0;
        uploadNext();

    }

    @Override
    public void cancelUpload() {
        if (mTransferUtility != null) {
            mTransferUtility.cancelAllWithType(TransferType.UPLOAD);
        }
        if (mList != null) {
            mList.clear();
        }
        mUploadCallback = null;
    }

    private void uploadNext() {
        UploadBean bean = null;
        while (mIndex < mList.size() && (bean = mList.get(mIndex)).getOriginFile() == null) {
            mIndex++;
        }
        if (mIndex >= mList.size()) {
            if (mUploadCallback != null) {
                mUploadCallback.onFinish(mList, true);
            }
            return;
        }
        if (bean.getType() == UploadBean.IMG) {
            bean.setRemoteFileName("upload/jpg/"+StringUtil.contact(StringUtil.generateFileName(), ".jpg"));
        } else if (bean.getType() == UploadBean.VIDEO) {
            bean.setRemoteFileName("upload/mp4/"+StringUtil.contact(StringUtil.generateFileName(), ".mp4"));
        } else if (bean.getType() == UploadBean.VOICE) {
            bean.setRemoteFileName("upload/m4a/"+StringUtil.contact(StringUtil.generateFileName(), ".m4a"));
        }
        if (bean.getType() == UploadBean.IMG && mNeedCompress) {
            if (mLubanBuilder == null) {
                mLubanBuilder = Luban.with(CommonAppContext.getInstance())
                        .ignoreBy(8)//8k以下不压缩
                        .setTargetDir(CommonAppConfig.IMAGE_PATH)
                        .setRenameListener(new OnRenameListener() {
                            @Override
                            public String rename(String filePath) {
                                return mList.get(mIndex).getRemoteFileName();
                            }
                        }).setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onSuccess(File file) {
                                UploadBean uploadBean = mList.get(mIndex);
                                uploadBean.setCompressFile(file);
                                uploadFile(uploadBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                uploadFile(mList.get(mIndex));
                            }
                        });
            }
            mLubanBuilder.load(bean.getOriginFile()).launch();
        } else {
            uploadFile(bean);
        }
    }

    private void uploadFile(UploadBean bean) {

        if (bean != null && mTransferUtility != null) {
            Log.e("awsUploadImpl", bean.getRemoteFileName());
            File uploadFile = bean.getOriginFile();
            if (bean.getType() == UploadBean.IMG && mNeedCompress) {
                File compressedFile = bean.getCompressFile();
                if (compressedFile != null && compressedFile.exists()) {
                    uploadFile = compressedFile;
                }
            }
            try {
                // Upload using TransferUtility
                TransferObserver observer = mTransferUtility.upload(
                        mBucketName,
                        bean.getRemoteFileName(),
                        uploadFile,
                        CannedAccessControlList.PublicRead
                );
                observer.setTransferListener(mTransferListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (mUploadCallback != null) {
                mUploadCallback.onFinish(mList, false);
            }
        }
    }

}
