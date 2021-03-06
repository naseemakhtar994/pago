/*
 * Copyright (C) 2017 Vasily Styagov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.octo.bear.pago;

import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static io.octo.bear.pago.BillingServiceTestingUtils.OWNED_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.OWNED_SKU;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_SKU;
import static io.octo.bear.pago.BillingServiceTestingUtils.createBuyIntentResponseBundle;
import static io.octo.bear.pago.BillingServiceTestingUtils.createProductDetailsRequestBundle;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

/**
 * Created by shc on 21.07.16.
 *
 * Shadow {@link com.android.vending.billing.IInAppBillingService.Stub} returning
 * {@link IInAppBillingService}, that returns errors in responses.
 */
@Implements(IInAppBillingService.Stub.class)
public class ShadowFaultyIInAppBillingServiceStub {

    @SuppressWarnings("unused")
    @Implementation
    public static IInAppBillingService asInterface(android.os.IBinder obj) throws Exception {
        final IInAppBillingService service = Mockito.mock(IInAppBillingService.class);

        setupBillingSupportedResponse(service, PurchaseType.INAPP);
        setupBillingSupportedResponse(service, PurchaseType.SUBSCRIPTION);

        setupDetailsResponse(service, PurchaseType.INAPP);
        setupDetailsResponse(service, PurchaseType.SUBSCRIPTION);

        setupBuyIntentResponse(service, PurchaseType.INAPP);
        setupBuyIntentResponse(service, PurchaseType.SUBSCRIPTION);

        setupConsumptionResponse(service);

        setupPurchasedItemsResponse(service, PurchaseType.INAPP);
        setupPurchasedItemsResponse(service, PurchaseType.SUBSCRIPTION);

        return service;
    }

    private static void setupPurchasedItemsResponse(IInAppBillingService service, PurchaseType type)
            throws RemoteException, IntentSender.SendIntentException {

        Mockito.doReturn(createErrorBundle(ResponseCode.ERROR))
                .when(service)
                .getPurchases(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(type.value),
                        nullable(String.class));
    }

    private static void setupConsumptionResponse(IInAppBillingService service) throws RemoteException {
        Mockito.doReturn(1)
                .when(service)
                .consumePurchase(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(null));
    }

    private static void setupBillingSupportedResponse(IInAppBillingService service, PurchaseType type) throws RemoteException {
        Mockito.doReturn(1)
                .when(service)
                .isBillingSupported(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(type.value));
    }

    private static void setupDetailsResponse(IInAppBillingService service, PurchaseType type)
            throws RemoteException, IntentSender.SendIntentException {

        Mockito.doReturn(createErrorBundle(ResponseCode.ERROR))
                .when(service)
                .getSkuDetails(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(type.value),
                        argThat(new BundleMatcher(createProductDetailsRequestBundle(TEST_SKU))));
    }

    private static void setupBuyIntentResponse(IInAppBillingService service, PurchaseType type) throws RemoteException, IntentSender.SendIntentException {
        Mockito.doReturn(createErrorBundle(ResponseCode.ITEM_ALREADY_OWNED))
                .when(service)
                .getBuyIntent(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(OWNED_SKU),
                        eq(type.value),
                        eq(OWNED_DEVELOPER_PAYLOAD));

        Mockito.doReturn(createBuyIntentResponseBundle())
                .when(service)
                .getBuyIntent(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(TEST_SKU),
                        eq(type.value),
                        eq(TEST_DEVELOPER_PAYLOAD));
    }

    private static Bundle createErrorBundle(final ResponseCode code) throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt(RESPONSE_CODE, code.code);
        return result;
    }

}