
package com.nsdeveloper.musific.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nsdeveloper.musific.R;
import com.nsdeveloper.musific.activities.BaseActivity;
import com.nsdeveloper.musific.activities.MainActivity;
import com.nsdeveloper.musific.adapters.SongsListAdapter;
import com.nsdeveloper.musific.dataloaders.SongLoader;
import com.nsdeveloper.musific.listeners.MusicStateListener;
import com.nsdeveloper.musific.models.Song;
import com.nsdeveloper.musific.utils.PreferencesUtility;
import com.nsdeveloper.musific.utils.SortOrder;
import com.nsdeveloper.musific.widgets.BaseRecyclerView;
import com.nsdeveloper.musific.widgets.DividerItemDecoration;
import com.nsdeveloper.musific.widgets.FastScroller;

import java.util.List;

public class SongsFragment extends Fragment implements MusicStateListener {

    private int count = 0;
    private int check = 0;
    MainActivity parent;
    private  AdRequest adRequest;
    private  AdView mAdView;
    private SongsListAdapter mAdapter;
    private BaseRecyclerView recyclerView;
    private PreferencesUtility mPreferences;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent=(MainActivity) getActivity();
        mPreferences = PreferencesUtility.getInstance(getActivity());
        MobileAds.initialize(getActivity(), "ca-app-pub-9371521457039747~5912092116");
//        mInterstitialAd = new InterstitialAd(getActivity());
//        mInterstitialAd.setAdUnitId("ca-app-pub-9371521457039747/6662590164");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false);

        recyclerView =  rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No media found");
        FastScroller fastScroller = (FastScroller) rootView.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(recyclerView);
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());




        mAdView = (AdView) rootView.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();


        mAdView.setAdListener(new AdListener() {
          @Override
          public void onAdLoaded() {
              super.onAdLoaded();
              mAdView.setVisibility(View.VISIBLE);
              int marginInDp = parent.getResources().getDimensionPixelSize(R.dimen.space_for_ads);
              ViewGroup.MarginLayoutParams marginLayoutParams =
                      (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
              marginLayoutParams.setMargins(0,marginInDp,0,0);
              recyclerView.setLayoutParams(marginLayoutParams);

          }
      });


      mAdView.loadAd(adRequest);


        new loadSongs().execute("");
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);




        return rootView;
    }





    @Override
    public void onResume() {
        super.onResume();

//        check = getActivity().getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE).getInt("show_once",0);
//        if(check%5 == 0){
//
//            if (mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//                count++;
//                getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
//                        .edit()
//                        .putInt("show_once",count).commit();
//                        }
//        }else{
//            count++;
//            getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
//                    .edit()
//                    .putInt("show_once",count).commit();
//
//        }

    }

    public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<Song> songList = SongLoader.getAllSongs(getActivity());
                mAdapter.updateDataSet(songList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_artist:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_album:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_duration:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                reloadAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null)
                mAdapter = new SongsListAdapter((AppCompatActivity) getActivity(), SongLoader.getAllSongs(getActivity()), false, false);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            if (getActivity() != null)
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        }

        @Override
        protected void onPreExecute() {
        }
    }
}