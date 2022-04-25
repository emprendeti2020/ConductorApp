package com.creativeapps.schoolbusdriver.ui.activity.main.childList;

import android.view.View;
import android.widget.TextView;
import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;


import androidx.recyclerview.widget.RecyclerView;



public class ChildItemViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public TextView mChildName, mChildAbsence;
    public Integer mAbsent;
    public Integer mLastCheckStatus;

    private ParentChildrenSection.ItemClickListener mCheckInOutClickListener;


    public ChildItemViewHolder(final View view, ParentChildrenSection.ItemClickListener itemClickListener) {
        super(view);

        mCheckInOutClickListener = itemClickListener;

        mChildName = view.findViewById(R.id.child_name);
        mChildAbsence = view.findViewById(R.id.child_absence);
        mView = view;

        mView.findViewById(R.id.check_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckInOutClickListener.onItemClick(view, getAdapterPosition(), Util.CHECK_IN_FLAG);
            }
        });

        mView.findViewById(R.id.check_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckInOutClickListener.onItemClick(view, getAdapterPosition(), Util.CHECK_OUT_FLAG);
            }
        });
    }

}


