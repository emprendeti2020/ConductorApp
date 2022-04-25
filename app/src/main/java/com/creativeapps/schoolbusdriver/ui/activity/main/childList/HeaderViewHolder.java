package com.creativeapps.schoolbusdriver.ui.activity.main.childList;

import android.view.View;
import android.widget.TextView;

import com.creativeapps.schoolbusdriver.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


final class HeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView mParentNameTextView;

    HeaderViewHolder(@NonNull View view) {
        super(view);

        mParentNameTextView = view.findViewById(R.id.parentNameTextView);
    }
}
