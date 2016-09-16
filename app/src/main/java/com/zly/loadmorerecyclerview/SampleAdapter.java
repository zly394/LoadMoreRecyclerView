package com.zly.loadmorerecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zhuleiyue on 16/1/26.
 */
public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.ItemViewHolder> {
    private ArrayList<String> mData;

    public SampleAdapter(ArrayList<String> data) {
        mData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sample, parent, false));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.tvItem.setText(mData.get(position));
        holder.itemView.setPadding(0, 0, 0, 100 + new Random().nextInt(100));
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvItem = (TextView) itemView.findViewById(R.id.tv_item);
        }
    }
}
