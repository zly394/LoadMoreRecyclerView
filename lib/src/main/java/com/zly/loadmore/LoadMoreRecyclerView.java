package com.zly.loadmore;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by zhuleiyue on 15/11/21.
 */
public class LoadMoreRecyclerView extends RecyclerView {
    public static final int STATUS_PREPARE = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_EMPTY = -1;
    public static final int STATUS_ERROR = -2;
    public static final int STATUS_DISMISS = -3;

    private boolean isScrollDown;
    private LoadMoreAdapter mLoadMoreAdapter;
    private OnLoadMore mOnLoadMore;
    private boolean mIsLoadMore;

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
                        int lastVisibleItem;
                        LayoutManager layoutManager = recyclerView.getLayoutManager();
                        if (layoutManager instanceof LinearLayoutManager) {
                            lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                            lastVisibleItem = findMax(into);
                        } else {
                            throw new RuntimeException("Unsupported LayoutManager used");
                        }
                        int totalItemCount = layoutManager.getItemCount();
                        if (mLoadMoreAdapter.getLoadMoreStatus() == STATUS_PREPARE
                                && lastVisibleItem >= totalItemCount - 1 && isScrollDown) {
                            mLoadMoreAdapter.setLoadMoreStatus(STATUS_LOADING);
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
        this.mIsLoadMore = true;
        if (mLoadMoreAdapter != null) {
            this.mLoadMoreAdapter.setIsLoadMore(true);
        }
        this.mOnLoadMore = onLoadMore;
    }

    public void setAdapter(Adapter adapter) {
        this.mLoadMoreAdapter = new LoadMoreAdapter(adapter, mIsLoadMore);
        this.mLoadMoreAdapter.setRetryListener(retryListener);
        super.setAdapter(mLoadMoreAdapter);
    }

    public void setLoadMoreStatus(int status) {
        if (mLoadMoreAdapter != null) {
            mLoadMoreAdapter.setLoadMoreStatus(status);
        }
    }

    public interface OnLoadMore {
        void onLoad();
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    View.OnClickListener retryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLoadMoreAdapter.setLoadMoreStatus(STATUS_LOADING);
            mOnLoadMore.onLoad();
        }
    };

    private static class LoadMoreAdapter extends Adapter {
        private static final int TYPE_FOOTER = -1;
        protected int mLoadMoreStatus = STATUS_PREPARE;
        protected View.OnClickListener mListener;
        private Adapter mAdapter;
        private boolean mIsLoadMore;
        private GridLayoutManager mGridLayoutManager;

        public LoadMoreAdapter(Adapter adapter, boolean isLoadMore) {
            this.mAdapter = adapter;
            this.mIsLoadMore = isLoadMore;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                this.mGridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            }
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (mIsLoadMore) {
                if (mGridLayoutManager != null) {
                    mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return position == getItemCount() - 1 ? mGridLayoutManager.getSpanCount() : 1;
                        }
                    });
                }
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                    if (holder.getLayoutPosition() == getItemCount() - 1) {
                        ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
                    }
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mIsLoadMore && viewType == TYPE_FOOTER) {
                return onCreateFooterViewHolder(parent);
            } else {
                return mAdapter.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mIsLoadMore && getItemViewType(position) == TYPE_FOOTER) {
                bindFooterItem(holder);
            } else {
                mAdapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            return mIsLoadMore ? mAdapter.getItemCount() == 0 ? 0 : mAdapter.getItemCount() + 1 : mAdapter.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            if (mIsLoadMore && position == getItemCount() - 1) {
                return TYPE_FOOTER;
            } else {
                return mAdapter.getItemViewType(position);
            }
        }

        void setLoadMoreStatus(int status) {
            this.mLoadMoreStatus = status;
            notifyItemChanged(getItemCount() - 1);
        }

        public void setRetryListener(View.OnClickListener listener) {
            this.mListener = listener;
        }

        public int getLoadMoreStatus() {
            return this.mLoadMoreStatus;
        }

        public ViewHolder onCreateFooterViewHolder(ViewGroup parent) {
            return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_view_sample, parent, false));
        }

        public void setIsLoadMore(boolean isLoadMore) {
            this.mIsLoadMore = isLoadMore;
        }

        protected void bindFooterItem(ViewHolder holder) {
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
                case STATUS_DISMISS:
                    holder.itemView.setVisibility(GONE);
            }
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
