package com.daninic9.ubahnstations.stations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daninic9.ubahnstations.GetStationInfoQuery;
import com.daninic9.ubahnstations.MainActivity;
import com.daninic9.ubahnstations.R;

import java.io.IOException;
import java.net.URL;
import java.util.List;


public class StationCustomAdapter extends ArrayAdapter<GetStationInfoQuery.StationWithEvaId> {
    private List<GetStationInfoQuery.StationWithEvaId> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView stationnameTextview;
        ImageView stationImage;
    }

    public StationCustomAdapter(List<GetStationInfoQuery.StationWithEvaId> data, Context context) {
        super(context, R.layout.item_station, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        GetStationInfoQuery.StationWithEvaId station = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        StationCustomAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new StationCustomAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_station, parent, false);

            viewHolder.stationnameTextview = convertView.findViewById(R.id.stationname_textview);
            viewHolder.stationImage = convertView.findViewById(R.id.station_image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StationCustomAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.stationnameTextview.setText(station.name());

        if (station.picture() != null && !station.picture().url().isEmpty()) {
            new Thread(() -> {
                try {
                    URL newUrl = new URL(station.picture().url());
                    Bitmap bitmap = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream());
                    ((MainActivity) mContext).runOnUiThread(() -> {
                        viewHolder.stationImage.setImageBitmap(bitmap);
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            viewHolder.stationImage.setImageResource(R.drawable.noimage);
        }




//        viewHolder.searchcoordinates_dropoff_button.setOnClickListener(v -> {
//            ActivityVehicleList.selectedVehicle = station;
//            ((ActivityVehicleList) mContext).createFragment(MapsFragment.class, Names.FRAG_MAPS_DOP);
//        });

        convertView.setOnClickListener(view -> {
            //TODO: program what happens when clicking on a station item
        });

        return convertView;
    }

    public static Bitmap bytesToBitmap(byte[] bytes) {
        // This function converts a byte array into a Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }


}
