package com.creativeapps.schoolbusdriver.ui.activity.main.contacts;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ParentsContactsFragment extends Fragment implements SearchView.OnQueryTextListener, ParentsAdapter.ParentsAdapterListener {
    //adapter used with recyclerView used to display name and telephone numbers of parents
    private ParentsAdapter mAdapter;
    //search view to filter parents either with their phone numbers or names
    private SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        //inflate the layout
        View view = inflater.inflate(R.layout.fragment_parent_contacts, container, false);
        //get the last saved driver information from the SharedPreference
        Driver driver = Util.getSavedObjectFromPreference(this.getActivity().getApplicationContext(),
                "mPreference", "Driver", Driver.class);
        //define a parent list
        List<Parent> parentList = new ArrayList<>();
        if (driver.getParents() != null) {
            //set the parent list with the parents in the driver object
            parentList = driver.getParents();
        }
        //get the adapter
        mAdapter = new ParentsAdapter(parentList, this);

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        //define the recyclerView that used to display the names and phone numbers of parents
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //define the search view
        mSearchView = view.findViewById(R.id.search);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.clearFocus();



        mSearchView.setIconifiedByDefault(true);
        mSearchView.setFocusable(false);
        mSearchView.setIconified(true);
        //mSearchView.requestFocusFromTouch();

        //check the permission for making phone calls when creating this fragment
        if (ActivityCompat.checkSelfPermission(ParentsContactsFragment.this.getActivity(), Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            //if not, request a phone call permission
            ActivityCompat.requestPermissions(ParentsContactsFragment.this.getActivity(),
                    new String[]{Manifest.permission.CALL_PHONE}, MainActivity.PERMISSIONS_REQUEST_CALL);
        }
        ((MainActivity) getActivity()).showHideProgressBar(false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //clear the search view after click search
        mSearchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //filter the displayed parent list based on the entered text in search view
        mAdapter.filter(newText);
        return true;
    }

    @Override
    public void onParentSelected(final Parent parent) {
        //handle the click event on a parent in the displayed list

        //display an alert dialog to warn the user that he is about to make phone call. If the user
        // chooses yes, a call will be placed
        new AlertDialog.Builder(this.getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.call_parent_alert_title))
                .setMessage(getString(R.string.are_you_sure_call_parent))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //define the call intent
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        //set the telephone number
                        callIntent.setData(Uri.parse("tel:" + parent.getTel_number()));
                        //if the permission is not given by the user, display a message and finish
                        if (ActivityCompat.checkSelfPermission(ParentsContactsFragment.this.getActivity(), Manifest.permission.CALL_PHONE) !=
                                PackageManager.PERMISSION_GRANTED) {
                            Util.displayExitMessage(getString(R.string.go_setting_enable_call),
                                    ParentsContactsFragment.this.getActivity(), false);
                            return;
                        }
                        //if the permission is given by the user, place the call
                        ParentsContactsFragment.this.getActivity().startActivity(callIntent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}