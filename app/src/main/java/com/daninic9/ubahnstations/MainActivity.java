package com.daninic9.ubahnstations;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The app presents the user with a list of UBahn stations (underground stations in Germany).
 * The list shows the name, lat, lon and a small image of each station.
 * The user can filter the list using a string filter at the top of the list.
 * Spinners are shown when the user executes a filter search (this an asynchronous request to the
 * API).
 * You can use the publicly available Graphql API at:
 * https://bahnql.herokuapp.com/graphql
 */
public class MainActivity extends AppCompatActivity {

    /* Adapter to manage station items views */
    private StationCustomAdapter stationCustomAdapter;

    /* Parent view of recycler that allows swipe down event */
    private SwipeRefreshLayout swipeRefreshLayout;

    /* View to visualize station List */
    private RecyclerView stationsRecycler;

    /* Class to manage GraphQl queries */
    private GraphQlManager graphQlManager;

    private SearchView searchStation;

    /* Flag to control if list is being filtered */
    private boolean isFiltered = false;

    /**
     * Flag to keep track if a filter was done so when user closes search, it refreshes
     */
    private boolean needsRefreshList = false;

    private boolean isRefreshing = false;

    /* List that contains stations being currently shown */
    private static final List<GetStationInfoQuery.StationWithEvaId> stationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLogger();
        declareViews();
        graphQlManager = new GraphQlManager(this);
        initInfoFetch("");
    }

    private void initLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .tag("UbahnStationsLogger")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.i("------------------ STARTING APP ------------------");
    }

    private void initInfoFetch(String search) {
        blockScreen(true);
        graphQlManager.queryGetAll(search);
    }

    private void declareViews() {
        stationsRecycler = findViewById(R.id.stations_recycler);
        stationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeContainer_stations);
        searchStation = findViewById(R.id.searchStation);
        setViewEvents();
    }

    private void setViewEvents() {
        stationsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !isRefreshing) {
                    if (graphQlManager.getStationsShown() < graphQlManager.getIdList().size()) {
                        Logger.i("Loading more...");
                        blockScreen(true);
                        graphQlManager.addMore();
                    } else {
                        Logger.i("End of the list reached, can't load more.");
                    }
                }
            }

        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isFiltered) {
                Logger.i("Refresh");
                initInfoFetch("");
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        findViewById(R.id.button_goup).setOnClickListener(v ->
                stationsRecycler.smoothScrollToPosition(0));

        searchStation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                isFiltered = !query.isEmpty();
                needsRefreshList = true;
                Logger.d("Searching: " + query);
                initInfoFetch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchStation.setOnCloseListener(() -> {
            isFiltered = false;
            if(needsRefreshList) {
                needsRefreshList = false;
                initInfoFetch("");
            }
            return false;
        });
    }

    private synchronized void blockScreen(boolean lock) {
        isRefreshing = lock;
        swipeRefreshLayout.setRefreshing(lock);
        stationsRecycler.suppressLayout(lock);
    }

    /**
     * Sends a pop up on screen to notify user.
     *
     * @param title pop up title text.
     * @param message pop up content message text.
     */
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

    /**
     * Sends a toast to notify the user
     * @param message message so show on the toast
     */
    public void sendToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            blockScreen(false);
        });
    }

    /**
     * Fetches new station info and sets the list with updated items
     */
    public void infoUpdate(boolean last) {
        if(!stationList.isEmpty()) {
            runOnUiThread(() -> {
                stationCustomAdapter = new StationCustomAdapter(stationList, this);
                stationCustomAdapter.setClickListener((view, position) -> {
                    if (!swipeRefreshLayout.isRefreshing()) {
                        Logger.i("Clicked " + stationCustomAdapter.getItem(position).name);
                    }
                });
                stationsRecycler.setAdapter(stationCustomAdapter);
                if (last) blockScreen(false);
            });
        }
    }

    /**
     * Adds more items to the already printed list
     */
    public void infoAdd(boolean last) {
        if(!stationList.isEmpty()) {
            runOnUiThread(() -> {
                stationCustomAdapter.notifyDataSetChanged();
                if (last) blockScreen(false);
            });
        }
    }

    public static List<GetStationInfoQuery.StationWithEvaId> getStationList() {
        return stationList;
    }
}
