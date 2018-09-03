package ru.profapp.RanobeReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.profapp.RanobeReader.Common.ErrorConnectionException;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeHubApi;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.Models.Ranobe;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private RanobeRecyclerViewAdapter mRanobeRecyclerViewAdapter;
    private OnFragmentInteractionListener mListener;
    private List<Ranobe> mRanobeList;
    private Context mContext;
    private TextView resultLabel;

    public SearchFragment() {

    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRanobeList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mContext = getContext();

        SearchView simpleSearchView = view.findViewById(R.id.search);

        resultLabel = view.findViewById(R.id.search_result_label);

        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                resultLabel.setVisibility(View.GONE);

                ProgressDialog progressDialog = new ProgressDialog(mContext);

                progressDialog.setTitle(getResources().getString(R.string.load_please_wait));
                progressDialog.setCancelable(true);
                progressDialog.show();

                findRanobe(query);

                if (mRanobeList.size() == 0) {
                    resultLabel.setVisibility(View.VISIBLE);
                }
                mRanobeRecyclerViewAdapter.notifyItemRangeInserted(0, mRanobeList.size());
                recyclerView.scrollToPosition(0);
                progressDialog.dismiss();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        recyclerView = view.findViewById(R.id.ranobeListView);

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mRanobeRecyclerViewAdapter = new RanobeRecyclerViewAdapter(recyclerView, mRanobeList);
        recyclerView.setAdapter(mRanobeRecyclerViewAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void findRanobe(String searchString) {
        int size = mRanobeList.size();
        mRanobeList.clear();
        mRanobeRecyclerViewAdapter.notifyItemRangeRemoved(0, size);
        try {
            searchString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            MyLog.SendError(StringResources.LogType.WARN, SearchFragment.class.toString(), "", e);
        }

        try {
          //  findRanoberfRanobe(searchString);
        //    findRulateRanobe(searchString);
            findRanobeHubRanobe(searchString);
        } catch (ErrorConnectionException e) {
            Objects.requireNonNull(getActivity()).runOnUiThread(
                    () -> Toast.makeText(mContext,
                            getString(R.string.ErrorConnection),
                            Toast.LENGTH_SHORT).show());
        }

    }

    private void findRulateRanobe(String searchString) throws ErrorConnectionException {
        List<Ranobe> ranobes = new ArrayList<>();
        try {
            String response = JsonRulateApi.getInstance().SearchBooks(searchString);

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {

                JSONArray jsonArray = jsonObject.getJSONArray("response");
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject value = jsonArray.getJSONObject(i);
                    Ranobe ranobe = new Ranobe();
                    ranobe.UpdateRanobe(value, RanobeConstans.JsonObjectFrom.RulateSearch);
                    ranobes.add(ranobe);

                }

            }
        } catch (JSONException e) {
            MyLog.SendError(StringResources.LogType.WARN, SearchFragment.class.toString(), "", e);

        }

        if (ranobes.size() > 0) {
            Ranobe ranobet = new Ranobe();
            ranobet.setRanobeSite(StringResources.Title_Site);
            ranobet.setTitle(this.getString(R.string.tl_rulate_name));
            mRanobeList.add(ranobet);

            mRanobeList.addAll(ranobes);
        }

    }

    private void findRanobeHubRanobe(String searchString) throws ErrorConnectionException {
        List<Ranobe> ranobes = new ArrayList<>();
        try {
            String response = JsonRanobeHubApi.getInstance().SearchBooks(searchString);

            JSONObject jsonObject = new JSONObject(response);


                JSONArray jsonArray = jsonObject.getJSONObject("categories").getJSONObject("ranobe").getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject value = jsonArray.getJSONObject(i);
                    Ranobe ranobe = new Ranobe();
                    ranobe.UpdateRanobe(value, RanobeConstans.JsonObjectFrom.RanobeHubSearch);
                    ranobes.add(ranobe);

                }


        } catch (JSONException e) {
            MyLog.SendError(StringResources.LogType.WARN, SearchFragment.class.toString(), "", e);

        }

        if (ranobes.size() > 0) {
            Ranobe ranobet = new Ranobe();
            ranobet.setRanobeSite(StringResources.Title_Site);
            ranobet.setTitle(this.getString(R.string.ranobe_rf));
            mRanobeList.add(ranobet);

            mRanobeList.addAll(ranobes);
        }

    }

    private void findRanoberfRanobe(String searchString) throws ErrorConnectionException {
        List<Ranobe> ranobes = new ArrayList<>();
        try {
            String response = JsonRanobeRfApi.getInstance().SearchBooks(searchString);

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 200) {

                JSONArray jsonArray = jsonObject.getJSONArray("result");
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject value = jsonArray.getJSONObject(i);
                    Ranobe ranobe = new Ranobe();
                    ranobe.UpdateRanobe(value, RanobeConstans.JsonObjectFrom.RanobeRfSearch);
                    ranobes.add(ranobe);

                }

            }
        } catch (JSONException e) {
            MyLog.SendError(StringResources.LogType.WARN, SearchFragment.class.toString(), "", e);

        }

        if (ranobes.size() > 0) {
            Ranobe ranobet = new Ranobe();
            ranobet.setRanobeSite(StringResources.Title_Site);
            ranobet.setTitle(this.getString(R.string.ranobe_rf));
            mRanobeList.add(ranobet);

            mRanobeList.addAll(ranobes);
        }

    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

    }

}

