package com.xmeteor.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;
import com.xmeteor.view.adater.GridListVIewAdater;
import com.xmeteor.view.NestScrollView;
import com.xmeteor.view.R;

import java.util.ArrayList;

/**
 * Created by xs on 15/1/20.
 */
public class ListViewInsideDemo extends Activity {

    private NestScrollView mScrollView;

    private ListView mListView;

    static ArrayList<String> data = new ArrayList<String>();

    static {
        for (int i = 0; i < 30; i++) {
            data.add("here" + String.valueOf(i));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mScrollView = (NestScrollView) findViewById(R.id.scrollview);
        mListView = (ListView) findViewById(R.id.listview);

        // in xml layout first set listview height 0dp, then set listview height
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int viewHeight = mScrollView.getHeight();
                    mListView.getLayoutParams().height = viewHeight;
                }
            });
        }
        // set listview
        mScrollView.setListView(mListView);
        // must set setFocusable fase or the scrollview will scroll to listview top first
        mListView.setFocusable(false);

        mListView.setAdapter(mAdater);
        mAdater.notifyDataSetChanged();
    }

    class Holder extends GridListVIewAdater.ViewHolder<String> implements Cloneable {

        private View rootView;

        private TextView title;

        @Override
        public GridListVIewAdater.ViewHolder newInstance() {
            return new Holder();
        }

        @Override
        public View createView(int index, LayoutInflater inflater) {
            rootView = inflater.inflate(R.layout.list_item, null);
            title = (TextView) rootView.findViewById(R.id.title);
            return rootView;
        }

        @Override
        public void showData(int index, String data) {
            title.setText("--->" + data);
        }
    }

    private GridListVIewAdater mAdater = new GridListVIewAdater(1, data, new Holder()) {
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    };

}
