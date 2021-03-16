package com.example.ble_gatt_example.Common;

public interface CentralCallback {

    void requestEnableBLE();

    void requestLocationPermission();

    void onStatusMsg(final String message);

    void onToast(final String message);
}
