package com.livestreaming.common.pay;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.R;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public class GooglePayPresenter {
    private static GooglePayPresenter instance;
    public ProductDetails selectedProduct;

    private BillingClient billingClient;
    PurchasesUpdatedListener purchasesUpdatedListener;

    public static GooglePayPresenter getInstance() {
        if (instance == null) {
            instance = new GooglePayPresenter();
        }
        return instance;
    }
    private void releasePaymentClient(){
        if (billingClient != null) {
            billingClient.endConnection();
        }
        billingClient = null;
    }
    private GooglePayPresenter() {

    }

    public void initClient() {

        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    for (Purchase purchase : purchases) {

                        ConsumeParams consumeParams =
                                ConsumeParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();

                        ConsumeResponseListener listener = new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                call_api_to_update_user(purchaseToken, false);
                            }
                        };

                        billingClient.consumeAsync(consumeParams, listener);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    queryAndConsumeUnconsumedPurchases();

                }
            }

        };
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(CommonAppContext.getInstance())
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build();
        }
    }

    public void startConnection(AbsActivity activity,Map<String, String> orderParams) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    QueryProductDetailsParams queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                    .setProductList(
                                            ImmutableList.of(
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId("pid0001")
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId("pid0002")
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId("pid0003")
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId("pid0004")
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId("pid0005")
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build()
                                            ))
                                    .build();

                    billingClient.queryProductDetailsAsync(
                            queryProductDetailsParams,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                                                     @NonNull List<ProductDetails> productDetailsList) {
                                    // check billingResult
                                    // process returned productDetailsList
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                                        for (int i = 0; i < productDetailsList.size(); i++) {
                                            Log.e("test", productDetailsList.get(i).getName() + ", " + orderParams.get("coin"));
                                            if (productDetailsList.get(i).getName().equals(orderParams.get("coin"))) {
                                                selectedProduct = productDetailsList.get(i);

                                                break;
                                            }
                                        }
                                        if (selectedProduct != null) {
                                            ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                                                    ImmutableList.of(
                                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                                    .setProductDetails(selectedProduct)
                                                                    .build()
                                                    );
                                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                    .setProductDetailsParamsList(productDetailsParamsList)
                                                    .build();
                                            billingClient.launchBillingFlow(activity, billingFlowParams);
                                        }
                                    } else {
                                        ToastUtil.show("Couldn't get product list " + billingResult.getResponseCode());
                                    }
                                }
                            }
                    );
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                int x;
            }
        });
    }

    private void queryAndConsumeUnconsumedPurchases() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchasesList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchasesList != null) {
                    for (Purchase purchase : purchasesList) {
                        // Check if the item the user tried to buy is unconsumed
                        if (!purchase.isAcknowledged()) {
                            ConsumeParams consumeParams =
                                    ConsumeParams.newBuilder()
                                            .setPurchaseToken(purchase.getPurchaseToken())
                                            .build();
                            ConsumeResponseListener listener = new ConsumeResponseListener() {
                                @Override
                                public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                    call_api_to_update_user(purchaseToken, true);
                                }
                            };
                            billingClient.consumeAsync(consumeParams, listener);
                        }
                    }
                } else {
                    Log.e("Billing", "Error querying purchases: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    private void call_api_to_update_user(String purchaseToken, boolean isExistsBefore) {
        if (selectedProduct != null) {
            CommonHttpUtil.checkGooglePayPayment(purchaseToken, selectedProduct, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (!isExistsBefore) {
                        ToastUtil.show(CommonAppContext.getInstance().getString(R.string.pay_succ));
                    }
                    releasePaymentClient();
                }
            });
        }

    }

}
