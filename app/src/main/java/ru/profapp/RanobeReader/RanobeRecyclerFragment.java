package ru.profapp.RanobeReader;

import static android.content.Context.MODE_PRIVATE;

import static ru.profapp.RanobeReader.Common.RanobeConstans.fragmentBundle;
import static ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref;
import static ru.profapp.RanobeReader.Models.Ranobe.empty;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfBook;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfGetReadyGson;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateBook;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateReadyGson;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.Models.TextChapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RanobeRecyclerFragment extends Fragment {

    private final Gson gson = new GsonBuilder().setLenient().create();
    ProgressDialog progressDialog;
    private List<Ranobe> ranobeList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OnListFragmentInteractionListener mListener;
    private RanobeRecyclerViewAdapter mRanobeRecyclerViewAdapter;
    private Context mContext;

    private Thread.UncaughtExceptionHandler ExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    MyLog.SendError(StringResources.LogType.WARN, "RanobeRecyclerFragment",
                            "Uncaught exception", ex);
                    progressDialog.dismiss();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            };
    private RanobeConstans.FragmentType fragmentType;
    private int page;
    private boolean loadFromDatabase;

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

            mRanobeRecyclerViewAdapter = new RanobeRecyclerViewAdapter(recyclerView, ranobeList );
            Drawable downloadDoneImage = mContext.getResources().getDrawable(
                    R.drawable.ic_cloud_done_black_24dp);
            mRanobeRecyclerViewAdapter.setDownloadDoneImage(downloadDoneImage);
            recyclerView.setAdapter(mRanobeRecyclerViewAdapter);

            //set load more listener for the RecyclerView adapter
            if (fragmentType != RanobeConstans.FragmentType.Favorite
                    && fragmentType != RanobeConstans.FragmentType.Search && fragmentType != RanobeConstans.FragmentType.History){

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
            case Favorite:
                favoriteLoadRanobe(remove);
                break;
            case History:
                HistoryLoadRanobe(remove);
                break;
            default:
               throw new NullPointerException();

        }

    }

    private void onItemsLoadComplete(boolean remove) {

        for (Ranobe ranobe : ranobeList) {

            if (ranobe != null) {
                SharedPreferences sPref = mContext.getSharedPreferences(
                        is_readed_Pref,
                        MODE_PRIVATE);
                if (sPref != null) {
                    Object[] allReadedChapters = sPref.getAll().keySet().toArray();

                    for (Chapter chapter : ranobe.getChapterList()) {
                        if (!chapter.getReaded()) {

                            for (Object readed : allReadedChapters) {

                                if (chapter.getUrl().equals(readed.toString())) {
                                    chapter.setReaded(true);
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        }
        mRanobeRecyclerViewAdapter.notifyDataSetChanged();

        mRanobeRecyclerViewAdapter.setLoaded();

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void onItemsLoadFailed() {
        Toast.makeText(mContext, "Error connection", Toast.LENGTH_SHORT).show();
        mRanobeRecyclerViewAdapter.setLoaded();

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void favoriteLoadRanobe(final boolean remove) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.load_please_wait));
        progressDialog.setTitle(getResources().getString(R.string.load_ranobes));
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mSwipeRefreshLayout.setRefreshing(false);
        progressDialog.show();

        Thread t = new Thread() {

            @Override
            public void run() {

                int step = 0;

                List<Ranobe> newRanobeList = new ArrayList<>();

                List<Ranobe> favRanobeList = DatabaseDao.getInstance(
                        mContext).getRanobeDao().GetFavoriteBySite(StringResources.Rulate_Site);
                if (favRanobeList.size() > 0) {
                    Ranobe TitleRanobe = new Ranobe();
                    TitleRanobe.setTitle(getString(R.string.tl_rulate_name));
                    TitleRanobe.setRanobeSite(StringResources.Title_Site);
                    newRanobeList.add(TitleRanobe);
                    newRanobeList.addAll(favRanobeList);
                    step--;
                }

                favRanobeList = DatabaseDao.getInstance(
                        mContext).getRanobeDao().GetFavoriteBySite(StringResources.RanobeRf_Site);
                if (favRanobeList.size() > 0) {
                    Ranobe TitleRanobe = new Ranobe();
                    TitleRanobe.setTitle(getString(R.string.ranobe_rf));
                    TitleRanobe.setRanobeSite(StringResources.Title_Site);
                    newRanobeList.add(TitleRanobe);
                    newRanobeList.addAll(favRanobeList);
                    step--;
                }

                List<Ranobe> rulateWebList = getRulateWebFavorite();
                if (rulateWebList.size() > 0) {
                    Ranobe TitleRanobe = new Ranobe();
                    TitleRanobe.setTitle(mContext.getString(R.string.tl_rulate_web));
                    TitleRanobe.setRanobeSite(StringResources.Title_Site);
                    newRanobeList.add(TitleRanobe);
                    step--;
                }
                newRanobeList.addAll(rulateWebList);

                progressDialog.setMax(newRanobeList.size() + step);

                for (Ranobe ranobe : newRanobeList) {

                    boolean error = false;
                    // Todo:
                    getActivity().runOnUiThread(() -> progressDialog.setMessage(ranobe.getTitle()));

                    if (!loadFromDatabase) {
                        try {
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
                        } catch (NullPointerException e) {
                            MyLog.SendError(StringResources.LogType.WARN, "RanobeRecyclerFragment",
                                    "", e);
                            error = true;
                            getActivity().runOnUiThread(
                                    () -> Toast.makeText(mContext, "Error connection",
                                            Toast.LENGTH_SHORT).show());
                        }

                    }

                    if (loadFromDatabase || error) {
                        List<Chapter> chapterList = DatabaseDao.getInstance(
                                mContext).getChapterDao().getChaptersForRanobe(
                                ranobe.getUrl());
                        ranobe.setChapterList(chapterList);

                    }

                    ranobeList.add(ranobe);
                    if (!ranobe.getRanobeSite().equals(StringResources.Title_Site)) {
                        progressDialog.incrementProgressBy(1);
                    }

                }

                loadFromDatabase = false;

                if (getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                progressDialog.dismiss();

            }

        };
        t.setUncaughtExceptionHandler(ExceptionHandler);
        t.start();

    }
    private void HistoryLoadRanobe(final boolean remove) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.load_please_wait));
        progressDialog.setTitle(getResources().getString(R.string.load_ranobes));
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mSwipeRefreshLayout.setRefreshing(false);
        progressDialog.show();

        Thread t = new Thread() {

            @Override
            public void run() {

                List<TextChapter> mChapterTexts = DatabaseDao.getInstance(mContext).getTextDao().getAllText();
                List<Ranobe> newRanobeList = new ArrayList<>();
                String prevRanobeName = "";
                Ranobe newRanobe =new Ranobe() ;
                List<Chapter> chapterList = new ArrayList<>();
                for (TextChapter mChapterText:mChapterTexts ) {


                    if(!mChapterText.getRanobeName().equals(prevRanobeName)){

                        prevRanobeName = mChapterText.getRanobeName();
                        newRanobe = new Ranobe();
                        chapterList = new ArrayList<>();
                        newRanobe.setTitle(mChapterText.getRanobeName());
                        newRanobe.setChapterList(chapterList);

                        newRanobeList.add(newRanobe);

                    }
                    Chapter chapter = new Chapter();
                    chapter.setRanobeName(mChapterText.getRanobeName());
                    chapter.setTitle(mChapterText.getChapterName());
                    chapter.setText(mChapterText.getText());
                    chapter.setUrl(mChapterText.getChapterUrl());
                    if( empty(newRanobe.getUrl()))
                        newRanobe.setUrl(chapter.getRanobeUrl());
                    chapterList.add(chapter);
                }




                List<Ranobe> rulateList = new ArrayList<>();
                List<Ranobe> ranobeRfList = new ArrayList<>();
                for (Ranobe ranobe:newRanobeList ) {
                    if(ranobe.getRanobeSite().equals(StringResources.RanobeRf_Site)) {
                        ranobeRfList.add(ranobe);
                    }else if(ranobe.getRanobeSite().equals(StringResources.Rulate_Site)) {
                        rulateList.add(ranobe);
                    }
                }
                if(ranobeRfList.size()>0){
                    Ranobe TitleRanobe = new Ranobe();
                    TitleRanobe.setTitle(getString(R.string.ranobe_rf));
                    TitleRanobe.setRanobeSite(StringResources.Title_Site);
                    ranobeList.add(TitleRanobe);
                    ranobeList.addAll(ranobeRfList);
                }
                if(rulateList.size()>0){
                    Ranobe TitleRanobe = new Ranobe();
                    TitleRanobe.setTitle(getString(R.string.tl_rulate_name));
                    TitleRanobe.setRanobeSite(StringResources.Title_Site);
                    ranobeList.add(TitleRanobe);
                    ranobeList.addAll(rulateList);
                }

                getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                progressDialog.dismiss();

            }

        };
        t.setUncaughtExceptionHandler(ExceptionHandler);
        t.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
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
                            ranobe.setFavoritedInWeb(true);
                            DatabaseDao.getInstance(mContext).getRanobeDao().insert(ranobe);
                            DatabaseDao.getInstance(mContext).getChapterDao().insertAll(
                                    ranobe.getChapterList().toArray(
                                            new Chapter[ranobe.getChapterList().size()]));
                        }else{
                            Ranobe dbRanobe =DatabaseDao.getInstance(mContext).getRanobeDao().getRanobeByUrl(ranobe.getUrl());
                            if(dbRanobe!=null)
                                ranobe =dbRanobe;
                        }
                        ranobe.setFavoritedInWeb(true);
                        resultList.add(ranobe);

                    }

                }
            } catch (JSONException | NullPointerException e) {
                MyLog.SendError(StringResources.LogType.WARN, "RanobeRecyclerFragment", "", e);
                getActivity().runOnUiThread(() -> onItemsLoadFailed());
            }

        }
        return resultList;
    }

    private void rulateLoadRanobe(final boolean remove) {

        Thread t = new Thread() {
            @Override
            public void run() {
                String response = JsonRulateApi.getInstance().GetReadyTranslates("",
                        String.valueOf(page + 1));

                try {
                    RulateReadyGson readyGson = gson.fromJson(response, RulateReadyGson.class);
                    if (readyGson.getStatus().equals("success")) {

                        for (RulateBook book : readyGson.getBooks()) {
                            Ranobe ranobe = new Ranobe();
                            ranobe.UpdateRulateRanobe(book);
                            ranobeList.add(ranobe);
                        }
                    }

                    getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                    page++;
                } catch (JsonParseException | NullPointerException e) {
                    MyLog.SendError(StringResources.LogType.WARN, "RanobeRecyclerFragment", "", e);
                    getActivity().runOnUiThread(() -> onItemsLoadFailed());
                }

            }
        };
        t.setUncaughtExceptionHandler(ExceptionHandler);
        t.start();

    }

    private void ranoberfLoadRanobe(final boolean remove) {

        Thread t = new Thread() {
            @Override
            public void run() {
                String response = JsonRanobeRfApi.getInstance().GetReadyBooks(page);
                try {
                    RfGetReadyGson readyGson = gson.fromJson(response, RfGetReadyGson.class);

                    if (readyGson.getStatus() == 200) {
                        for (RfBook value : readyGson.getResult()) {

                            Ranobe ranobe = new Ranobe();
                            ranobe.UpdateRanobeRfRanobe(value);
                            ranobeList.add(ranobe);

                        }
                    }

                    getActivity().runOnUiThread(() -> onItemsLoadComplete(remove));

                    page++;
                } catch (JsonParseException | NullPointerException e) {
                    MyLog.SendError(StringResources.LogType.WARN,
                            RanobeRecyclerFragment.class.toString(), "", e);
                    getActivity().runOnUiThread(() -> onItemsLoadFailed());

                }

            }

        };
        t.setUncaughtExceptionHandler(ExceptionHandler);
        t.start();

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

        void onListFragmentInteraction(Ranobe item);
    }

}
