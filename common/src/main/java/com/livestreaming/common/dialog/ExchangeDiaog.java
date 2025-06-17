package com.livestreaming.common.dialog;

import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.adapter.ExchangeAdapter;
import com.livestreaming.common.adapter.OnItemClickListener;
import com.livestreaming.common.bean.SelectionModel;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;

public class ExchangeDiaog extends DialogFragment implements View.OnClickListener {

    public String votes;
    TextView tv_votes;
    TextView btn_exchange;
    EditText et_tickets_amount;
    RecyclerView items_exchange_rv;
    ExchangeAdapter adapter;

    public ExchangeDiaog() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Lingver.getInstance().setLocale(requireContext(), Constants.CUR_LANGUAGE);
        return inflater.inflate(R.layout.exchange_diaog, container, false);
    }

    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_votes = view.findViewById(R.id.tv_votes);
        btn_exchange = view.findViewById(R.id.btn_exchange);
        btn_exchange.setOnClickListener(this);
        et_tickets_amount = view.findViewById(R.id.et_tickets_amount);
        votes = CommonAppConfig.getInstance().getUserBean().getVotestotal();
        tv_votes.setText(CommonAppConfig.getInstance().getUserBean().getVotestotal());
        items_exchange_rv = view.findViewById(R.id.items_exchange_rv);

        items_exchange_rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ArrayList<SelectionModel> items = new ArrayList<>();
        items.add(new SelectionModel(10000,8000));
        items.add(new SelectionModel(20000,16000));
        items.add(new SelectionModel(50000,40000));
        items.add(new SelectionModel(100000,80000));
        items.add(new SelectionModel(200000,160000));
        adapter = new ExchangeAdapter(items, new OnItemClickListener<SelectionModel>() {
            @Override
            public void notifyItemClickCallback(int position, SelectionModel model) {
                exchange(2, model);
            }
        });
        items_exchange_rv.setAdapter(adapter);
        view.findViewById(R.id.btn_close).setOnClickListener(this);
        setCancelable(false);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();
        } else if (i == R.id.btn_exchange) {
            exchange(1, null);
        }
    }

    private void exchange(int type, SelectionModel model) {
        if (type == 1) {
            String amount = et_tickets_amount.getText().toString();
            if (amount.isBlank() || amount.isEmpty()) {
                ToastUtil.show(getString(R.string.tickets_not_enogh));
                return;
            }
            try {
                if (Double.parseDouble(amount) <= Double.parseDouble(votes)) {
                    if(Double.parseDouble(amount)>=1000.0) {
                        CommonHttpUtil.exchangeTicketToCoins(amount, new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                ToastUtil.show(getString(R.string.done));
                                JSONObject obj = JSON.parseObject(info[0]);
                                UserBean bean = JSON.toJavaObject(obj, UserBean.class);
                                CommonAppConfig.getInstance().setUserBean(bean);
                                dismiss();
                            }
                        });
                    }else{
                        ToastUtil.show(getString(R.string.limit_100000_or_more));
                    }
                } else {
                    ToastUtil.show(getString(R.string.tickets_not_enogh));
                }
            } catch (Exception e) {
                ToastUtil.show(getString(R.string.tickets_not_enogh));
            }
        } else {
            try {
                if (model.value <= Double.parseDouble(votes)) {
                    CommonHttpUtil.exchangeTicketToCoins("" + model.value, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            ToastUtil.show(getString(R.string.done));
                            JSONObject obj = JSON.parseObject(info[0]);
                            UserBean bean = JSON.toJavaObject(obj, UserBean.class);
                            CommonAppConfig.getInstance().setUserBean(bean);
                            dismiss();
                        }
                    });
                } else {
                    ToastUtil.show(getString(R.string.tickets_not_enogh));
                }
            } catch (Exception e) {
                ToastUtil.show(getString(R.string.tickets_not_enogh));
            }
        }

    }
}