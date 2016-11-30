# HaoBle
基于RxAndroidBle封装的蓝牙扫描、连接、蓝牙连接监听、Gat读写、通知等蓝牙操作

![ble1](/screenshots/ble11.png)
## 1、搜索蓝牙列表对话框
```
 final BleScanDeviceListDialog deviceListDialog = new BleScanDeviceListDialog();
                deviceListDialog.show(getSupportFragmentManager(), MainActivity.class.getSimpleName());
                deviceListDialog.setOnRVItemClickListener(new BleScanDeviceListDialog.OnRvItemClickListener() {
                    @Override
                    public void onItemClick(BleScanDeviceListDialog.BleDevice bleDevice) {
                        deviceListDialog.dismiss();
                        Log.e("选择设备", bleDevice.toString());
                        BleHelper.create(MainActivity.this).connectionBleAndNotificationConnectState(false, bleDevice.getMac(), new Action1<RxBleConnection.RxBleConnectionState>() {
                            @Override
                            public void call(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
                                if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
                                    tvState.setText("连接成功。。。");
                                } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
                                    tvState.setText("断开。。。");
                                } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTING)) {
                                    tvState.setText("连接中。。。");
                                }
                            }
                        }, new Action1<RxBleConnection>() {
                            @Override
                            public void call(RxBleConnection rxBleConnection) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });
                    }
                });
```


## 选择设备连接并监听连接状态
```
BleHelper.create(MainActivity.this).connectionBleAndNotificationConnectState(false, bleDevice.getMac(), new Action1<RxBleConnection.RxBleConnectionState>() {
                            @Override
                            public void call(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
                                if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
                                    tvState.setText("连接成功。。。");
                                } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
                                    tvState.setText("断开。。。");
                                } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTING)) {
                                    tvState.setText("连接中。。。");
                                }
                            }
                        }, new Action1<RxBleConnection>() {
                            @Override
                            public void call(RxBleConnection rxBleConnection) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });
```

读写GAT,通知等看代码！！
