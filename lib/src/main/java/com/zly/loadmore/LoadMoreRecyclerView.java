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

    /**
     * 是否是向下滑动
     */
    private boolean isScrollDown;
    private LoadMoreAdapter mLoadMoreAdapter;
    /**
     * 加载更多的回调接口
     */
    private OnLoadMore mOnLoadMore;
    /**
     * 是否加载更多
     */
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
                if (mOnLoadMore != null) {// 如果加载更多的回调接口不为空
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {// RecyclerView已经停止滑动
                        int lastVisibleItem;
                        // 获取RecyclerView的LayoutManager
                        LayoutManager layoutManager = recyclerView.getLayoutManager();
                        // 获取到最后一个可见的item
                        if (layoutManager instanceof LinearLayoutManager) {// 如果是LinearLayoutManager
                            lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                        } else if (layoutManager instanceof StaggeredGridLayoutManager) {// 如果是StaggeredGridLayoutManager
                            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                            lastVisibleItem = findMax(into);
                        } else {// 否则抛出异常
                            throw new RuntimeException("Unsupported LayoutManager used");
                        }
                        // 获取item的总数
                        int totalItemCount = layoutManager.getItemCount();
                        /*
                            如果RecyclerView的footer的状态为准备中
                            并且最后一个可见的item为最后一个item
                            并且是向下滑动
                         */
                        if (mLoadMoreAdapter.getLoadMoreStatus() == STATUS_PREPARE
                                && lastVisibleItem >= totalItemCount - 1 && isScrollDown) {
                            // 设置RecyclerView的footer的状态为加载中
                            mLoadMoreAdapter.setLoadMoreStatus(STATUS_LOADING);
                            // 触发加载更多的回调方法
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

    /**
     * 设置加载更多的回调接口
     *
     * @param onLoadMore 加载更多的回调接口
     */
    public void setOnLoadMore(OnLoadMore onLoadMore) {
        // 是否加载更多置为true
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

    /**
     * 设置footer的状态
     */
    public void setLoadMoreStatus(int status) {
        if (mLoadMoreAdapter != null) {
            mLoadMoreAdapter.setLoadMoreStatus(status);
        }
    }

    /**
     * 加载更多的回调接口
     */
    public interface OnLoadMore {
        void onLoad();
    }

    /**
     * 获取数组中的最大值
     *
     * @param lastPositions 需要找到最大值的数组
     * @return 数组中的最大值
     */
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * footer的重试点击事件
     */
    View.OnClickListener retryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLoadMoreAdapter.setLoadMoreStatus(STATUS_LOADING);
            mOnLoadMore.onLoad();
        }
    };

    private static class LoadMoreAdapter extends Adapter {
        /**
         * 添加footer的类型
         */
        private static final int TYPE_FOOTER = -1;
        /**
         * footer的状态
         */
        protected int mLoadMoreStatus = STATUS_PREPARE;
        /**
         * footer的点击事件
         */
        protected View.OnClickListener mListener;
        /**
         * 正常item的adapter
         */
        private Adapter mAdapter;
        /**
         * 是否加载更多
         */
        private boolean mIsLoadMore;
        /**
         * GridLayoutManager
         */
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
            if (mIsLoadMore) {// 如果加载更多
                if (mGridLayoutManager != null) {
                    mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            // 当position为最后一项时返回spanCount
                            return position == getItemCount() - 1 ? mGridLayoutManager.getSpanCount() : 1;
                        }
                    });
                }
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                    if (holder.getLayoutPosition() == getItemCount() - 1) { // 当position为最后一项时这是FullSpan为true
                        ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
                    }
                }
            }
        }

        /**
         * 如果是footer类型,创建FooterView
         * 否则创建正常的ItemView
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mIsLoadMore && viewType == TYPE_FOOTER) {
                return onCreateFooterViewHolder(parent);
            } else {
                return mAdapter.onCreateViewHolder(parent, viewType);
            }
        }

        /**
         * 如果加载更多且是footer类型,则展示footer
         * 否则展示正常的item
         */
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mIsLoadMore && getItemViewType(position) == TYPE_FOOTER) {
                bindFooterItem(holder);
            } else {
                mAdapter.onBindViewHolder(holder, position);
            }
        }

        /**
         * 如果加载更多
         * 如果正常的item为0  则不显示footer,返回0
         * 如果正常的item不为0  则返回mAdapter.getItemCount() + 1
         * 如果不加载更多
         * 返回mAdapter.getItemCount()
         */
        @Override
        public int getItemCount() {
            return mIsLoadMore ? mAdapter.getItemCount() == 0 ? 0 : mAdapter.getItemCount() + 1 : mAdapter.getItemCount();
        }

        /**
         * 如果加载更多且position为最有一个,则返回类型为footer
         * 否则返回mAdapter.getItemViewType(position)
         */
        @Override
        public int getItemViewType(int position) {
            if (mIsLoadMore && position == getItemCount() - 1) {
                return TYPE_FOOTER;
            } else {
                return mAdapter.getItemViewType(position);
            }
        }

        /**
         * 设置footer的状态,并通知更改
         */
        void setLoadMoreStatus(int status) {
            this.mLoadMoreStatus = status;
            notifyItemChanged(getItemCount() - 1);
        }

        /**
         * 设置footer的点击重试事件
         * @param listener
         */
        public void setRetryListener(View.OnClickListener listener) {
            this.mListener = listener;
        }

        public int getLoadMoreStatus() {
            return this.mLoadMoreStatus;
        }

        /**
         * 创建FooterView
         */
        public ViewHolder onCreateFooterViewHolder(ViewGroup parent) {
            return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_view_sample, parent, false));
        }

        /**
         * 设置是否加载更多
         */
        public void setIsLoadMore(boolean isLoadMore) {
            this.mIsLoadMore = isLoadMore;
        }

        /**
         * 展示FooterView
         * @param holder
         */
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
