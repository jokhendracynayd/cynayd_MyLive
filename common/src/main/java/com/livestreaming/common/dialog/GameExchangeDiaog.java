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

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.utils.ToastUtil;
import com.yariksoffice.lingver.Lingver;

public class GameExchangeDiaog extends DialogFragment implements View.OnClickListener {

    public String votes;
    TextView tv_votes;
    TextView btn_exchange;
    EditText et_tickets_amount;
    TextView gameCoins;
    public OnGameCoins onGameCoins;

    public GameExchangeDiaog() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Lingver.getInstance().setLocale(requireContext(), Constants.CUR_LANGUAGE);
        return inflater.inflate(R.layout.view_game_exchange, container, false);
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
        View g=view.findViewById(R.id.game_exchange_layout);
        tv_votes = view.findViewById(R.id.tv_currency);
        btn_exchange = g.findViewById(R.id.tv_recharge_confirm);
        btn_exchange.setOnClickListener(this);
        et_tickets_amount =g.findViewById(R.id.et_exchange_money);
        gameCoins=((TextView)g.findViewById(R.id.tv_exchange_diamonds_coins));
        et_tickets_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int coins=Integer.parseInt(s.toString());
                    gameCoins.setText(getString(R.string._game_currency)+" : "+(coins));
                }catch (Exception e){

                }
            }
        });
        votes = CommonAppConfig.getInstance().getUserBean().getCoin();
        tv_votes.setText(getString(R.string.coin_balance) + " : " + CommonAppConfig.getInstance().getUserBean().getCoin());
      view.findViewById(R.id.tv_recharge_cancel).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              dismiss();
          }
      });
        setCancelable(false);
    }



    @Override
    public void onClick(View v) {
        int i = v.getId();
         if (i == R.id.tv_recharge_confirm) {

             String amount = et_tickets_amount.getText().toString();
             if (amount.isBlank() || amount.isEmpty()) {
                 ToastUtil.show(getString(R.string.tickets_not_enogh));
                 return;
             }
             try {
                 if (Double.parseDouble(amount) <= Double.parseDouble(votes)) {
                     onGameCoins.exchange(Double.parseDouble(amount));
                     dismiss();
                 }
             }catch (Exception e){

             }
        }
    }

    public interface OnGameCoins{
        public default void exchange(double amount){

        }
        public default void withdraw(double amount){

        }
    }
}