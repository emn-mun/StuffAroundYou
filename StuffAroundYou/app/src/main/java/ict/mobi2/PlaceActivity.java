package ict.mobi2;

import android.media.Image;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlaceActivity extends Activity {

    @Bind(R.id.location_picture)
    ImageView locationPicture;
    @Bind(R.id.location_name)
    TextView locationName;
    @Bind(R.id.location_established)
    TextView locationEstablished;
    @Bind(R.id.location_preview)
    TextView locationPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        ButterKnife.bind(this);

        String beaconName = getIntent().getExtras().getString("BeaconName");
        if (beaconName != null && !beaconName.isEmpty()) {

            switch (beaconName) {
                case Constants.LABORATORY_3:
                    locationPicture.setImageResource(R.drawable.casa_arena);
                    locationName.setText(R.string.casa_name);
                    locationEstablished.setText(R.string.casa_established);
                    locationPreview.setText(R.string.casa_preview);
                    break;
                case Constants.LABORATORY_6:
                    locationPicture.setImageResource(R.drawable.prison);
                    locationName.setText(R.string.pri_name);
                    locationEstablished.setText(R.string.pri_established);
                    locationPreview.setText(R.string.pri_preview);
                    break;
                case Constants.LABORATORY_9:
                    locationPicture.setImageResource(R.drawable.via);
                    locationName.setText(R.string.via_name);
                    locationEstablished.setText(R.string.via_established);
                    locationPreview.setText(R.string.via_preview);
                    break;
                default:
                    locationPicture.setImageResource(R.drawable.art);
                    locationName.setText(R.string.art_name);
                    locationEstablished.setText(R.string.art_established);
                    locationPreview.setText(R.string.art_preview);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
