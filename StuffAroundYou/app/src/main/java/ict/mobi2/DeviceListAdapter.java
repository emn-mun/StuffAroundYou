package ict.mobi2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Pop Alex-Cristian on 11/4/2015.
 */
public class DeviceListAdapter extends BaseAdapter {

    private Collection<Beacon> mArray;
    private Context mContext;

    public DeviceListAdapter(Context context, Collection<Beacon> beacons) {
        this.mContext = context;
        this.mArray = beacons;
    }

    @Override
    public int getCount() {
        return mArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.location_item, null);
            viewHolder = new ViewHolder();
            viewHolder.placeName = (TextView) convertView.findViewById(R.id.place_name);
            viewHolder.placeDistance = (TextView) convertView.findViewById(R.id.place_range);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Beacon device = (Beacon) mArray.toArray()[position];
        final String deviceName = device.getBluetoothName();

        if (deviceName != null && deviceName.length() > 0) {
            switch (deviceName) {
                case Constants.LABORATORY_3:
                    viewHolder.placeName.setText(Constants.CASA);
                    break;
                case Constants.LABORATORY_6:
                    viewHolder.placeName.setText(Constants.PRISON);
                    break;
                case Constants.LABORATORY_9:
                    viewHolder.placeName.setText(Constants.VIA);
                    break;
                default:
                    viewHolder.placeName.setText(Constants.ART);
                    break;
            }
        }
        DecimalFormat df = new DecimalFormat("###.#");

        if (device.getDistance() < 1) {
            viewHolder.placeDistance.setText("Very Close");
        } else if (device.getDistance() < 2) {
            viewHolder.placeDistance.setText("Near");
        } else {
            viewHolder.placeDistance.setText("In Range");
        }

        return convertView;
    }

    class ViewHolder {
        TextView placeName;
        TextView placeDistance;
    }
}
