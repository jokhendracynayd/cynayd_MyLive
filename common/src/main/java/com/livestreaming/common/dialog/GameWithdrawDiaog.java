package com.livestreaming.common.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.utils.ToastUtil;
import com.yariksoffice.lingver.Lingver;

public class GameWithdrawDiaog extends DialogFragment  {

    public double gameConis,widthow_coins;
    TextView game_cons;
    EditText et_withdrow_amount;


    public GameExchangeDiaog.OnGameCoins onGameCoins;

    public GameWithdrawDiaog() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Lingver.getInstance().setLocale(requireContext(), Constants.CUR_LANGUAGE);
        return inflater.inflate(R.layout.view_game_withdrow, container, false);
    }

    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(R.style.animCenter);
        window.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.layer_white_solid_main_color_stroke_20dp_corner));
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        game_cons = view.findViewById(R.id.tv_game_coins);
        game_cons.setText(gameConis+" "+getString(R.string._game_currency));
        view.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameConis>=widthow_coins) {
                    onGameCoins.withdraw(widthow_coins);
                    dismiss();
                }else{
                    ToastUtil.show(R.string.live_coin_not_enough);
                }
            }
        });

        et_withdrow_amount =view.findViewById(R.id.et_withdrow_amount);
        et_withdrow_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    widthow_coins=Double.parseDouble(s.toString());

                }catch (Exception e){

                }
            }
        });
      view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              dismiss();
          }
      });
        setCancelable(false);
    }
}