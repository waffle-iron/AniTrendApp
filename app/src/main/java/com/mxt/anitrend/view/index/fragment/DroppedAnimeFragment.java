package com.mxt.anitrend.view.index.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.viewmodel.fragment.UserListFragment;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Maxwell on 1/7/2017.
 */
public class DroppedAnimeFragment extends UserListFragment {

    public DroppedAnimeFragment() {
        // Required empty public constructor
    }

    public static UserListFragment newInstance(int id) {
        Bundle bundle = new Bundle();
        DroppedAnimeFragment mFragment = new DroppedAnimeFragment();
        bundle.putInt(ARG_KEY, id);
        mFragment.setArguments(bundle);
        return mFragment;
    }

    /**
     * Override if you need to include extra functionality into the method,
     * the method will get the arguments from the from your bundle and into
     * the model followed by initialization of your presenter
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mRequestType = AsyncTaskFetch.RequestType.USER_ANIME_LIST_REQ;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResponse(Call<User> call, Response<User> response) {
        if (isVisible() && (!isDetached() || !isRemoving()))
            if (response.isSuccessful() && response.body() != null)
                if(response.body().getLists() == null)
                    showEmpty();
                else
                    sortItems(response.body().getLists().getDropped());
            else
                showError(response);
    }
}