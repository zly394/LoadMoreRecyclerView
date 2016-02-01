package com.zly.loadmorerecyclerview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;

public class SampleActivity extends AppCompatActivity {
    private SampleAdapter mSampleAdapter;
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
        LoadMoreRecyclerView mRvSample = (LoadMoreRecyclerView) findViewById(R.id.rv_sample);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvSample.setLayoutManager(linearLayoutManager);
        mSampleAdapter = new SampleAdapter(this, mData);
        mRvSample.setAdapter(mSampleAdapter);
        mRvSample.setOnLoadMore(new LoadMoreRecyclerView.OnLoadMore() {
            @Override
            public void onLoad() {
                getData(++mCurrentPage);
            }
        });
    }

    private void getData(final int page) {
        if (page == 1) {
            for (int i = (page - 1) * 10; i < page * 10; i++) {
                mData.add("RecyclerViewItem" + i);
            }
            mSampleAdapter.notifyDataSetChanged();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSampleAdapter.setLoadMoreStatus(LoadMoreRecyclerView.LoadMoreAdapter.STATUS_LOADING, null);
                            }
                        });
                        Thread.sleep(2000);
                        if (page == 4 && !isRetry) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSampleAdapter.setLoadMoreStatus(LoadMoreRecyclerView.LoadMoreAdapter.STATUS_ERROR, retryListener);
                                }
                            });
                        } else {
                            for (int i = (page - 1) * 10; i < page * 10; i++) {
                                mData.add("RecyclerViewItem" + i);
                            }
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSampleAdapter.setLoadMoreStatus(LoadMoreRecyclerView.LoadMoreAdapter.STATUS_PREPARE, null);
                                    mSampleAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSampleAdapter.setLoadMoreStatus(LoadMoreRecyclerView.LoadMoreAdapter.STATUS_ERROR, retryListener);
                            }
                        });
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    public View.OnClickListener retryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isRetry = true;
            getData(mCurrentPage);
        }
    };

}
