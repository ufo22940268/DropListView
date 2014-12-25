package com.bettycc.myapplication;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DropViewContainer dropViewContainer = (DropViewContainer) findViewById(R.id.list);
        dropViewContainer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {
                "a",
                "b",
                "c"
        }));
    }
}
