package com.daninic9.ubahnstations;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

//        The app presents the user with a list of UBahn stations (underground stations in Germany).
//        The list shows the name, lat, lon and a small image of each station.
//        The user can filter the list using a string filter at the top of the list.
//        Spinners are shown when the user executes a filter search (this an asynchronous request to the API).
//        You can use the publicly available Graphql API at:
//        https://bahnql.herokuapp.com/graphql


public class MainActivity extends AppCompatActivity {

    private StationCustomAdapter stationCustomAdapter;
    private RecyclerView stationsRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GraphQlManager graphQlManager;

    private static final boolean IS_FILTERED = false;

    private static final List<GetStationInfoQuery.StationWithEvaId> stationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLogger();
        declareViews();
        graphQlManager = new GraphQlManager(this);
        initInfoFetch();
    }

    private void initLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .tag("UbahnStationsLogger")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.i("------------------ STARTING APP ------------------");
    }
    private void initInfoFetch() {
        blockScreen(true);
        graphQlManager.queryGetAll();
    }

    private void declareViews() {
        stationsRecycler = findViewById(R.id.stations_recycler);
        stationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeContainer_stations);
        setViewEvents();
    }
    private void setViewEvents() {
        stationsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing()) {
                    Logger.i("Load Next 10");
                    blockScreen(true);
                    graphQlManager.addMore();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!IS_FILTERED) {
                if (!stationList.isEmpty()) {
                    stationList.clear();
                    graphQlManager.cleanList();
                }
                Logger.i("Refresh");
                initInfoFetch();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        findViewById(R.id.button_goup).setOnClickListener(v -> stationsRecycler.smoothScrollToPosition(0));
    }

    private void blockScreen(boolean lock) {
        swipeRefreshLayout.setRefreshing(lock);
        stationsRecycler.suppressLayout(lock);
    }

    public void sendWarningError(String title, String message) {
        blockScreen(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(getResources().getString(R.string.ok), null);

        runOnUiThread(() -> {
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void infoUpdate(boolean isAdding) {
        if(!stationList.isEmpty()) {
            if (isAdding) {
                runOnUiThread(() -> {
                    stationCustomAdapter.notifyDataSetChanged();
                    blockScreen(false);
                });
            } else {
                runOnUiThread(() -> {
                    stationCustomAdapter = new StationCustomAdapter(stationList, this);
                    stationCustomAdapter.setClickListener((view, position) -> {
                        if (!swipeRefreshLayout.isRefreshing()) {
                            Logger.i("Clicked " + stationCustomAdapter.getItem(position).name);
                        }
                    });
                    stationsRecycler.setAdapter(stationCustomAdapter);
                    blockScreen(false);
                });
            }
        }
    }

    public static List<GetStationInfoQuery.StationWithEvaId> getStationList() {
        return stationList;
    }
}
