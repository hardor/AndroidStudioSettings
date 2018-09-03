package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.RanobeConstans.chaptersNum;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import ru.profapp.RanobeReader.Common.OnLoadMoreListener;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.RanobeRecyclerFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Ranobe} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
class RanobeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Ranobe> mValues;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_GROUP_TITLE = 2;
    private Context mContext;
    private OnLoadMoreListener onLoadMoreListener;

    private boolean isLoading;
    private int pastVisiblesItems, lastVisibleItem, totalItemCount;

    public RanobeRecyclerViewAdapter(RecyclerView recyclerView, List<Ranobe> items) {
        mValues = items;

        final LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {
                    lastVisibleItem = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading) {
                        if ((lastVisibleItem + pastVisiblesItems) >= totalItemCount) {

                            if (onLoadMoreListener != null) {
                                isLoading = true;
                                onLoadMoreListener.onLoadMore();
                            } else {
                                isLoading = false;
                            }
                        }
                    }
                }
            }
        });

    }


    @Override
    public int getItemViewType(int position) {
        if (mValues.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (Objects.equals(mValues.get(position).getRanobeSite(),
                StringResources.Title_Site)) {
            return VIEW_TYPE_GROUP_TITLE;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        switch (viewType) {
            case VIEW_TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranobe_item,
                        parent, false);
                return new RanobeViewHolder(view);
            }
            default:
            case VIEW_TYPE_LOADING: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading,
                        parent, false);
                return new LoadingViewHolder(view);
            }
            case VIEW_TYPE_GROUP_TITLE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title,
                        parent, false);
                return new TitleViewHolder(view);
            }

        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RanobeViewHolder) {
            ((RanobeViewHolder) holder).mItem = mValues.get(position);
            ((RanobeViewHolder) holder).mTitleView.setText(mValues.get(position).getTitle());

            if (mValues.get(position).getReadyDate() != null) {
                long diff = (new Date().getTime() - mValues.get(position).getReadyDate().getTime())
                        / 1000 / 60;

                int numOfDays = (int) (diff / (60 * 24));
                int hours = (int) (diff / 60 - numOfDays * 24);
                int minutes = (int) (diff % 60);

                String updateTime = mContext.getString(R.string.update_time, numOfDays, hours,
                        minutes);

                ((RanobeViewHolder) holder).mUpdateTime.setText(updateTime);
            } else {
                ((RanobeViewHolder) holder).mUpdateTime.setVisibility(View.INVISIBLE);
            }


                ((RanobeViewHolder) holder).mImageView.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(mValues.get(position).getImage()).apply(
                        new RequestOptions()
                                .placeholder(R.drawable.ic_adb_black_24dp)
                                .error(R.drawable.ic_error_outline_black_24dp)
                                .fitCenter()
                ).into(((RanobeViewHolder) holder).mImageView);

            ((RanobeViewHolder) holder).mView.setOnClickListener(v -> {

                Intent intent = new Intent(mContext, RanobeInfoActivity.class);
                RanobeKeeper.getInstance().setRanobe(((RanobeViewHolder) holder).mItem);

                if (RanobeKeeper.getInstance().getRanobe() != null) {
                    mContext.startActivity(intent);
                }

            });

            List<Chapter> chapterList = ((RanobeViewHolder) holder).mItem.getChapterList();

            ChapterRecyclerViewAdapter adapter = new ChapterRecyclerViewAdapter(
                    chapterList.subList(0, Math.min(chaptersNum, chapterList.size())), mContext,
                    ((RanobeViewHolder) holder).mItem);

            DividerItemDecoration itemDecorator = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
            itemDecorator.setDrawable(mContext.getResources().getDrawable(R.drawable.divider));
            ((RanobeViewHolder) holder).mChaptersListView.addItemDecoration(itemDecorator);
            ((RanobeViewHolder) holder).mChaptersListView.setAdapter(adapter);

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        } else if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.mTextView.setText(mValues.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    void setLoaded() {
        isLoading = false;
    }

    void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        final ProgressBar progressBar;

        LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBar);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        final TextView mTextView;

        TitleViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.group_title);
        }
    }

    class RanobeViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mImageView;
        final RecyclerView mChaptersListView;
        private final TextView mTitleView;
        private final TextView mUpdateTime;
        Ranobe mItem;

        RanobeViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.ranobeTitle);
            mImageView = view.findViewById(R.id.imageView);
            mUpdateTime = view.findViewById(R.id.ranobeUpdateTime);
            mChaptersListView = view.findViewById(R.id.list_chapter_list);
          //  mChaptersListView.setHasFixedSize(true);
            mChaptersListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        }

    }

}
