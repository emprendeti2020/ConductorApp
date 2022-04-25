package com.creativeapps.schoolbusdriver.ui.activity.main.childList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Child;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivityModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;


public class ChildListFragment extends Fragment implements ParentChildrenSection.ItemClickListener,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "ChildListFragment";

    private MainActivityModel mViewModel;
    private Driver mDriver;

    private SectionedRecyclerViewAdapter mSectionedAdapter;
    private RecyclerView mChildListRecyclerView;

    //search view to filter children either with their names
    private SearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = ((MainActivity) getActivity()).createViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_child_list, container, false);

        mSectionedAdapter = new SectionedRecyclerViewAdapter();


        mChildListRecyclerView = view.findViewById(R.id.child_list_recycler_view);
        mChildListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mChildListRecyclerView.setAdapter(mSectionedAdapter);



        mDriver = Util.getSavedObjectFromPreference(getContext(),
                "mPreference", "Driver", com.creativeapps.schoolbusdriver.data.network.models.Driver.class);

        if(mDriver!=null) {
            ((MainActivity) getActivity()).showHideProgressBar(true);
            mViewModel.getDriverServer(mDriver.getCountry_code(), mDriver.getTel_number(), mDriver.getSecretKey());
        }
        else
        {
            ((MainActivity) getActivity()).logout();
        }

        //observe changes for driver information
        mViewModel.getDriver().observe(this, new DriverObserver());

        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getCheckinStatus().removeObservers(this);
        mViewModel.getIsWaitRespChecking().removeObservers(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.getCheckinStatus().observe(this, new CheckingObserver());
        mViewModel.getIsWaitRespChecking().observe(this, new LoadingObserver());

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        mSearchView = view.findViewById(R.id.searchChildren);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.clearFocus();

        mSearchView.setIconifiedByDefault(true);
        mSearchView.setFocusable(false);
        mSearchView.setIconified(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_childlist_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh_driver:
                if(mDriver!=null) {
                    ((MainActivity) getActivity()).showHideProgressBar(true);
                    mViewModel.getDriverServer(mDriver.getCountry_code(), mDriver.getTel_number(),
                            mDriver.getSecretKey());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(View view, int position, final int check_in_out) {
        int childPos = mSectionedAdapter.getPositionInSection(position);
        ParentChildrenSection section = (ParentChildrenSection) mSectionedAdapter.getSectionForPosition(position);
        final Child child = section.mChildrenList.get(childPos);

        String title = "", message="";
        if(check_in_out == Util.CHECK_IN_FLAG)
        {
            title = "Check In";
            message = "You are about to check in " + child.getchildName() + ", are you sure?";
        }
        else if(check_in_out == Util.CHECK_OUT_FLAG)
        {
            title = "Check Out";
            message = "You are about to check out " + child.getchildName() + ", are you sure?";
        }
        //display an alert dialog to warn the driver that he is about to check in or out a child.
        new AlertDialog.Builder(this.getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).showHideProgressBar(true);
                        mViewModel.checkInOutChildServer(mDriver.getSecretKey(),
                                child.getId(), check_in_out, getString(R.string.checked_in_string),
                                getString(R.string.checked_out_string));
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        //clear the search view after click search
        mSearchView.clearFocus();
        List<Parent> parents = mDriver.getParents();
        List<Parent> filteredParents = new ArrayList<Parent>();
        for (Parent p:parents) {
            final List<Child> children = p.getChildren();

            for (Child c : children) {
                if(c.getchildName().toLowerCase().contains(newText.toLowerCase()))
                {
                    filteredParents.add(p);
                }
            }
        }
        setChildListUI(filteredParents);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onClose() {
        setChildListUI(mDriver.getParents());
        return false;
    }


    private class CheckingObserver implements Observer<Integer> {
        @Override
        public void onChanged(Integer CheckingStatus) {
            if (CheckingStatus == null)
                return;

            if (CheckingStatus != 0) {
                Log.d(TAG, "CheckingStatus=> success");

                new AlertDialog.Builder(ChildListFragment.this.getContext())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Success")
                        .setMessage("Child checked " + (CheckingStatus==Util.CHECK_IN_FLAG? "in":"out") + " successfully.")
                        .setNegativeButton("Ok", null)
                        .show();
            }
            else{
                Log.d(TAG, "CheckingStatus=> failed");
                //display an alert dialog to warn the user that there is an error
                new AlertDialog.Builder(ChildListFragment.this.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Error")
                        .setMessage(getString(R.string.unexpected_error))
                        .setNegativeButton("Ok", null)
                        .show();

            }
            mViewModel.setCheckinStatus(null);
        }
    }

    /*Observer for the boolean live data IsWaitchecking that indicates if the process*/
    private class LoadingObserver implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean isLoading) {
            if (isLoading == null) return;
            //if the process is running
            if (isLoading) {
                ((MainActivity) getActivity()).showHideProgressBar(true);
            } else {
                ((MainActivity) getActivity()).showHideProgressBar(false);
            }
        }
    }

    private class DriverObserver implements Observer<Driver> {

        @Override
        public void onChanged(@Nullable Driver driver) {
            ((MainActivity) getActivity()).showHideProgressBar(false);
            if (driver == null) {
                //get the last saved parent information from the SharedPreference
                mDriver = Util.getSavedObjectFromPreference(getContext(),
                        "mPreference", "Driver", Driver.class);
                //update the UI to show "offline" status
                mViewModel.setConnectivityStatus(false);
            } else {
                mDriver = driver;
                Util.saveObjectToSharedPreference(getContext(),
                        "mPreference", "Driver", driver);
                //update the UI to show "online" status
                mViewModel.setConnectivityStatus(true);
            }
            if(mDriver!=null && mDriver.getParents()!=null && mDriver.getParents().size()>0)
                //put the homes of the parents on the map
                setChildListUI(mDriver.getParents());
            //if the driver is not verified, go to the login activity
            if (mDriver.getVerified() != 1)
                //logout
                ((MainActivity) getActivity()).logout();
        }
    }

    private void setChildListUI(List<Parent> parents) {

        mSectionedAdapter.removeAllSections();

        final Map<String, List<Child>> childrenMap = new LinkedHashMap<>();

        for (Parent p:parents) {
            final List<Child> children = p.getChildren();

            if (children.size() > 0) {
                childrenMap.put(p.getName(), children);
            }
        }

        for (final Map.Entry<String, List<Child>> entry : childrenMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                mSectionedAdapter.addSection(new ParentChildrenSection(entry.getKey(), entry.getValue(), this));
            }
        }

        mChildListRecyclerView.setAdapter(mSectionedAdapter);
        mSectionedAdapter.notifyDataSetChanged();
    }
}