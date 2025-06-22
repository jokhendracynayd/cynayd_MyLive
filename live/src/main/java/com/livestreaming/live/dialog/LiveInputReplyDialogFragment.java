package com.livestreaming.live.dialog;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.custom.MyRadioButton;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.bean.LiveChatBean;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

/**
 * Enhanced Live chat input dialog with reply functionality and quick replies
 */
public class LiveInputReplyDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private InputMethodManager imm;
    private EditText mInput;
    private CheckBox mCheckBox;
    private MyRadioButton mMyRadioButton;
    private String mHint1;
    private String mHint2;
    
    // Reply components
    private View mReplyContainer;
    private RoundedImageView mReplyAvatar;
    private TextView mReplyUsername;
    private TextView mReplyContent;
    private ImageView mReplyClose;
    
    // Quick reply components
    private View mQuickReplyContainer;
    private LinearLayout mQuickReplyLayout;
    private TextView[] mQuickReplyButtons;
    
    private LiveChatBean mReplyToMessage;
    private String[] mQuickReplies = {"👍", "❤️", "Nice!", "Agree", "Thanks!"};

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_chat_input_with_reply;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    public static LiveInputReplyDialogFragment newInstance(String danmuPrice, String coinName, String atName, LiveChatBean replyToMessage) {
        LiveInputReplyDialogFragment fragment = new LiveInputReplyDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_DANMU_PRICE, danmuPrice);
        bundle.putString(Constants.COIN_NAME, coinName);
        bundle.putString(Constants.AT_NAME, atName);
        if (replyToMessage != null) {
            bundle.putSerializable("reply_to_message", replyToMessage);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        initViews();
        setupReplyData();
        setupQuickReplies();
        setupInput();
        setupHints();
        
        // Animate reply container entrance
        if (mReplyToMessage != null) {
            animateReplyContainerEntrance();
        }
        
        // Show keyboard
        mInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInput.requestFocus();
                imm.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
            }
        }, 200);
    }
    
    private void initViews() {
        mInput = mRootView.findViewById(R.id.input);
        mCheckBox = mRootView.findViewById(R.id.danmu);
        mMyRadioButton = mRootView.findViewById(R.id.btn_send);
        
        // Reply components
        mReplyContainer = mRootView.findViewById(R.id.reply_container);
        mReplyAvatar = mRootView.findViewById(R.id.reply_avatar);
        mReplyUsername = mRootView.findViewById(R.id.reply_username);
        mReplyContent = mRootView.findViewById(R.id.reply_content);
        mReplyClose = mRootView.findViewById(R.id.reply_close);
        
        // Quick reply components
        mQuickReplyContainer = mRootView.findViewById(R.id.quick_reply_container);
        mQuickReplyLayout = mRootView.findViewById(R.id.quick_reply_layout);
        
        mMyRadioButton.setOnClickListener(this);
        mReplyClose.setOnClickListener(this);
        
        // Initialize quick reply buttons
        mQuickReplyButtons = new TextView[5];
        mQuickReplyButtons[0] = mRootView.findViewById(R.id.quick_reply_1);
        mQuickReplyButtons[1] = mRootView.findViewById(R.id.quick_reply_2);
        mQuickReplyButtons[2] = mRootView.findViewById(R.id.quick_reply_3);
        mQuickReplyButtons[3] = mRootView.findViewById(R.id.quick_reply_4);
        mQuickReplyButtons[4] = mRootView.findViewById(R.id.quick_reply_5);
        
        for (TextView button : mQuickReplyButtons) {
            if (button != null) {
                button.setOnClickListener(this);
            }
        }
    }
    
    private void setupReplyData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mReplyToMessage = (LiveChatBean) bundle.getSerializable("reply_to_message");
        }
        
        if (mReplyToMessage != null) {
            mReplyContainer.setVisibility(View.VISIBLE);
            
            // Set reply avatar with subtle animation
            if (!TextUtils.isEmpty(mReplyToMessage.avatar)) {
                Glide.with(this)
                     .load(mReplyToMessage.avatar)
                     .error(R.mipmap.icon_avatar_none)
                     .placeholder(R.mipmap.icon_avatar_none)
                     .into(mReplyAvatar);
            } else {
                mReplyAvatar.setImageResource(R.mipmap.icon_avatar_none);
            }
            
            // Set reply username
            mReplyUsername.setText(mReplyToMessage.getUserNiceName());
            
            // Set reply content (truncated)
            String content = mReplyToMessage.getContent();
            if (!TextUtils.isEmpty(content)) {
                if (content.length() > 80) {
                    content = content.substring(0, 80) + "...";
                }
                mReplyContent.setText(content);
            } else {
                mReplyContent.setText("Message");
            }
        } else {
            mReplyContainer.setVisibility(View.GONE);
        }
    }
    
    private void setupQuickReplies() {
        if (mReplyToMessage != null) {
            mQuickReplyContainer.setVisibility(View.VISIBLE);
            
            // Animate quick replies entrance
            mQuickReplyContainer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateQuickRepliesEntrance();
                }
            }, 300);
        } else {
            mQuickReplyContainer.setVisibility(View.GONE);
        }
    }
    
    private void animateReplyContainerEntrance() {
        mReplyContainer.setAlpha(0f);
        mReplyContainer.setTranslationY(-50f);
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mReplyContainer, "alpha", 0f, 1f);
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(mReplyContainer, "translationY", -50f, 0f);
        
        alphaAnimator.setDuration(300);
        translationAnimator.setDuration(300);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        alphaAnimator.start();
        translationAnimator.start();
    }
    
    private void animateQuickRepliesEntrance() {
        mQuickReplyContainer.setAlpha(0f);
        mQuickReplyContainer.setTranslationY(30f);
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mQuickReplyContainer, "alpha", 0f, 1f);
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(mQuickReplyContainer, "translationY", 30f, 0f);
        
        alphaAnimator.setDuration(250);
        translationAnimator.setDuration(250);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        alphaAnimator.start();
        translationAnimator.start();
    }
    
    private void setupInput() {
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    mMyRadioButton.doChecked(false);
                    // Show quick replies when input is empty
                    if (mReplyToMessage != null && mQuickReplyContainer.getVisibility() != View.VISIBLE) {
                        mQuickReplyContainer.setVisibility(View.VISIBLE);
                        animateQuickRepliesEntrance();
                    }
                } else {
                    mMyRadioButton.doChecked(true);
                    // Hide quick replies when typing
                    if (mQuickReplyContainer.getVisibility() == View.VISIBLE) {
                        hideQuickReplies();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        
        Bundle bundle = getArguments();
        if (bundle != null) {
            String atName = bundle.getString(Constants.AT_NAME);
            if (!TextUtils.isEmpty(atName)) {
                mInput.setText("@" + atName + " ");
                mInput.setSelection(mInput.getText().length());
            }
        }
    }
    
    private void hideQuickReplies() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mQuickReplyContainer, "alpha", 1f, 0f);
        alphaAnimator.setDuration(150);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                mQuickReplyContainer.setVisibility(View.GONE);
            }
        });
        alphaAnimator.start();
    }
    
    private void setupHints() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        
        String danmuPrice = bundle.getString(Constants.LIVE_DANMU_PRICE);
        String coinName = bundle.getString(Constants.COIN_NAME);
        mHint1 = String.format(WordUtil.getString(com.livestreaming.common.R.string.live_open_alba), danmuPrice, coinName);
        
        if (mReplyToMessage != null) {
            mHint2 = "Reply to " + mReplyToMessage.getUserNiceName();
        } else {
            mHint2 = WordUtil.getString(com.livestreaming.common.R.string.live_say_something);
        }
        
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked) {
                    mInput.setHint(mHint1);
                } else {
                    mInput.setHint(mHint2);
                }
            }
        });
        
        mInput.setHint(mHint2);
        
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            mCheckBox.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_send) {
            sendMessage();
        } else if (id == R.id.reply_close) {
            closeReply();
        } else if (id == R.id.quick_reply_1 || id == R.id.quick_reply_2 || 
                   id == R.id.quick_reply_3 || id == R.id.quick_reply_4 || id == R.id.quick_reply_5) {
            // Handle quick reply button click
            String quickReply = "";
            if (id == R.id.quick_reply_1) quickReply = mQuickReplies[0];
            else if (id == R.id.quick_reply_2) quickReply = mQuickReplies[1];
            else if (id == R.id.quick_reply_3) quickReply = mQuickReplies[2];
            else if (id == R.id.quick_reply_4) quickReply = mQuickReplies[3];
            else if (id == R.id.quick_reply_5) quickReply = mQuickReplies[4];
            
            mInput.setText(quickReply);
            mInput.setSelection(mInput.getText().length());
            hideQuickReplies();
        }
    }
    
    private void closeReply() {
        mReplyToMessage = null;
        mReplyContainer.setVisibility(View.GONE);
        mInput.setHint(WordUtil.getString(com.livestreaming.common.R.string.live_say_something));
    }

    private void sendMessage() {
        String content = mInput.getText().toString().trim();
        if (!TextUtils.isEmpty(content)) {
            if (mCheckBox.isChecked()) {
                ((LiveActivity) mContext).sendDanmuMessage(content);
            } else {
                if (mReplyToMessage != null) {
                    ((LiveActivity) mContext).sendReplyMessage(content, mReplyToMessage);
                } else {
                    ((LiveActivity) mContext).sendChatMessage(content);
                }
            }
            mInput.setText("");
            dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imm != null) {
            imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }
} 