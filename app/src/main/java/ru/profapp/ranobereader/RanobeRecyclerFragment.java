package ru.profapp.ranobereader;

import static ru.profapp.ranobereader.Common.Constans.fragmentBundle;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.profapp.ranobereader.Common.Constans;
import ru.profapp.ranobereader.Common.EndlessRecyclerViewScrollListener;
import ru.profapp.ranobereader.DAO.Database;
import ru.profapp.ranobereader.Models.Ranobe;
import ru.profapp.ranobereader.RanobeRf.JsonRanobeRfApi;
import ru.profapp.ranobereader.Rulate.JsonRulateApi;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RanobeRecyclerFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    public List<Ranobe> ranobeList = new ArrayList<Ranobe>();
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    EndlessRecyclerViewScrollListener scrollListener;
    OnListFragmentInteractionListener mListener;

    RanobeRecyclerViewAdapter mRanobeRecyclerViewAdapter;
    Context mContext;
    private Constans.FragmentType fragmentType;
    private int page;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RanobeRecyclerFragment() {
        page = 0;
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
            fragmentType = Constans.FragmentType.valueOf(getArguments().getString(fragmentBundle));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranobe_list, container, false);

        // Set the adapter
        if (view instanceof SwipeRefreshLayout) {
            mContext = view.getContext();
            recyclerView = (RecyclerView) view.findViewById(R.id.ranobeListView);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

            scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    int a = 8;
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
            mRanobeRecyclerViewAdapter = new RanobeRecyclerViewAdapter(ranobeList, mListener,fragmentType);
            recyclerView.setAdapter(mRanobeRecyclerViewAdapter);

            mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    refreshItems();
                }
            });
        }

        return view;
    }

    void refreshItems() {
        ranobeList.clear();
        switch (fragmentType) {
            case Rulate:
                rulateLoadRanobe();
                break;
            case Ranoberf:
                ranoberfLoadRanobe();
                break;
            case Search:
            case Favorite:
                favoriteLoadRanobe();
                break;
            default:
                break;
        }
    }

    void onItemsLoadComplete(boolean remove) {
        recyclerView.swapAdapter(mRanobeRecyclerViewAdapter, remove);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void favoriteLoadRanobe() {

        new Thread() {
            @Override
            public void run() {

                ranobeList.addAll(Database.getInstance(mContext).getRanobeDao().GetFavoriteRanobes());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onItemsLoadComplete(false);
                    }
                });

            }

        }.start();

    }

    void rulateLoadRanobe() {

        new Thread() {
            @Override
            public void run() {
                String response = JsonRulateApi.getInstance().GetReadyTranslatesHtml("", "1");
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.get("status").equals("success")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("response");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject value = jsonArray.getJSONObject(i);
                            Ranobe ranobe = new Ranobe();
                            ranobe.UpdateRanobe(value, Constans.JsonObjectFrom.RulateGetReady);
                            ranobeList.add(ranobe);

                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete(false);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    void ranoberfLoadRanobe() {

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
                            ranobe.UpdateRanobe(value, Constans.JsonObjectFrom.RanobeRfGetReady);
                            ranobeList.add(ranobe);

                        }

                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete(false);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
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
