package com.zly.loadmorerecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by zhuleiyue on 16/1/26.
 */
public class SampleAdapter extends LoadMoreRecyclerView.LoadMoreAdapter {
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mData;

    public SampleAdapter(Context context, ArrayList<String> data){
        mLayoutInflater = LayoutInflater.from(context);
        mData = data;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getViewType(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemView(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mLayoutInflater.inflate(R.layout.list_item_sample, parent, false));
    }

    @Override
    public void onBindItemView(RecyclerView.ViewHolder holder, int position) {
        ((ItemViewHolder)holder).tvItem.setText(mData.get(position));
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent) {
        return new FooterViewHolder(mLayoutInflater.inflate(R.layout.footer_view_sample, parent, false));
    }

    @Override
    protected void bindFooterItem(RecyclerView.ViewHolder holder) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
        switch (mLoadMoreStatus) {
            case STATUS_LOADING:
                holder.itemView.setVisibility(View.VISIBLE);
                footerViewHolder.pb.setVisibility(View.VISIBLE);
                footerViewHolder.tv.setText("正在加载更多...");
                break;
            case STATUS_EMPTY:
                holder.itemView.setVisibility(View.VISIBLE);
                footerViewHolder.pb.setVisibility(View.GONE);
                footerViewHolder.tv.setText("没有更多了");
                holder.itemView.setOnClickListener(null);
                break;
            case STATUS_ERROR:
                holder.itemView.setVisibility(View.VISIBLE);
                footerViewHolder.pb.setVisibility(View.GONE);
                footerViewHolder.tv.setText("加载出错，点击重试");
                holder.itemView.setOnClickListener(mListener);
                break;
            case STATUS_PREPARE:
                holder.itemView.setVisibility(View.INVISIBLE);
                break;
        }

    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvItem = (TextView) itemView.findViewById(R.id.tv_item);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        ProgressBar pb;
        TextView tv;

        public FooterViewHolder(View itemView) {
            super(itemView);
            pb = (ProgressBar) itemView.findViewById(R.id.pb_footer_view);
            tv = (TextView) itemView.findViewById(R.id.tv_footer_view);
        }
    }

}
