package com.livestreaming.common.upload

import android.content.Context
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions

/**
 * Created by http://www.yunbaokj.com on 2024/2/29.
 */
class AwsTransferUtil(
    context: Context,
    key: String,
    secretKey: String,
    endPoint: String,
) {
    private val networkLossHandler: TransferNetworkLossHandler = TransferNetworkLossHandler.getInstance(context)
    val mTransferUtility: TransferUtility
    var s3Client : AmazonS3Client
    init {


        val credentials = BasicAWSCredentials(key, secretKey)

        // Create an S3 client for DigitalOcean Spaces
         s3Client = AmazonS3Client(credentials)


        // Configure the endpoint and enable path-style access (specific to DigitalOcean Spaces)
        s3Client.setEndpoint(endPoint)
        s3Client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build())

        // Initialize the TransferUtility for uploading/downloading files
        mTransferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(context)
            .build()
    }


}
