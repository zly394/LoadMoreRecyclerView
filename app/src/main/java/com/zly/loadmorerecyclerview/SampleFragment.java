package com.zly.loadmorerecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zly.loadmore.LoadMoreRecyclerView;

import java.util.ArrayList;

/**
 * Created by zhuleiyue on 16/9/16.
 */
public class SampleFragment extends Fragment {
    public static final String MODE = "mode";
    public static final int MODE_LIST = 1;
    public static final int MODE_GRID = 2;
    public static final int MODE_STAGGERED_GRID = 3;
    private LoadMoreRecyclerView mRvSample;
    private ArrayList<String> mData = new ArrayList<>();
    private int mCurrentPage = 1;
    private static Handler mHandler = new Handler();
    private boolean isRetry = false;
    private int mMode;

    public static SampleFragment newInstance(int mode) {
        SampleFragment fragment = new SampleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(MODE, mode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMode = bundle.getInt(MODE, MODE_LIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getData(1);
    }

    private void initView(View view) {
        mRvSample = (LoadMoreRecyclerView) view.findViewById(R.id.rv_sample);
        RecyclerView.LayoutManager layoutManager;
        switch (mMode) {
            case MODE_LIST:
            default:
                layoutManager = new LinearLayoutManager(getContext());
                break;
            case MODE_GRID:
                layoutManager = new GridLayoutManager(getContext(), 4);
                break;
            case MODE_STAGGERED_GRID:
                layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                break;
        }
        mRvSample.setLayoutManager(layoutManager);
        mRvSample.setAdapter(new SampleAdapter(mData));
        mRvSample.setOnLoadMore(() -> getData(mCurrentPage));
    }

    private void getData(final int page) {
        if (page == 1) {
            for (int i = (page - 1) * 36; i < page * 36; i++) {
                mData.add("RecyclerViewItem" + i);
            }
            mCurrentPage++;
            mRvSample.getAdapter().notifyDataSetChanged();
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (page % 3 == 0 && !isRetry) {
                        isRetry = true;
                        mHandler.post(() -> mRvSample.setLoadMoreStatus(LoadMoreRecyclerView.STATUS_ERROR));
                    } else {
                        isRetry = false;
                        for (int i = (page - 1) * 36; i < page * 36; i++) {
                            mData.add("RecyclerViewItem" + i);
                        }
                        mCurrentPage++;
                        mHandler.post(() -> {
                            mRvSample.setLoadMoreStatus(LoadMoreRecyclerView.STATUS_PREPARE);
                            mRvSample.getAdapter().notifyDataSetChanged();
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
