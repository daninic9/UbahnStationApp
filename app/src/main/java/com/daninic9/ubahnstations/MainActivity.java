package com.daninic9.ubahnstations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.daninic9.ubahnstations.stations.StationCustomAdapter;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

//        The app presents the user with a list of UBahn stations (underground stations in Germany).
//        The list shows the name, lat, lon and a small image of each station.
//        The user can filter the list using a string filter at the top of the list.
//        Spinners are shown when the user executes a filter search (this an asynchronous request to the API).
//        You can use the publicly available Graphql API at:
//        https://bahnql.herokuapp.com/graphql


public class MainActivity extends AppCompatActivity {

    private StationCustomAdapter stationCustomAdapter;
    private RecyclerView stations_recycler;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GraphQlManager graphQlManager;

    private boolean isFiltered = false;
    private Parcelable recyclerViewState;

    public static List<GetStationInfoQuery.StationWithEvaId> stationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .tag("UbahnStationsLogger")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.i("------------------ STARTING APP ------------------");

        declareViews();

        Logger.i("Init");
        blockScreen(true);
        graphQlManager = new GraphQlManager(this);
        graphQlManager.queryGetAll();
    }

    private void declareViews() {
        stations_recycler = findViewById(R.id.stations_recycler);
        stations_recycler.setLayoutManager(new LinearLayoutManager(this));
        stations_recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing()) {
                    Logger.i("Load Next 10");
                    blockScreen(true);
                    recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
                    graphQlManager.addMore();
                }
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeContainer_stations);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isFiltered) {
                if (!stationList.isEmpty()) {
                    stationList.clear();
                    graphQlManager.cleanList();
                }
                Logger.i("Refresh");
                graphQlManager.queryGetAll();
                blockScreen(true);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public void blockScreen(boolean lock) {
        swipeRefreshLayout.setRefreshing(lock);
        stations_recycler.setEnabled(!lock);
    }

    public void infoUpdate() {
        if(!stationList.isEmpty()) {
            runOnUiThread(() -> {
                stationCustomAdapter = new StationCustomAdapter(stationList, this);
                stationCustomAdapter.setClickListener((view, position) -> {
                    if (!swipeRefreshLayout.isRefreshing()) {
                        Logger.i("Clicked " + stationCustomAdapter.getItem(position).name);
                    }
                });
                stations_recycler.setAdapter(stationCustomAdapter);
                blockScreen(false);
                Objects.requireNonNull(stations_recycler.getLayoutManager()).onRestoreInstanceState(recyclerViewState);
            });
        }
    }

    //TODO
    public void sendWarningError() {

    }
}
