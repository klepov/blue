package com.example.admin.bluelight.fragment;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.admin.bluelight.R;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.tankery.lib.circularseekbar.CircularSeekBar;

/**
 * Created by admin on 24/10/15.
 */
public class StartFragment extends Fragment implements CircularSeekBar.OnCircularSeekBarChangeListener,SearchDevices.DevicesSearch{


    private static final String TAG = "OK";
    private static final String NAME_BT_DEVICES = "HC-05";
    public static final String TAG_DIALOG_FRAGMENT = "start";
    private static String address = null;
    int stateLight;

    private Boolean isOn = true;


    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private CircularSeekBar seekBar;

    private OutputStream outStream = null;

    private List<BluetoothDevice> mArrayAdapter = new ArrayList();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start, container, false);

        seekBar = (CircularSeekBar) v.findViewById(R.id.seekChangeVol);
        seekBar.setMax(255);
        seekBar.setCircleStrokeWidth(50);
        seekBar.setCircleFillColor(getActivity().getResources().getColor(R.color.circleDEF));



        seekBar.setOnSeekBarChangeListener(this);

        seekBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.seekChangeVol){
                if (isOn){
                    stateLight = 0;
                    sendData(stateLight);
                    isOn = false;
                }else{
                    stateLight = 255;
                    sendData(stateLight);
                    isOn = true;
                }
            }
            }
        });
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        adapter = BluetoothAdapter.getDefaultAdapter();

        address = adapter.getAddress();

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
// если устройств подключнеия нет то запустить их поиск
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if (device.getName().equals(NAME_BT_DEVICES)) {

//                    если устройство есть, то запустить коннект

                    mArrayAdapter.add(device);

                    connectToDevices();
                }
            }
        }

        if (mArrayAdapter.size() == 0){
            showDialog(new SearchDevices());
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        поиск устройства
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.d("startSCAN","SCAN");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d("FoundDEV ",device.getName());
                if (device.getName().equals(NAME_BT_DEVICES)){
                    mArrayAdapter.add(device);

//                    как только найдено устройство, убить фрагмнет и запустить коннект
                    DialogFragment fragment = (DialogFragment) getFragmentManager()
                            .findFragmentByTag(TAG_DIALOG_FRAGMENT);
                    fragment.dismiss();

                    connectToDevices();
                }
            }
        }
    };

    private void errorExit(String s, String s1) {
        Toast.makeText(getActivity(), s1, Toast.LENGTH_LONG).show();
        getActivity().finish();
    }



    private void startScan(){
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getActivity().registerReceiver(mReceiver, filter);
        adapter.startDiscovery();

        showDialog(new FindDevices());

    }

    private void showDialog(DialogFragment fragment){

        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), "start");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)

    private void connectToDevices(){
        try {
            socket = mArrayAdapter.get(0).createRfcommSocketToServiceRecord(MY_UUID);

            adapter.cancelDiscovery();

            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        try {
            outStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData(int vol) {


        try {
            outStream.write(vol);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nВ переменной address у вас прописан 00:00:00:00:00:00, вам необходимо прописать реальный MAC-адрес Bluetooth модуля";
            msg = msg + ".\n\nПроверьте поддержку SPP UUID: " + MY_UUID.toString() + " на Bluetooth модуле, к которому вы подключаетесь.\n\n";

            errorExit("Fatal Error", msg);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.seekChangeVol:
//                Log.d("on","ok");
//
//        }
//    }


    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
        Log.d("val", "" + (int) circularSeekBar.getProgress());
        stateLight = (int) circularSeekBar.getProgress();
        sendData(stateLight);
        if (stateLight > 0){
            seekBar.setCircleFillColor(getActivity().getResources().getColor(R.color.circleON));
        }
        else {
            seekBar.setCircleFillColor(getActivity().getResources().getColor(R.color.circleOFF));

        }
    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {

    }

//    ответ пользователя
    @Override
    public void search(boolean ints) {
        if (ints){
            startScan();
        }else {
            getActivity().finish();
        }
    }
}
