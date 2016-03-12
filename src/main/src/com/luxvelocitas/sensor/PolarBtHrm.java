package com.luxvelocitas.sensor;

import org.slf4j.Logger;

import com.aau.bluetooth.BluetoothClient;
import com.aau.bluetooth.BluetoothException;
import com.aau.bluetooth.IBluetooth;
import com.aau.bluetooth.IBluetoothDevice;
import com.aau.bluetooth.IBluetoothReadyStateListener;
import com.aau.bluetooth.data.IDataListener;
import com.aau.bluetooth.data.SimpleDataListener;
import com.luxvelocitas.datautils.DataBundle;
import com.luxvelocitas.tinyevent.SimpleTinyEventDispatcher;


/**
 * Interface to a Polar Bluetooth heart rate monitor
 *
 * @author Konrad Markus <konker@luxvelocitas.com>
 *
 */
public class PolarBtHrm extends SimpleTinyEventDispatcher<PolarBtHrmEventType, DataBundle> {
    public static final int NO_HEART_RATE = -1;
    protected static final int DEFAULT_NUM_RETRIES = 3;
    protected static final int DEFAULT_RETRY_INTERVAL_MS = 3000;

    protected int mHeartRate;
    protected int mBatteryLevel;
    protected int mSeconds;

    protected Logger mLogger;
    protected IBluetooth mBluetooth;
    protected String mBluetoothName;
    protected BluetoothClient mBluetoothClient;
    protected final IDataListener mDataListener;

    public PolarBtHrm(Logger logger, IBluetooth bluetooth, String bluetoothName) {
        mHeartRate = NO_HEART_RATE;
        mBatteryLevel = 0;
        mSeconds = 0;

        mLogger = logger;
        mBluetooth = bluetooth;
        mBluetoothName = bluetoothName;

        mDataListener = new SimpleDataListener() {
            @Override
            public void onData(BluetoothClient connection, final byte[] data, final int len) {
                //[TODO: handle data]
                mLogger.info("Got data: " + len + ": " + data);
            }
        };
    }

    public void connect(IBluetoothReadyStateListener bluetoothReadyStateListener) {
        // Find the device from already paired devices
        IBluetoothDevice device = mBluetooth.getPairedDeviceByName(mBluetoothName);

        if (device == null) {
            mLogger.error("Could not find paired device: " + mBluetoothName);
            bluetoothReadyStateListener.onError(null, new BluetoothException("Could not find paired device: " + mBluetoothName));
            return;
        }
        mBluetoothClient = new BluetoothClient(mLogger,
                                               device,
                                               IBluetooth.RFCOMM_UUID,
                                               DEFAULT_NUM_RETRIES,
                                               DEFAULT_RETRY_INTERVAL_MS);

        // Add listeners to the connection
        mBluetoothClient
            .addReadyStateListener(bluetoothReadyStateListener)
            .addDataListener(mDataListener)
            .connect();
    }

    public void close() {
        if (mBluetoothClient != null) {
            mBluetoothClient.close();
        }
    }

    public int getHeartRate() {
        return mHeartRate;
    }
    public void setHeartRate(int heartRate) {
        this.mHeartRate = heartRate;
    }
    public int getBatteryLevel() {
        return mBatteryLevel;
    }
    public void setBatteryLevel(int batteryLevel) {
        this.mBatteryLevel = batteryLevel;
    }
    public int getSeconds() {
        return mSeconds;
    }
    public void setSeconds(int seconds) {
        this.mSeconds = seconds;
    }
}
