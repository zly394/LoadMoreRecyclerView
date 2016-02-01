package com.zly.loadmorerecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zhuleiyue on 15/11/21.
 */
public class LoadMoreRecyclerView extends RecyclerView {
    private boolean isScrollDown;
    private LoadMoreAdapter mLoadMoreAdapter;

    private OnLoadMore mOnLoadMore;

    public LoadMoreRecyclerView(Context context) {
        this(context, null);
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mOnLoadMore != null) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        int totalItemCount = layoutManager.getItemCount();
                        if (mLoadMoreAdapter.getLoadMoreStatus() == LoadMoreAdapter.STATUS_PREPARE
                                && lastVisibleItem >= totalItemCount - 1 && isScrollDown) {
                            mOnLoadMore.onLoad();
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isScrollDown = dy > 0;
            }
        });
    }

    public void setOnLoadMore(OnLoadMore onLoadMore) {
        this.mOnLoadMore = onLoadMore;
    }

    public void setAdapter(LoadMoreAdapter adapter) {
        this.mLoadMoreAdapter = adapter;
        super.setAdapter(adapter);
    }

    public interface OnLoadMore {
        void onLoad();
    }

    public static abstract class LoadMoreAdapter<VH extends ViewHolder> extends Adapter<VH> {
        private static final int TYPE_FOOTER = -1;
        public static final int STATUS_PREPARE = 0;
        public static final int STATUS_LOADING = 1;
        public static final int STATUS_EMPTY = -1;
        public static final int STATUS_ERROR = -2;
        protected int mLoadMoreStatus = STATUS_PREPARE;
        protected View.OnClickListener mListener;

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                return onCreateFooterViewHolder(parent);
            } else {
                return onCreateItemView(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                bindFooterItem(holder);
            } else {
                onBindItemView(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            return getCount() == 0 ? 0 : getCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            } else {
                return getViewType(position);
            }
        }

        public void setLoadMoreStatus(int status, View.OnClickListener listener) {
            this.mLoadMoreStatus = status;
            this.mListener = listener;
            notifyItemChanged(getItemCount() - 1);
        }

        public int getLoadMoreStatus() {
            return this.mLoadMoreStatus;
        }

        public abstract int getCount();

        public abstract int getViewType(int position);

        public abstract VH onCreateItemView(ViewGroup parent, int viewType);

        public abstract void onBindItemView(ViewHolder holder, int position);

        public abstract VH onCreateFooterViewHolder(ViewGroup parent);

        protected abstract void bindFooterItem(ViewHolder holder);
    }
}
