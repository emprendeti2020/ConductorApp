package com.creativeapps.schoolbusdriver.ui.activity.main.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;

import androidx.fragment.app.Fragment;


public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).showHideProgressBar(false);
    }
}
