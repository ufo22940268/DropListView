#DropRefreshView

A new PullToRefresh View like the mail app in ios.

![](./slide2.gif)

##Import in gradle

    compile 'me.biubiubiu.droprefreshview:library:0.1.1'

##Usage

**Declare in xml**

```xml
    <com.bettycc.droprefreshview.library.DropListView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

**Register listener in activity**

        dropListView.setOnRefreshListener(new DropListView.OnRefreshListener() {
            @Override
            public void onPullDownToRefresh() {
            }
        });


When refreshing finished, remeber to call `mListView.onRefreshCompleted` callback.

##Customization

    <declare-styleable name="DropListView">
        <attr name="drop_color" format="color"/>
    </declare-styleable>

