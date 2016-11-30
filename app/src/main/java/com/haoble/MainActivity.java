package com.haoble;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.haoblelibrary.helper.BleHelper;
import com.haoblelibrary.dialog.BleScanDeviceListDialog;
import com.polidea.rxandroidble.RxBleConnection;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {
    private TextView tvState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvState=(TextView)findViewById(R.id.tvState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final BleScanDeviceListDialog deviceListDialog = new BleScanDeviceListDialog();
                deviceListDialog.show(getSupportFragmentManager(), "sss");
                deviceListDialog.setOnRVItemClickListener(new BleScanDeviceListDialog.OnRvItemClickListener() {
                    @Override
                    public void onItemClick(BleScanDeviceListDialog.BleDevice bleDevice) {
                        deviceListDialog.dismiss();
                        Log.e("lgd", bleDevice.toString());
                        BleHelper.create(MainActivity.this).connectionBleAndNotificationConnectState(true, bleDevice.getMac(), new Action1<RxBleConnection.RxBleConnectionState>() {
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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
