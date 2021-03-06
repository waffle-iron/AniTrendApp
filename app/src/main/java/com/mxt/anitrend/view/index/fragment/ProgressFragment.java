package com.mxt.anitrend.view.index.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.ProgressAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.view.StatefulRecyclerView;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.event.RecyclerLoadListener;
import com.mxt.anitrend.presenter.index.UserActivityPresenter;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.FilterProvider;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;
import com.mxt.anitrend.view.detail.activity.MangaActivity;
import com.mxt.anitrend.view.detail.activity.UserReplyActivity;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressFragment extends Fragment implements Callback<List<UserActivity>>, SwipeRefreshLayout.OnRefreshListener, MultiInteractionListener, RecyclerLoadListener {

    @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    private int mPage = 1;
    private boolean isLimit;
    private List<UserActivity> mData;

    private static final String CONTENT_DESCRIPTION = "model_type";

    private final String MODEL_LIMIT = "loading_limit_reached";
    private final String MODEL_PAGE = "current_page_number";
    private final String MODEL_CACHE = "model_list";
    private final String MODEL_TYPE = "model_type";

    private String model_type;

    private GridLayoutManager mLayoutManager;

    private Unbinder unbinder;

    private List<Integer> skipIds = new ArrayList<>();


    private UserActivityPresenter mPresenter;

    private RecyclerViewAdapter<UserActivity> mAdapter;

    public ProgressFragment() {
        // Required empty public constructor
    }

    public static ProgressFragment newInstance(String type) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT_DESCRIPTION, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model_type = getArguments().getString(CONTENT_DESCRIPTION);
        skipIds.add(R.id.generic_recycler);
        mPresenter = new UserActivityPresenter(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View views = inflater.inflate(R.layout.fragment_base_view, container, false);
        unbinder = ButterKnife.bind(this,views);
        progressLayout.showLoading();
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false);//set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
        recyclerView.setLayoutManager(mLayoutManager);
        if(savedInstanceState != null)
            model_type = savedInstanceState.getString(MODEL_TYPE);
        return views;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mData == null)
            mPresenter.beginAsync(this, mPage, model_type);
        else
            showData();
    }

    private void addScrollLoadTrigger() {
        if(!recyclerView.hasOnScrollListener()) {
            mPresenter.initListener(mLayoutManager, mPage, this);
            recyclerView.addOnScrollListener(mPresenter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onPause() {
        super.onPause();
        recyclerView.clearOnScrollListeners();
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
        addScrollLoadTrigger();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(VIEW_PARCABLE, recyclerView.onSaveInstanceState());
        outState.putString(MODEL_TYPE, model_type);
        if(mData != null && mData.size() < 20) {
            outState.putInt(MODEL_PAGE, mPage);
            outState.putBoolean(MODEL_LIMIT, isLimit);
            outState.putParcelableArrayList(MODEL_CACHE, (ArrayList<? extends Parcelable>) mData);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            //recyclerView.onRestoreInstanceState(savedInstanceState.getParcelable(VIEW_PARCABLE));
            mPage = savedInstanceState.getInt(MODEL_PAGE);
            isLimit = savedInstanceState.getBoolean(MODEL_LIMIT);
            mData = savedInstanceState.getParcelableArrayList(MODEL_CACHE);
        }
    }

    private void showData() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new ProgressAdapter(mData, getContext(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs(), this);
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(mData);
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
                //addScrollLoadTrigger();
            } else
                progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), "No results found", skipIds);
        }
    }

    @Override
    public void onResponse(Call<List<UserActivity>> call, Response<List<UserActivity>> response) {
        if(isVisible() && !isRemoving() || !isDetached()) {
            if (response.body() != null && response.body().size() > 0) {
                if (mData == null)
                    mData = FilterProvider.getUserActivityFilter(mPresenter.getCurrentUser().getId(), response.body());
                else
                    mData.addAll(FilterProvider.getUserActivityFilter(mPresenter.getCurrentUser().getId(), response.body()));
            } else if (response.isSuccessful() && response.body() != null && mPage != 1) {
                isLimit = true;
            }
            showData();
        }
    }

    @Override
    public void onFailure(Call<List<UserActivity>> call, Throwable t) {
        if(isVisible() && !isRemoving() || !isDetached())
            progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.try_again), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressLayout.showLoading();
                    mPresenter.beginAsync(ProgressFragment.this, mPage, model_type);
                }
            });
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mPage = 1;
        mData = null;
        mPresenter.onRefreshPage();
        mPresenter.beginAsync(this, mPage, model_type);
    }

    @Override
    public void onItemClick(final int index, int viewId) {
        Intent intent;
        final UserActivity mItem = mData.get(index);
        switch (viewId) {
            case R.id.feed_avatar:
                intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mItem.getUsers().get(0));
                startActivity(intent);
                break;
            case R.id.feed_series_img:
                // Open the clicked series
                Series series = mItem.getSeries();
                if(series.getSeries_type().equals(FilterTypes.SeriesTypes[FilterTypes.SeriesType.ANIME.ordinal()])) {
                    intent = new Intent(getActivity(), AnimeActivity.class);
                    intent.putExtra(AnimeActivity.MODEL_ID_KEY, series.getId());
                    intent.putExtra(AnimeActivity.MODEL_BANNER_KEY, series.getImage_url_banner());
                    startActivity(intent);
                } else {
                    intent = new Intent(getActivity(), MangaActivity.class);
                    intent.putExtra(MangaActivity.MODEL_ID_KEY, series.getId());
                    intent.putExtra(MangaActivity.MODEL_BANNER_KEY, series.getImage_url_banner());
                    startActivity(intent);
                }
                break;
            case R.id.feed_comment:
                // Open comment activity
                intent = new Intent(getActivity(), UserReplyActivity.class);
                intent.putExtra(UserReplyActivity.USER_ACTIVITY_EXTRA, mItem);
                startActivity(intent);
                break;
            case R.id.mFlipper:
                // Like the feed
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(mItem.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (isVisible() && !isRemoving() || !isDetached()) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<UserSmall> mItemLikes = mItem.getLikes();

                                if(mItemLikes.contains(mPresenter.getCurrentUser()))
                                    mItemLikes.remove(mPresenter.getCurrentUser());
                                else
                                    mItemLikes.add(mPresenter.getCurrentUser());
                                mData.set(index, mItem);
                                mAdapter.onItemChanged(mData, index);
                            } else {
                                Toast.makeText(getContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                mAdapter.refreshItem(index);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        if(isVisible() && !isRemoving() || !isDetached())
                            try {
                                Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                mAdapter.refreshItem(index);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }, FilterTypes.ActionType.ACTIVITY_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
        }
    }

    @Override
    public void onLoadMore(int currentPage) {
        if(!isLimit) {
            mPage = currentPage;
            swipeRefreshLayout.setRefreshing(true);
            mPresenter.beginAsync(this, mPage, model_type);
        }
    }
}