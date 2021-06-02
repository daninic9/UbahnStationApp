package com.daninic9.ubahnstations;

import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class GraphQlManager {

    private final Context context;
    private final ApolloClient apolloClient;
    private int listAddSize;
    private int stationsShown = 0;

    private CountDownTimer countDownTimer;
    private int timeoutTime;

    private final List<Integer> idList = new ArrayList<>();

    /**
     * Constructor of the GraphQl Manager Class
     *
     * @param context {@link Context} of the {@link MainActivity}
     */
    public GraphQlManager(Context context) {
        this.context = context;

        apolloClient = ApolloClient.builder()
                .serverUrl(Objects.requireNonNull(getGraphqlUrl()))
                .build();
    }

    private String getGraphqlUrl() {
        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            listAddSize = Integer.parseInt(properties.getProperty("listsize"));
            timeoutTime = Integer.parseInt(properties.getProperty("timeout"));
            return properties.getProperty("graphqlserver_url");
        } catch (Resources.NotFoundException e) {
            Logger.e("Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Logger.e("Failed to open config file.");
        }
        return null;
    }

    /**
     * Checks the Station list for items to obtain station
     * information and add to listview
     * or in case of refresh, to fill de list
     *
     * @param max Total number of items shown on list
     */
    public void initList(int max) {
        Logger.i("Requesting " + max + "+" + listAddSize + " from " + idList.size());
        if (!idList.isEmpty() && max < idList.size()) {
            int ii = 0;
            for (Integer id : idList) {
                ii++;
                if (ii <= max) continue;
                stationsShown++;
                Logger.d("Requesting " + ii + "/" + idList.size() + " - " + id);
                boolean last = stationsShown >= max + listAddSize ||
                        stationsShown == idList.size() ||
                        ii == idList.size();
                getStationContent(id, max != 0, last);
                if (last) return;
            }
        } else {
            ((MainActivity) context).infoAdd(true);
        }
    }

    /**
     * Add new items to the already set list
     */
    public void addMore() {
        initList(stationsShown);
    }

    /**
     * Clean the station list
     */
    public void cleanList() {
        MainActivity.getStationList().clear();
        idList.clear();
        stationsShown = 0;
    }

    /**
     * Request a query to Graphql to obtain all stations
     */
    public void queryGetAll(String search) {
        cleanList();
        apolloClient.query(new GetStationsQuery(search))
                .enqueue(new ApolloCall.Callback<GetStationsQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetStationsQuery.Data> response) {
                        Logger.i("Query Success");
                        if (response.getData() != null &&
                                !response.getData().search.stations.isEmpty()) {
                            for (GetStationsQuery.Station station :
                                    response.getData().search.stations) {
                                if (station.primaryEvaId != null) {
                                    idList.add(station.primaryEvaId);
                                }
                            }
                            initList(0);
                        } else {
                            ((MainActivity) context).sendToast(context.getString(R.string.error_empty));
                        }
                    }
                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Logger.e("Error " + e.getLocalizedMessage());
                        ((MainActivity) context).sendWarningError(context.getString(R.string.error),
                                context.getString(R.string.error_query));
                    }
                });
    }

    private void getStationContent(int id, boolean isAdding, boolean last) {
        Logger.d(id + " - " + isAdding + " - " + last);
        setTimeout(last);
        apolloClient.query(new GetStationInfoQuery(id))
                .enqueue(new ApolloCall.Callback<GetStationInfoQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetStationInfoQuery.Data> response) {
                        if (response.getData() != null &&
                                response.getData().stationWithEvaId != null) {
                            if (last) {
                                Logger.i("Retrieved last station info: " +
                                        Objects.requireNonNull(response.getData()
                                                .stationWithEvaId()).name);
                                countDownTimer.cancel();
                            }
                            MainActivity.getStationList().add(response.getData().stationWithEvaId);
                            if (isAdding) {
                                ((MainActivity) context).infoAdd(last);
                            } else {
                                ((MainActivity) context).infoUpdate(last);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Logger.e("Error " + e.getLocalizedMessage());
                        ((MainActivity) context).sendWarningError(context.getString(R.string.error),
                                context.getString(R.string.error_query));
                    }
                });
    }

    private void setTimeout(boolean last) {
        ((MainActivity) context).runOnUiThread(() -> {
            if (last) {
                countDownTimer = new CountDownTimer(timeoutTime, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        Logger.d("Timeout running...");
                    }

                    @Override
                    public void onFinish() {
                        Logger.e("Timeout");
                        ((MainActivity) context).sendWarningError(context.getString(R.string.warn),
                                context.getString(R.string.timeout));
                    }
                };
                countDownTimer.start();
            }
        });
    }

    public int getStationsShown() {
        return stationsShown;
    }

    public List<Integer> getIdList() {
        return idList;
    }
}

