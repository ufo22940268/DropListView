package com.bettycc.myapplication;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private String[] mStrings;
    private ArrayAdapter<String> mAdapter;
    private List<String> mStringList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DropViewContainer dropViewContainer = (DropViewContainer) findViewById(R.id.list);
        String[] strings = new String[]{
                "a",
                "b",
                "c"
        };
        mStringList = new ArrayList(Arrays.asList(strings));
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStringList);
        dropViewContainer.setAdapter(mAdapter);
        dropViewContainer.setOnRefreshListener(new DropViewContainer.OnRefreshListener() {
            @Override
            public void onPullDownToRefresh() {
                /**
                 * Wait 10 seconds and finish refreshing.
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mStringList.add(String.valueOf(((char) (mStringList.get(mStringList.size() - 1).charAt(0) + 1))));
                                    mAdapter.notifyDataSetChanged();
                                    dropViewContainer.onRefreshCompleted();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
