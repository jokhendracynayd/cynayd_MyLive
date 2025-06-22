package com.livestreaming.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

/**
 * Utility class to handle all Branch.io operations
 */
public class BranchHelper {
    
    private static final String TAG = "BranchHelper";
    private static final String DOMAIN = "myliveapp.app.link";
    
    public interface BranchLinkCallback {
        void onLinkCreated(String url);
        void onLinkError(String error);
    }
    
    /**
     * Creates a Branch deep link for sharing live streams
     *
     * @param context     Context
     * @param streamId    The stream ID to include in the link
     * @param title       Title for the shared content
     * @param description Description for the shared content
     * @param callback    Callback to receive the generated link
     */
    public static void createLiveStreamLink(Context context, String streamId, String title, String description, BranchLinkCallback callback) {
        if (TextUtils.isEmpty(streamId)) {
            callback.onLinkError("Stream ID cannot be empty");
            return;
        }
        
        // Create Branch Universal Object
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(streamId)
                .setTitle(TextUtils.isEmpty(title) ? "Join my live stream!" : title)
                .setContentDescription(TextUtils.isEmpty(description) ? "Check out this live stream on MyLive!" : description)
                .setContentMetadata(
                        new ContentMetadata().addCustomMetadata("stream_id", streamId)
                );

        // Create link properties
        LinkProperties linkProperties = new LinkProperties()
                .setChannel("sharing")
                .setFeature("live_stream")
                .addControlParameter("$android_url", "myapp://open?screen=LauncherActivity")
                .addControlParameter("stream", streamId);

        // Generate short URL
        branchUniversalObject.generateShortUrl(context, linkProperties, (url, error) -> {
            if (error == null) {
                callback.onLinkCreated(url);
            } else {
                Log.e(TAG, "Error creating Branch link: " + error.getMessage());
                callback.onLinkError(error.getMessage());
            }
        });
    }
    
    /**
     * Creates a Branch deep link synchronously (blocking call)
     * Only use this method when absolutely necessary, prefer the async version
     *
     * @param context     Context
     * @param streamId    The stream ID to include in the link
     * @param title       Title for the shared content
     * @return            The generated link or fallback URL if generation fails
     */
    public static String createLiveStreamLinkSync(Context context, String streamId, String title) {
        if (TextUtils.isEmpty(streamId)) {
            return DOMAIN + "?stream=" + streamId;
        }
        
        final AtomicReference<String> result = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Create Branch Universal Object
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(streamId)
                .setTitle(TextUtils.isEmpty(title) ? "Join my live stream!" : title)
                .setContentDescription("Check out this live stream on MyLive!");
        
        // Create link properties
        LinkProperties linkProperties = new LinkProperties()
                .setChannel("sharing")
                .setFeature("live_stream")
                .addControlParameter("stream", streamId);
        
        // Generate short URL
        branchUniversalObject.generateShortUrl(context, linkProperties, (url, error) -> {
            if (error == null) {
                result.set(url);
            } else {
                Log.e(TAG, "Error creating Branch link: " + error.getMessage());
                result.set(DOMAIN + "?stream=" + streamId);
            }
            latch.countDown();
        });
        
        try {
            latch.await(3, TimeUnit.SECONDS); // Wait max 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return result.get() != null ? result.get() : DOMAIN + "?stream=" + streamId;
    }
    
    /**
     * Shows the standard share sheet with a Branch link
     *
     * @param activity     Activity
     * @param streamId     The stream ID to include in the link
     * @param title        Title for the shared content
     * @param description  Description for the shared content
     */
    public static void showShareSheet(Activity activity, String streamId, String title, String description) {
        if (TextUtils.isEmpty(streamId)) {
            return;
        }
        
        // Create Branch Universal Object
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(streamId)
                .setTitle(TextUtils.isEmpty(title) ? "Join my live stream!" : title)
                .setContentDescription(TextUtils.isEmpty(description) ? "Check out this live stream on MyLive!" : description);
        
        // Create link properties
        LinkProperties linkProperties = new LinkProperties()
                .setChannel("sharing")
                .setFeature("live_stream")
                .addControlParameter("stream", streamId);
        
        // Show share sheet
        ShareSheetStyle shareSheetStyle = new ShareSheetStyle(activity, 
                "Share this live stream", 
                "Join my live stream on MyLive!")
                .setCopyUrlStyle(activity.getResources().getDrawable(android.R.drawable.ic_menu_send), "Copy link", "Link copied")
                .setMoreOptionStyle(activity.getResources().getDrawable(android.R.drawable.ic_menu_more), "More options");
        
        branchUniversalObject.showShareSheet(activity, linkProperties, shareSheetStyle, null);
    }
    
    /**
     * Share a link directly with a specific app
     *
     * @param context    Context
     * @param streamId   The stream ID to include in the link
     * @param packageName The package name of the app to share with
     */
    public static void shareToApp(Context context, String streamId, String packageName) {
        createLiveStreamLink(context, streamId, null, null, new BranchLinkCallback() {
            @Override
            public void onLinkCreated(String url) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Join my live stream: " + url);
                    intent.setPackage(packageName);
                    context.startActivity(intent);
                } catch (Exception e) {
                    // If specific app sharing fails, try general share
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Join my live stream: " + url);
                    context.startActivity(Intent.createChooser(intent, "Share via"));
                }
            }
            
            @Override
            public void onLinkError(String error) {
                Log.e(TAG, "Error sharing to app: " + error);
                // Fallback to regular sharing
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Join my live stream on MyLive: " + 
                                DOMAIN + "?stream=" + streamId);
                context.startActivity(Intent.createChooser(intent, "Share via"));
            }
        });
    }
    
    /**
     * Process deep link data from Branch
     * 
     * @param activity Activity
     * @param uri The URI from the intent
     * @param listener Callback for processing the deep link data
     */
    public static void processDeepLink(Activity activity, Uri uri, Branch.BranchReferralInitListener listener) {
        Log.d(TAG, "Processing deep link: " + (uri != null ? uri.toString() : "null"));
        
        // For debugging only - print all intent details
        Intent intent = activity.getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d(TAG, String.format("Intent extra %s: %s", key, value != null ? value.toString() : "null"));
                }
            }
        }
        
        try {
            // First try to get any data from the intent directly
            if (uri != null && uri.getQueryParameter("stream") != null) {
                String streamId = uri.getQueryParameter("stream");
                Log.d(TAG, "Found stream ID in URI directly: " + streamId);
                // Create mock JSON object with stream parameter
                JSONObject params = new JSONObject();
                params.put("stream", streamId);
                listener.onInitFinished(params, null);
                return;
            }
            
            // If no direct parameters, try Branch session building
            Branch.sessionBuilder(activity)
                  .withCallback((referringParams, error) -> {
                      if (error != null) {
                          Log.e(TAG, "Branch initialization error: " + error.getMessage());
                      } else {
                          Log.d(TAG, "Branch initialization success: " + 
                                (referringParams != null ? referringParams.toString() : "null"));
                      }
                      listener.onInitFinished(referringParams, error);
                  })
                  .withData(uri)
                  .init();
        } catch (Exception e) {
            Log.e(TAG, "Error processing deep link", e);
            listener.onInitFinished(new JSONObject(), new BranchError("Error processing deep link: " + e.getMessage(), BranchError.ERR_BRANCH_NO_CONNECTIVITY));
        }
    }

    public static void processBranchIoDeepLink(Activity activity, Intent intent, boolean newIntent, Uri uri, Branch.BranchReferralInitListener listener) {
        try {
            // Always force a new session for deep links to ensure proper handling
            intent.putExtra("branch_force_new_session", true);
            
            // Get the URI from the intent if available
            Uri deepLinkUri = intent.getData();
            
            // Build the Branch session with proper method chaining
            if (newIntent) {
                Branch.sessionBuilder(activity)
                    .withCallback(listener)
                    .withData(deepLinkUri)
                    .reInit();
            } else {
                Branch.sessionBuilder(activity)
                    .withCallback(listener)
                    .withData(deepLinkUri)
                    .init();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in processBranchIoDeepLink: " + e.getMessage());
            // Ensure callback is always called even if there's an error
            if (listener != null) {
                listener.onInitFinished(null, new BranchError("Error processing deep link", BranchError.ERR_BRANCH_NO_CONNECTIVITY));
            }
        }
    }
    /**
     * Extract stream ID from Branch data
     * 
     * @param referringParams The JSONObject containing Branch data
     * @return The stream ID or null if not found
     */
    public static String extractStreamId(JSONObject referringParams) {
        try {
            if (referringParams != null && referringParams.has("stream")) {
                return referringParams.getString("stream");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting stream ID", e);
        }
        return null;
    }
} 