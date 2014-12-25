package com.bettycc.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

/**
 * Created by ccheng on 12/25/14.
 */
public class DropViewContainer extends ListView {

    public DropViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        View headerView = LayoutInflater.from(context).inflate(R.layout.sample_my_view, this, false);
        addHeaderView(headerView);
    }
}
