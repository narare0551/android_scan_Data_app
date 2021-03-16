package com.example.ble_gatt_example.Common;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.example.ble_gatt_example.Common.Constants.CHARACTERISTIC_UUID;
import static com.example.ble_gatt_example.Common.Constants.CONFIG_UUID;
import static com.example.ble_gatt_example.Common.Constants.SCAN_PERIOD;
import static com.example.ble_gatt_example.Common.Constants.SERVICE_UUID;

public class BleGattServer {
    protected static volatile BleGattServer sInstance = null;
    private BluetoothManager b_manager;
    private BluetoothAdapter b_adapter;
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private boolean isScanning = false;
    private boolean isConnect = false;
    private Map<String, BluetoothDevice> scanResults;
    private ScanCallback scanCallback;
    private BluetoothLeScanner bleScanner;
    private CentralCallback listener;
    private Handler scanHandler;
    public Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋

    public BleGattServer(Context context){
        this.context = context;
        initBle();
    }

    public void setCallBack(CentralCallback listener){this.listener = listener;}

    public static BleGattServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BleGattServer(context);
        }
        return sInstance;
    }

    public void initBle(){
        b_manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        b_adapter = b_manager.getAdapter();
    }
    /**
     * Start BLE scan
     */
    public void startScan() {
        /**
         * 위치 권한이 있는지 체크한다.
         * 없다면 콜백으로 requestLocationPermission() 호출한다.
         */

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.requestLocationPermission();
            listener.onStatusMsg("Scanning Failed: no fine location permission");
            return;
        }

        /**
         * 이미 연결된 상태라면 더이상 진행시키지 않는다.
         */
        if (isConnect) {
            return;
        }

        listener.onStatusMsg("Scanning...");
        /**
         * 블루투스를 사용할 수 있는 상태인지 체크한다.
         */
        if (b_adapter == null || !b_adapter.isEnabled()) {
            listener.requestEnableBLE();
            listener.onStatusMsg("Scanning Failed: ble not enabled");
            return;
        }

        bleScanner = b_adapter.getBluetoothLeScanner();

        /**
         * 이미 Gatt Server 와 연결된 상태일 수 있으니 호출해준다.
         */
        disconnectGattServer();

        //// set scan filters
        // create scan filter list
        List<ScanFilter> filters = new ArrayList<>();
        // create a scan filter with device uuid
        ScanFilter scan_filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();

        // add the filter to the list
        filters.add(scan_filter);
        //// scan settings
        // set low power scan mode
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        scanResults = new HashMap<>();
        scanCallback = new BLEScanCallback(scanResults);
        System.out.println("!!!!!!!!!!!!!!!!!!!!");
        System.out.println(scanCallback);
        //// now ready to scan
        // start scan
        bleScanner.startScan(filters, settings, scanCallback);
        // set scanning flag
        isScanning = true;

        scanHandler = new Handler();
        scanHandler.postDelayed(this::stopScan, SCAN_PERIOD);
    }

    /**
     * Stop scanning
     */
    private void stopScan() {
        // check pre-conditions
        if (isScanning && b_adapter != null && b_adapter.isEnabled() && bleScanner != null) {
            // stop scanning
            bleScanner.stopScan(scanCallback);
            scanComplete();
        }
        // reset flags
        if (scanCallback != null)
            scanCallback = null;
        if (scanHandler != null)
            scanHandler = null;
        isScanning = false;
        // update the status


        listener.onStatusMsg("scanning stopped");
    }

    /**
     * Handle scan results after scan stopped
     */
    private void scanComplete() {
        // check if nothing found
        if (scanResults.isEmpty()) {
            listener.onStatusMsg("scan results is empty");
            return;
        }
        System.out.println(scanResults);
        // loop over the scan results and connect to them
        for (String device_addr : scanResults.keySet()) {
            // get device instance using its MAC address
            BluetoothDevice device = scanResults.get(device_addr);
//            if (MAC_ADDR.equals(device_addr)) {
            // connect to the device
            connectDevice(device);
//            }
        }
    }

    /**
     * Connect to the ble device
     * @param _device
     */
    private void connectDevice(BluetoothDevice _device) {
        // update the status
        listener.onStatusMsg("Connecting to " + _device.getAddress());
        GattClientCallback gatt_client_cb = new GattClientCallback();
        bluetoothGatt = _device.connectGatt(context, false, gatt_client_cb);
    }

    /**
     * Disconnect Gatt Server
     */
    public void disconnectGattServer() {
        listener.onStatusMsg("Closing Gatt connection");
        // reset the connection flag
        isConnect = false;
        // disconnect and close the gatt
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    /**
     * Gatt Client Callback class
     */
    private class GattClientCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt _gatt, int _status, int _new_state) {
            super.onConnectionStateChange(_gatt, _status, _new_state);
            if (_status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer();
                return;
            } else if (_status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer();
                return;
            }
            if (_new_state == BluetoothProfile.STATE_CONNECTED) {
                // update the connection status message
                listener.onStatusMsg("Connected");
                // set the connection flag
                isConnect = true;
                _gatt.discoverServices();
            } else if (_new_state == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt _gatt, int _status) {
            super.onServicesDiscovered(_gatt, _status);
            // check if the discovery failed
            if (_status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            // find discovered characteristics
            List<BluetoothGattCharacteristic> matching_characteristics = BluetoothUtils.findBLECharacteristics(_gatt);
            for (BluetoothGattCharacteristic characteristic : matching_characteristics) {
            }

            if (matching_characteristics.isEmpty()) {
                return;
            }

            // log for successful discovery

            // Set CharacteristicNotification
            BluetoothGattCharacteristic cmd_characteristic = BluetoothUtils.findCharacteristic(bluetoothGatt, CHARACTERISTIC_UUID);
            _gatt.setCharacteristicNotification(cmd_characteristic, true);
            // 리시버 설정
            BluetoothGattDescriptor descriptor = cmd_characteristic.getDescriptor(UUID.fromString(CONFIG_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = _gatt.writeDescriptor(descriptor);
            if (success) {
            } else {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic) {
            super.onCharacteristicChanged(_gatt, _characteristic);

            readCharacteristic(_characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic, int _status) {
            super.onCharacteristicWrite(_gatt, _characteristic, _status);
            if (_status == BluetoothGatt.GATT_SUCCESS) {

            } else {
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacteristic(characteristic);
            } else {
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }

        /**
         * Log the value of the characteristic
         * @param _characteristic
         */
        private void readCharacteristic(BluetoothGattCharacteristic _characteristic) {
            byte[] msg = _characteristic.getValue();
            String message = new String(msg);
            listener.onStatusMsg("read : " + message);
            listener.onToast("read : " + message);
        }
    }

    /**
     * BLE Scan Callback class
     */
    private class BLEScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> cb_scan_results;

        /**
         * Constructor
         * @param _scan_results
         */
        BLEScanCallback(Map<String, BluetoothDevice> _scan_results) {
            cb_scan_results = _scan_results;
        }

        @Override
        public void onScanResult(int _callback_type, ScanResult _result) {
            addScanResult(_result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> _results) {
            for (ScanResult result : _results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int _error) {

        }

        /**
         * Add scan result
         * @param _result
         */
        private void addScanResult(ScanResult _result) {

            // get scanned device
            BluetoothDevice device = _result.getDevice();
            // get scanned device MAC address
            String device_address = device.getAddress();
            // add the device to the result list
            cb_scan_results.put(device_address, device);
            // log
            listener.onStatusMsg("scan results device: " + device_address + ", " + device.getName());
        }
    }
}
