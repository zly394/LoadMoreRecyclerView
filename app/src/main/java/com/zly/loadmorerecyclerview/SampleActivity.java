package com.zly.loadmorerecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;

import com.zly.loadmore.LoadMoreRecyclerView;

import java.util.ArrayList;

public class SampleActivity extends AppCompatActivity {
    private SampleAdapter mSampleAdapter;
    private LoadMoreRecyclerView mRvSample;
    private ArrayList<String> mData = new ArrayList<>();
    private int mCurrentPage = 1;
    private static Handler mHandler = new Handler();
    private boolean isRetry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initView();
        getData(1);
    }

    private void initView() {
        mRvSample = (LoadMoreRecyclerView) findViewById(R.id.rv_sample);
//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRvSample.setLayoutManager(new GridLayoutManager(this, 5));
        mSampleAdapter = new SampleAdapter(mData);
        mRvSample.setAdapter(mSampleAdapter);
        mRvSample.setOnLoadMore(() -> getData(++mCurrentPage));
    }

    private void getData(final int page) {
        if (page == 1) {
            for (int i = (page - 1) * 30; i < page * 30; i++) {
                mData.add("RecyclerViewItem" + i);
            }
            mSampleAdapter.notifyDataSetChanged();
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (page == 4 && !isRetry) {
                        mHandler.post(() -> mRvSample.setLoadMoreStatus(LoadMoreRecyclerView.STATUS_ERROR));
                    } else {
                        for (int i = (page - 1) * 30; i < page * 30; i++) {
                            mData.add("RecyclerViewItem" + i);
                        }
                        mHandler.post(() -> {
                            mRvSample.setLoadMoreStatus(LoadMoreRecyclerView.STATUS_PREPARE);
                            mSampleAdapter.notifyDataSetChanged();
                        });
                    }
                } catch (InterruptedException e) {
                    mHandler.post(() -> mRvSample.setLoadMoreStatus(LoadMoreRecyclerView.STATUS_ERROR));
                    e.printStackTrace();
                }

            }).start();
        }
    }
}
