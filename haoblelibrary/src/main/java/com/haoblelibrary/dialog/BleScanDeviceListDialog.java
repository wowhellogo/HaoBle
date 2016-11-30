package com.haoblelibrary.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haoblelibrary.R;
import com.haoblelibrary.helper.BleHelper;
import com.polidea.rxandroidble.RxBleScanResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import rx.functions.Action1;

/**
 * @Package com.mk.fortpeace.app.lock.view.fragment
 * @作 用:
 * @创 建 人: 林国定 邮箱：linggoudingg@gmail.com
 * @日 期: 2016年09月14日  15:16
 */

public class BleScanDeviceListDialog extends DialogFragment implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_CODE_PERMISSION_PHOTO_PICKER = 100;
    RecyclerView mRecyclerView;
    BleDeviceAdapter mBleDeviceAdapter;
    OnRvItemClickListener mOnRVItemClickListener;
    TextView mTitleTextView;

    public interface OnRvItemClickListener {
        void onItemClick(BleDevice bleDevice);
    }

    public void setOnRVItemClickListener(OnRvItemClickListener onRVItemClickListener) {
        mOnRVItemClickListener = onRVItemClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.dialog_scan_devices, null);
        View titleView = View.inflate(getContext(), R.layout.dialog_scan_title_view, null);

        mTitleTextView = (TextView) titleView.findViewById(R.id.titleView);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mBleDeviceAdapter = new BleDeviceAdapter();
        mRecyclerView.setAdapter(mBleDeviceAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = builder.setView(view).setCustomTitle(titleView).create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {
        private List<BleDevice> mDatas;


        public BleDeviceAdapter() {
            mDatas = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_scan_devices, parent, false));

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BleDevice mode = getItem(position);
            holder.tvName.setText(isEmpty(mode.getName()));
            holder.tvRssi.setText(isEmpty("强度：" + mode.getRssi()));
            holder.tvMac.setText(isEmpty(mode.getMac()));
        }

        public BleDevice getItem(int position) {
            return mDatas.get(position);
        }


        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        /**
         * 设置全新的数据集合，如果传入null，则清空数据列表（第一次从服务器加载数据，或者下拉刷新当前界面数据表）
         *
         * @param datas
         */
        public void setDatas(List<BleDevice> datas) {
            if (datas != null) {
                mDatas = datas;
            } else {
                mDatas.clear();
            }
            notifyDataSetChanged();
        }

        public List<BleDevice> getDatas() {
            return mDatas;
        }

        /**
         * 清空数据列表
         */
        public void clear() {
            mDatas.clear();
            notifyDataSetChanged();
        }

        /**
         * 在指定位置添加数据条目
         *
         * @param position
         * @param model
         */
        public void addItem(int position, BleDevice model) {
            mDatas.add(position, model);
            notifyDataSetChanged();
        }

        /**
         * 在集合头部添加数据条目
         *
         * @param model
         */
        public void addFirstItem(BleDevice model) {
            addItem(0, model);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvRssi;
            private TextView tvName;
            private TextView tvMac;

            public ViewHolder(View itemView) {
                super(itemView);
                tvRssi = (TextView) itemView.findViewById(R.id.tv_rssi);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvMac = (TextView) itemView.findViewById(R.id.tv_mac);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mOnRVItemClickListener) {
                            mOnRVItemClickListener.onItemClick(getItem(getAdapterPosition()));
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        scanBle();
    }

    @AfterPermissionGranted(REQUEST_CODE_PERMISSION_PHOTO_PICKER)
    public void scanBle() {
        String param[] = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), param) &&
                EasyPermissions.somePermissionPermanentlyDenied(this, Arrays.asList(param))
                ) {
            BleHelper.create(getContext()).scanBleDevices(new Action1<RxBleScanResult>() {
                @Override
                public void call(RxBleScanResult rxBleScanResult) {
                    mTitleTextView.setText(R.string.str_choice_device);
                    BleDevice bleDevice = new BleDevice();
                    bleDevice.setName(rxBleScanResult.getBleDevice().getName());
                    bleDevice.setMac(rxBleScanResult.getBleDevice().getMacAddress());
                    bleDevice.setRssi(rxBleScanResult.getRssi());
                    addDevice(bleDevice);
                }
            });
        } else {
            EasyPermissions.requestPermissions(this, "授予蓝牙权限", REQUEST_CODE_PERMISSION_PHOTO_PICKER, param);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                .setTitle(getString(R.string.title_settings_dialog))
                .setPositiveButton(getString(R.string.setting))
                .setNegativeButton(getString(R.string.cancel), null /* click listener */)
                .build()
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BleHelper.create(getContext()).unScanSubscription();
    }

    private String isEmpty(String str) {
        return str != null ? str : "";
    }

    public void addDevice(BleDevice bleDevice) {
        if (mBleDeviceAdapter.getDatas() != null && mBleDeviceAdapter.getDatas().size() > 0) {
            if (mBleDeviceAdapter.getDatas().contains(bleDevice)) {
                int index = mBleDeviceAdapter.getDatas().indexOf(bleDevice);
                mBleDeviceAdapter.getDatas().set(index, bleDevice);
                mBleDeviceAdapter.notifyDataSetChanged();
            } else {
                mBleDeviceAdapter.addFirstItem(bleDevice);
            }
        } else {
            mBleDeviceAdapter.addFirstItem(bleDevice);
        }
    }


    public class BleDevice implements Serializable {
        private String name;
        private String mac;
        private int rssi;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            BleDevice bleDevice = (BleDevice) o;
            return mac.equals(bleDevice.mac);
        }

        @Override
        public int hashCode() {
            return mac.hashCode();
        }

        @Override
        public String toString() {
            return "BleDevice{" +
                    "name='" + name + '\'' +
                    ", mac='" + mac + '\'' +
                    ", rssi='" + rssi + '\'' +
                    '}';
        }
    }


}
