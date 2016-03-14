package com.example.admin.bluelight.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.example.admin.bluelight.R;

/**
 * Created by dima on 08.11.15.
 */
public class SearchDevices extends DialogFragment {


    DevicesSearch instSearch;

    interface DevicesSearch {
        void search(boolean ints);
    }


//    public static SearchDevices newInstance() {
//        SearchDevices frag = new SearchDevices();
//        return frag;
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            instSearch = (DevicesSearch) getTargetFragment();
            Log.d("lol", ""+instSearch);
        } catch (ClassCastException e) {
            throw new ClassCastException("need impl");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setIcon(R.drawable.ic_cast_dark)
                .setTitle("Ничего не найдено!")
                .setMessage("повторить поиск?")
                .setNegativeButton("отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        instSearch.search(false);
                    }
                })
                .setPositiveButton("повторить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        instSearch.search(true);
                    }
                });

        return builder.create();

    }


}
