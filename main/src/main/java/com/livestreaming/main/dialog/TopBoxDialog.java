package com.livestreaming.main.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.R;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.main.presenter.CheckLivePresenter;
import com.bumptech.glide.Glide;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class TopBoxDialog {

    private Dialog dialog;
    private Context context;
    SVGAImageView coverImg;
    ImageView avatar;
    private LiveBean room;

    public TopBoxDialog(Context context) {
        // Create a dialog with the transparent themet
        this.context = context;
        dialog = new Dialog(context, R.style.TransparentDialog);
        dialog.setContentView(R.layout.custom_dialog);

        // Align the dialog at the top of the screen
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();

            // Set the dialog position and size
            layoutParams.gravity = Gravity.TOP;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.y = (int) (20 * context.getResources().getDisplayMetrics().density);  // Set margin from top

            // Make the dialog non-focusable to allow outside clicks
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            // Set the window background to transparent
            window.setBackgroundDrawableResource(android.R.color.transparent);

            // Remove default dimming effect
            window.setDimAmount(0f);  // No dimming, fully transparent background

            window.setAttributes(layoutParams);
            // Make the status bar transparent
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Optionally hide the status bar, if you want it to be hidden
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            // Set the status bar color to transparent
            window.setStatusBarColor(0x00000000);  // Transparent
        }

        // Ensure dialog is canceled on touch outside
        dialog.setCanceledOnTouchOutside(true);
    }

    private void parseSvga() {
        try {
            InputStream inputStream = dialog.getContext().getAssets().open("entry.svga");
            SVGAParser parser = new SVGAParser(dialog.getContext());
            parser.parse(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity svgaVideoEntity) {
                    // Set parsed SVGA to SVGAImageView
                    coverImg.setVideoItem(svgaVideoEntity);
                    // Start animation
                    coverImg.startAnimation();
                }

                @Override
                public void onError() {
                    // Handle error
                }
            }, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDialog(@Nullable String message, @Nullable String img, LiveBean room_name) {
        TextView messageTextView = dialog.findViewById(R.id.dialog_message);
        coverImg = dialog.findViewById(R.id.cover_img);
        avatar = dialog.findViewById(R.id.avatar);
        this.room = room_name;
        Glide.with(avatar.getContext()).load(img).into(avatar);
        parseSvga();
        messageTextView.setText(message);

        View rootView = dialog.findViewById(android.R.id.content);

        // Apply enter animation
        Animation enterAnimation = AnimationUtils.loadAnimation(context, R.anim.enter_from_left);
        rootView.startAnimation(enterAnimation); // Animate the entire content view
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room.getUid() != CommonAppConfig.getInstance().getUid()) {
                    watchLive(room);
                }
            }
        });
        dialog.show();

        // Schedule the exit animation
        new Handler().postDelayed(() -> exitDialog(rootView), 3000); // Show for 3 seconds

    }

    public void watchLive(LiveBean bean) {
        new CheckLivePresenter(context).watchLive(bean);
    }

    private void exitDialog(View rootView) {
        // Apply exit animation
        Animation exitAnimation = AnimationUtils.loadAnimation(context, R.anim.exit_to_right);
        rootView.startAnimation(exitAnimation);

        // Dismiss the dialog after the exit animation ends
        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialog.dismiss(); // Close the dialog
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
}
