package com.example.ttn_compass2.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.ttn_compass2.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    ListView lv1;

    ListView lv4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView= inflater.inflate(R.layout.fragment_settings, container, false);
        lv1 = (ListView)mView.findViewById(R.id.listview1);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(getActivity(),
                R.array.ttn_gateway, android.R.layout.simple_list_item_1);
        lv1.setAdapter(adapter1);


        return mView;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ToolbarInterface toolbarCallback = (ToolbarInterface) getActivity();
    }



}