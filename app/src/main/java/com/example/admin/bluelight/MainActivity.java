package com.example.admin.bluelight;


import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.admin.bluelight.fragment.StartFragment;

public class MainActivity extends FragmentActivity  {
    private StartFragment startFragment;
    private BluetoothAdapter adapter;
    public static final String FRAGMENT_LIGHT = "FRAGMENT_LIGHT";
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        adapter = BluetoothAdapter.getDefaultAdapter();
//        если блют выключен, то запрос на включение
        if (!checkBTState()){

            Intent enableBtIntent = new Intent(adapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            startBTFragment();
        }

    }

    private boolean checkBTState() {

        boolean check;

        check = adapter.isEnabled();

        return check;
    }


    private void startBTFragment(){
        Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_LIGHT);


        if (fragment == null) {
            startFragment = new StartFragment();
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().add(R.id.placeFragment, startFragment, FRAGMENT_LIGHT).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//      если пользователь включил бт, загрузить фрагмент
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                startBTFragment();
            }
            else{
                finish();
            }
        }
    }
}
