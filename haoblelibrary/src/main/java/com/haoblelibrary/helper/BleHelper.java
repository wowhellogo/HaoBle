package com.haoblelibrary.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.haoblelibrary.R;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleScanResult;
import com.polidea.rxandroidble.exceptions.BleScanException;

import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @Package com.haoblelibrary.builder
 * @作 用:基于RxAndroidBle封装的蓝牙扫描、连接、蓝牙连接监听、Gat读写、通知等蓝牙操作
 * @创 建 人: 林国定 邮箱：linggoudingg@gmail.com
 * @日 期: 2016年11月30日  10:52
 */

public class BleHelper {

    private static final String TAG = BleHelper.class.getSimpleName();
    private static Context mContext;
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private final RxBleClient mRxBleClient;
    private Subscription mScanSubscription;//搜索订阅者
    private Subscription mConnectionSubscription;//连接订阅者
    private Subscription mObserveConnectionStateChanges;//蓝牙连接状态订阅者

    private Subscription readSubscription;//读特征订阅者
    private Subscription writeSubscription;//写特征订阅者
    private Subscription notificationSubscription;//通知订阅者

    private static class NestInstance {
        private static BleHelper sMBleHelper;

        static {
            sMBleHelper = new BleHelper();
        }
    }

    private BleHelper() {
        mRxBleClient = RxBleClient.create(mContext);
    }

    public static BleHelper create(Context context) {
        if (initialize(context)) {
            return NestInstance.sMBleHelper;
        } else {
            throw new IllegalArgumentException(mContext.getString(R.string.error_don_not_support_bluetooth));
        }
    }


    /**
     * 销毁
     */
    public void clear() {
        mContext = null;
        mBluetoothManager = null;
        mBluetoothAdapter = null;
    }


    public BleHelper unScanSubscription() {
        if (mScanSubscription != null) mScanSubscription.unsubscribe();
        return NestInstance.sMBleHelper;
    }


    /**
     * 蓝牙扫描
     *
     * @param action1            订阅事件源，处理蓝牙扫描成功的回调
     * @param filterServiceUUIDs 根据serviceUUIDs过滤蓝牙扫描
     */
    public BleHelper scanBleDevices(Action1<RxBleScanResult> action1, UUID... filterServiceUUIDs) {
        mScanSubscription = mRxBleClient.scanBleDevices(filterServiceUUIDs)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        clear();
                    }
                })
                .subscribe(action1, this::onScanFailure);
        return NestInstance.sMBleHelper;
    }

    /**
     * 监听蓝牙连接状态
     *
     * @param macAddress 蓝牙地址
     * @param action1    蓝牙状态回调
     */
    /*-------------------------------------------------------------------------------------------------------*/
    /*---------------------------------蓝牙连接监听----------------------------------------------------*/
    public BleHelper observeConnectionStateChanges(String macAddress, Action1<RxBleConnection.RxBleConnectionState> action1) {
        unObserveConnectionStateChanges();
        mObserveConnectionStateChanges = mRxBleClient.getBleDevice(macAddress)
                .observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1);
        return NestInstance.sMBleHelper;
    }

    public void unObserveConnectionStateChanges() {
        if (mObserveConnectionStateChanges != null) mObserveConnectionStateChanges.unsubscribe();
    }


    /**
     * 蓝牙连接操作
     *
     * @param context     上下文
     * @param autoConnect 是否自动连接
     * @param macAddress  蓝牙连接地址
     * @param action1     连接成功回调
     * @param error
     */
    public BleHelper connectionBle(Context context, boolean autoConnect, String macAddress, Action1<RxBleConnection> action1, Action1<Throwable> error) {
        mConnectionSubscription = mRxBleClient.getBleDevice(macAddress)
                .establishConnection(context, autoConnect)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::clearconnectionSubscription)
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }

    /**
     * 蓝牙连接操作并监听连接状态
     *
     * @param autoConnect 是否自动连接
     * @param macAddress  蓝牙连接地址
     * @param action0     连接状态
     * @param action1     连接成功回调
     * @param error
     */
    public BleHelper connectionBleAndNotificationConnectState(boolean autoConnect, String macAddress, Action1<RxBleConnection.RxBleConnectionState> action0, Action1<RxBleConnection> action1, Action1<Throwable> error) {
        mConnectionSubscription = mRxBleClient.getBleDevice(macAddress)
                .establishConnection(mContext, autoConnect)
                .doOnSubscribe(() -> observeConnectionStateChanges(macAddress, action0))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::clearconnectionSubscription)
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }


    /*----------------------------------------------读/写 通知特征---------------------------------------------------------*/

    /**
     * 读特性
     *
     * @param rxBleConnection    蓝牙连接处理
     * @param characteristicUUID 特征UUID
     * @param action1            读成功回调
     * @param error              错误回调
     */
    public BleHelper readCharacteristic(RxBleConnection rxBleConnection, String characteristicUUID, Action1<byte[]> action1, Action1<Throwable> error) {
        readSubscription = rxBleConnection.readCharacteristic(UUID.fromString(characteristicUUID))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }

    /**
     * 写特征
     *
     * @param rxBleConnection    蓝牙连接处理
     * @param characteristicUUID 特征UUID
     * @param bytes              写入字节
     * @param action1            写成功回调
     * @param error              错误回调
     */
    public BleHelper writeCharacteristic(RxBleConnection rxBleConnection, String characteristicUUID, byte[] bytes, Action1<byte[]> action1, Action1<Throwable> error) {
        writeSubscription = rxBleConnection.writeCharacteristic(UUID.fromString(characteristicUUID), bytes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }


    /**
     * 通知
     *
     * @param rxBleConnection
     * @param characteristicUUID
     * @param action1
     * @param error
     */
    public BleHelper setupNotification(RxBleConnection rxBleConnection, String characteristicUUID, Action1<byte[]> action1, Action1<Throwable> error) {
        notificationSubscription = rxBleConnection.setupNotification(UUID.fromString(characteristicUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }


    /**
     * 连接并读特性
     *
     * @param context
     * @param macAddress         mac地址
     * @param characteristicUUID 特性UUID
     * @param action1            回调
     * @param error              错误回调
     */
    public BleHelper connectionAndReadCharacteristic(Context context, String macAddress, String characteristicUUID, Action1<byte[]> action1, Action1<Throwable> error) {
        readSubscription = mRxBleClient.getBleDevice(macAddress)
                .establishConnection(context, false)
                .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(UUID.fromString(characteristicUUID)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }

    /**
     * 连接并写入特性
     *
     * @param context
     * @param macAddress         mac地址
     * @param characteristicUUID 特征UUID
     * @param bytes              写入的字节数组
     * @param action1            回调
     * @param error              错误回调
     */
    public BleHelper connectionAndWriteCharacteristic(Context context, String macAddress, String characteristicUUID, byte[] bytes, Action1<byte[]> action1, Action1<Throwable> error) {
        writeSubscription = mRxBleClient.getBleDevice(macAddress)
                .establishConnection(context, false)
                .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(characteristicUUID), bytes))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }


    /**
     * 连接并接收蓝牙设备通知
     *
     * @param context
     * @param macAddress         mac地址
     * @param characteristicUUID 特征UUID
     * @param action1            成功回调
     * @param error              错误回调
     */
    public BleHelper connectionAndSetupNotification(Context context, String macAddress, String characteristicUUID, Action1<byte[]> action1, Action1<Throwable> error) {
        notificationSubscription = mRxBleClient.getBleDevice(macAddress)
                .establishConnection(context, false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(UUID.fromString(characteristicUUID)))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, error);
        return NestInstance.sMBleHelper;
    }

    public BleHelper writeCharacteristicAndReadCharacteristic(RxBleConnection rxBleConnection, String characteristicUUID, Action1<byte[]> action1, Action1<Throwable> error) {
        byte b[] = new byte[8];
        rxBleConnection.writeCharacteristic(UUID.fromString(characteristicUUID), b).doOnNext(new Action1<byte[]>() {
            @Override
            public void call(byte[] bytes) {
                //写入回调
            }
        }).flatMap(new Func1<byte[], Observable<byte[]>>() {
            @Override
            public Observable<byte[]> call(byte[] bytes) {
                return rxBleConnection.readCharacteristic(UUID.fromString(characteristicUUID));
            }
        }).subscribe(new Action1<byte[]>() {
            @Override
            public void call(byte[] bytes) {

            }
        });
        return NestInstance.sMBleHelper;
    }


    public BleHelper unNotificationSubscription() {
        if (notificationSubscription != null) notificationSubscription.unsubscribe();
        return NestInstance.sMBleHelper;

    }

    public BleHelper unReadSubscription() {
        if (readSubscription != null) readSubscription.unsubscribe();
        return NestInstance.sMBleHelper;
    }

    public BleHelper unWriteSubscription() {
        if (writeSubscription != null) writeSubscription.unsubscribe();
        return NestInstance.sMBleHelper;
    }


    /**
     * 扫描失败信息
     */
    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(mContext, mContext.getString(R.string.error_bluetooth_not_available), Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(mContext, mContext.getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(mContext, mContext.getString(R.string.error_location_permission_missing), Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(mContext, mContext.getString(R.string.error_location_services_disabled), Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(mContext, mContext.getString(R.string.error_unable_to_start_scanning), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void clearconnectionSubscription() {
        mConnectionSubscription = null;
    }


    /**
     * 判断设备是否支持蓝牙
     *
     * @param context
     * @return
     */
    private static boolean initialize(Context context) {
        mContext = context;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to initialize BluetoothAdapter.");
            return false;
        }
        return true;
    }


}
