package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.RanobeConstans.fragmentBundle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.Rulate.JsonRulateApi;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateBook;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateReadyGson;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RanobeRecyclerFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<Ranobe> ranobeList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OnListFragmentInteractionListener mListener;

    private RanobeRecyclerViewAdapter mRanobeRecyclerViewAdapter;
    private Context mContext;
    private RanobeConstans.FragmentType fragmentType;
    private int page;
    private boolean loadFromDatabase;
    ProgressDialog progressDialog;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RanobeRecyclerFragment() {
        page = 0;
        loadFromDatabase = true;
    }

    public static RanobeRecyclerFragment newInstance(String fragmentType) {
        RanobeRecyclerFragment fragment = new RanobeRecyclerFragment();
        Bundle args = new Bundle();
        args.putString(fragmentBundle, fragmentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (getArguments() != null && getArguments().containsKey(fragmentBundle)) {
            fragmentType = RanobeConstans.FragmentType.valueOf(
                    getArguments().getString(fragmentBundle));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranobe_list, container, false);

        // Set the adapter
        if (view instanceof SwipeRefreshLayout) {

            mContext = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.ranobeListView);

            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

            mRanobeRecyclerViewAdapter = new RanobeRecyclerViewAdapter(recyclerView, ranobeList,
                    fragmentType);
            recyclerView.setAdapter(mRanobeRecyclerViewAdapter);

            //set load more listener for the RecyclerView adapter
            if (fragmentType != RanobeConstans.FragmentType.Favorite
                    && fragmentType != RanobeConstans.FragmentType.Search) {

                mRanobeRecyclerViewAdapter.setOnLoadMoreListener(() -> {

                    ranobeList.add(null);

                    recyclerView.post(() -> {
                        mRanobeRecyclerViewAdapter.notifyItemInserted(ranobeList.size() - 1);
                    });

                    AsyncTask.execute(() -> {
                        ranobeList.remove(ranobeList.size() - 1);
                        recyclerView.post(() -> {
                            mRanobeRecyclerViewAdapter.notifyItemRemoved(ranobeList.size());
                        });

                        refreshItems(false);

                    });

                });
            }
            mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setOnRefreshListener(() -> {
                mSwipeRefreshLayout.setRefreshing(false);
                page = 0;
                refreshItems(true);
            });
            mSwipeRefreshLayout.setRefreshing(true);

            refreshItems(true);
        }

        return view;
    }

    private void refreshItems(boolean remove) {

        if (remove) {
            page = 0;
            int size = ranobeList.size();
            ranobeList.clear();

            mRanobeRecyclerViewAdapter.notifyDataSetChanged();

        }

        switch (fragmentType) {
            case Rulate:
                rulateLoadRanobe(remove);
                break;
            case Ranoberf:
                ranoberfLoadRanobe(remove);
                break;
            case Search:
            case Favorite:
                favoriteLoadRanobe(remove);
                break;
            default:
                break;
        }
    }

    private void onItemsLoadComplete(boolean remove) {

        mRanobeRecyclerViewAdapter.notifyDataSetChanged();

        mRanobeRecyclerViewAdapter.setLoaded();

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void favoriteLoadRanobe(final boolean remove) {
//        ProgressDialog progressDialog = ProgressDialog.show(mContext,
//                getResources().getString(R.string.load_ranobes),
//                getResources().getString(R.string.load_please_wait), true, true);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.load_please_wait));
        progressDialog.setTitle(getResources().getString(R.string.load_ranobes));
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mSwipeRefreshLayout.setRefreshing(false);
        progressDialog.show();

        new Thread() {

            @Override
            public void run() {

                List<Ranobe> newRanobeList = DatabaseDao.getInstance(
                        mContext).getRanobeDao().GetFavoriteRanobes();

                List<Ranobe> rulateWebList = getRulateWebFavorite();
                if (rulateWebList.size() > 0) {
                    Ranobe TitleRanobe = new Ranobe();
                    TitleRanobe.setTitle(getString(R.string.tl_rulate_web));
                    TitleRanobe.setRanobeSite(StringResources.Title_Site);
                    newRanobeList.add(TitleRanobe);
                }
                newRanobeList.addAll(rulateWebList);

                progressDialog.setMax(newRanobeList.size());

                for (Ranobe ranobe : newRanobeList) {
                    progressDialog.setMessage(ranobe.getTitle());
                    if (!loadFromDatabase) {
                        ranobe.updateRanobe(mContext);
                        AsyncTask.execute(() -> {

                            if (!ranobe.getFavoritedInWeb() && !ranobe.getRanobeSite().equals(
                                    StringResources.Title_Site)) {
                                DatabaseDao.getInstance(mContext).getRanobeDao().update(ranobe);
                                DatabaseDao.getInstance(mContext).getChapterDao().insertAll(
                                        ranobe.getChapterList().toArray(
                                                new Chapter[ranobe.getChapterList().size()]));

                            }

                        });
                    } else {
                        List<Chapter> chapterList = DatabaseDao.getInstance(
                                mContext).getChapterDao().getChaptersForRanobe(
                                ranobe.getUrl());
                        ranobe.setChapterList(chapterList);

                    }
                    ranobeList.add(ranobe);
                    progressDialog.incrementProgressBy(1);

                }

                loadFromDatabase = false;

                if (getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                progressDialog.dismiss();

            }

        }.start();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( progressDialog!=null && progressDialog.isShowing() ){
            progressDialog.cancel();
        }
    }

    private List<Ranobe> getRulateWebFavorite() {

        List<Ranobe> resultList = new ArrayList<>();
        SharedPreferences mPreferences = mContext.getSharedPreferences(
                StringResources.Rulate_Login_Pref, 0);

        String token = mPreferences.getString(StringResources.KEY_Token, "");
        if (!token.equals("")) {
            String response = JsonRulateApi.getInstance().GetFavoriteBooks(token);
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.get("status").equals("success")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("response");
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject value = jsonArray.getJSONObject(i);
                        Ranobe ranobe = new Ranobe();
                        ranobe.UpdateRanobe(value, RanobeConstans.JsonObjectFrom.RulateFavorite);

                        if (!loadFromDatabase) {
                            ranobe.updateRanobe(mContext);
                        }
                        ranobe.setFavoritedInWeb(true);
                        resultList.add(ranobe);

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
        return resultList;
    }

    private void rulateLoadRanobe(final boolean remove) {

        new Thread() {
            @Override
            public void run() {
                String response = JsonRulateApi.getInstance().GetReadyTranslatesHtml("",
                        String.valueOf(page + 1));

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                RulateReadyGson readyGson = gson.fromJson(response, RulateReadyGson.class);

                if(readyGson.getStatus().equals("success")){

                    for (RulateBook book:readyGson.getBooks()){
                        Ranobe ranobe = new Ranobe(book);
                        ranobeList.add(ranobe);
                    }
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.get("status").equals("success")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("response");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject value = jsonArray.getJSONObject(i);
                            Ranobe ranobe = new Ranobe();
                            ranobe.UpdateRanobe(value,
                                    RanobeConstans.JsonObjectFrom.RulateGetReady);
                            ranobeList.add(ranobe);

                        }
                    }

                    getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                    page++;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }.start();

    }

    private void ranoberfLoadRanobe(final boolean remove) {

        new Thread() {
            @Override
            public void run() {
                String response = JsonRanobeRfApi.getInstance().GetReadyBooks(page);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("status") == 200) {

                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject value = jsonArray.getJSONObject(i);
                            Ranobe ranobe = new Ranobe();
                            ranobe.UpdateRanobe(value,
                                    RanobeConstans.JsonObjectFrom.RanobeRfGetReady);
                            ranobeList.add(ranobe);

                        }

                    }

                    getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                    page++;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }

        }.start();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Ranobe item);
    }

}
