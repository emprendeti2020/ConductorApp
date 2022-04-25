package com.creativeapps.schoolbusdriver.ui.activity.main.contacts;


import static com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity.getContext;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ParentsAdapter extends RecyclerView.Adapter<ParentsAdapter.ParentsViewHolder> {

    //original parent list
    private List<Parent> mParentListOriginal;
    //filtered parent list (contains the parents that matches a search text)
    private List<Parent> mParentListFiltered;
    //Listener to handle the choice of parents from the displayed list
    private ParentsAdapterListener mListener;
    private Context context;
    //region Constructor
    public ParentsAdapter(List<Parent> parentList, ParentsAdapterListener listener) {
        this.mListener = listener;

        this.mParentListOriginal = new ArrayList<>();
        this.mParentListOriginal.addAll(parentList);

        this.mParentListFiltered = new ArrayList<>();
        this.mParentListFiltered.addAll(parentList);
    }
    //endregion

    @Override
    public ParentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate rows of the RecycleView
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.from(parent.getContext())
                .inflate(R.layout.parent_contact_row_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ParentsViewHolder vh = new ParentsViewHolder(view);
        // set the Context here
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(ParentsViewHolder holder, final int position) {
        //Display only the parent name and his telephone number in each row
        Parent parent = mParentListFiltered.get(position);
        holder.name.setText(parent.getName());
        holder.phone.setText(parent.getTel_number());
        holder.mContext = context;
        holder.mParent = parent;
        if(parent.getAddress_longitude() != null && parent.getAddress_latitude() != null) {
            holder.directions.setVisibility(View.VISIBLE);
        }
        else {
            holder.directions.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        //return the size of the list to be displayed
        if(mParentListFiltered != null){
            return mParentListFiltered.size();
        }
        return 0;
    }

    /*filter the parent list based on parent's name or his mobile number*/
    public void filter(String newText) {
        //clear the filtered parent list with every new search text
        mParentListFiltered.clear();
        //make search text lower case
        newText = newText.toLowerCase();
        //if nothing entered for search, keep the original parent list
        if (newText.length() == 0) {
            mParentListFiltered.addAll(mParentListOriginal);
        }
        else
        {
            //otherwise, search for parents with either names or telephone numbers matched with the
            // search text
            for (Parent model : mParentListOriginal) {
                final String name = model.getName().toLowerCase();
                final String tel_number = model.getTel_number().toLowerCase();
                //if either the name of the parent or his telephone number contains the search text,
                // include him in the filtered parent list
                if ((name.contains(newText)) || (tel_number.contains(newText))) {
                    mParentListFiltered.add(model);
                }
            }
        }
        //signal data change
        notifyDataSetChanged();
    }

    /*Define a class that instantiate gui elements of rows of the displayed list.
    Object from ParentsViewHolder class is used in onBindViewHolder function*/
    public class ParentsViewHolder extends RecyclerView.ViewHolder {
        //only name and telephone number are used here
        public TextView name, phone;
        public ImageView call;
        public ImageView directions;
        public Context mContext;
        public Parent mParent;
        public ParentsViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            phone = view.findViewById(R.id.phone);
            call = view.findViewById(R.id.call);
            directions = view.findViewById(R.id.directions);
            //handle click event of a row
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                // send selected parent in callback
                mListener.onParentSelected(mParentListFiltered.get(getAdapterPosition()));
                }
            });
            directions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("https")
                            .authority("www.google.com")
                            .appendPath("maps")
                            .appendPath("dir")
                            .appendPath("")
                            .appendQueryParameter("api", "1")
                            .appendQueryParameter("destination", mParent.getAddress_latitude() + "," + mParent.getAddress_longitude());
                    String url = builder.build().toString();
                    Log.d("Directions", url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    mContext.startActivity(i);
                }
            });
        }
    }

    /*interface used to define call back of handling click events on displayed rows*/
    public interface ParentsAdapterListener {
        void onParentSelected(Parent parent);
    }
}