package karthi.speedogps;

import android.Manifest;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.location.*;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ProgressiveGauge;
import com.github.anastr.speedviewlib.base.Gauge;
import com.github.anastr.speedviewlib.base.LinearGauge;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import com.loopj.android.http.*;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener locationListener;
    long  sentreq = 0;
    long  failreq = 0;
    TextView tv;
    ProgressiveGauge gauge;
    int count=0;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        System.out.println("Hide UI");
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

////-----------------

        tv = (TextView) findViewById(R.id.textView2);
        gauge = (ProgressiveGauge) findViewById(R.id.gauge);
        gauge.setMaxSpeed(140);
        gauge.setWithTremble(false);
        gauge.setSpeedTextPosition(ProgressiveGauge.Position.TOP_CENTER);
        gauge.setSpeedTextTypeface(Typeface.createFromAsset(getAssets(), "fonts/ticking-timebomb-bb.italic.ttf"));
        gauge.setSpeedTextFormat(ProgressiveGauge.INTEGER_FORMAT);
        tv.setText("Init: " + new Date().toString());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String url = "http://210.212.210.89:8000/gps";
        url += "?_id=" + "59b7786ef8fecb0960ff7ef1";
        url += "&Data="
                + "0"
                + "," + "1"
                + "," + df.format(new Date())
                + "," +  ""
                + "," +  ""
                + "," + 0
                + "," + 0
                + "," + 0
                + "," + "8"
                + "," + "9"
                + "," + 1
                + "," + "11"
                + "," + "12"
                + "," + "13"
                + "," + "14"
                + "," + "15"
                + "," + "16"
                + "," + "17"
                + "," + "18"
                + "," + "19"
                + "," + "20"
                + "," + "21"
                + "," + "1"
                + "," + "1";

        AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(1, 500);
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                tv.append("\nStart notice");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                tv.append("\n" + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                tv.append("\n" + statusCode);
             }

            @Override
            public void onRetry(int retryNo) {
                tv.append("\nretryNo: " + retryNo);
            }
        });



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gauge.speedTo(140, 500);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gauge.speedTo(0, 500);
                    }
                }, 800);
            }
        }, 500);

////-----------------
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                tv.setText("");

                double i = (location.getSpeed() * 3.6);
                gauge.speedTo((float) i);

                if (i < 10) {
                    gauge.setSpeedometerColor(Color.BLACK);
                    gauge.setSpeedometerBackColor(Color.GREEN);
                } else if (i < 50) {
                    gauge.setSpeedometerColor(Color.GREEN);
                    gauge.setSpeedometerBackColor(Color.BLUE);
                } else if (i < 100) {
                    gauge.setSpeedometerColor(Color.CYAN);
                    gauge.setSpeedometerBackColor(Color.RED);
                } else {
                    gauge.setSpeedometerColor(Color.RED);
                    gauge.setSpeedometerBackColor(Color.GREEN);
                }
                count++;

                if(count==3) {
                    count = 0;
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); // Quoted "Z" to indicate UTC, no timezone offset
                    df.setTimeZone(tz);
                    String url = "http://210.212.210.89:8000/gps";
                    url += "?_id=" + "59b7786ef8fecb0960ff7ef1";
                    url += "&Data="
                            + "0"
                            + "," + "1"
                            + "," + df.format(new Date(location.getTime()))
                            + "," + location.getLatitude()
                            + "," + location.getLongitude()
                            + "," + location.getAltitude()
                            + "," + (int) i
                            + "," + location.getBearing()
                            + "," + "8"
                            + "," + "9"
                            + "," + 1
                            + "," + "11"
                            + "," + "12"
                            + "," + "13"
                            + "," + "14"
                            + "," + "15"
                            + "," + "16"
                            + "," + "17"
                            + "," + "18"
                            + "," + "19"
                            + "," + "20"
                            + "," + "21"
                            + "," + "1";

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.setMaxRetriesAndTimeout(0, 0);
                    client.setConnectTimeout(100);
                    client.setResponseTimeout(100);
                    client.setTimeout(100);
                    client.get(url, new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                            tv.append("\nStart - "+sentreq + " : " +failreq); sentreq++;
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                            tv.append("\n success: " + statusCode);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                            tv.append("\n fail: " + statusCode);
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            tv.append("\nretryNo: " + retryNo);
                        }
                    });
                }
                tv.append("\nlat: " + location.getLatitude());
                tv.append("\nlng: " + location.getLongitude());
                tv.append("\nacc: " + location.getAccuracy() + " m");
                tv.append("\nspeed: " + location.getSpeed() * 3.6 + " km/h");
                tv.append("\ntime: " + new Date(location.getTime()));
                tv.append("\nsource: " + location.getProvider());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                String msg = "";
                switch (status) {
                    case LocationProvider.AVAILABLE:
                        msg = "AVAILABLE";
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        msg = "OUT_OF_SERVICE";
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        msg = "TEMPORARILY_UNAVAILABLE";
                        break;
                }
                tv.append("\n" + msg);
                tv.append("\n" + extras);
            }

            public void onProviderEnabled(String provider) {
                tv.append("\n" + "GPS enabled");
            }

            public void onProviderDisabled(String provider) {
                tv.append("\n" + "GPS disabled - please enable GPS as device only");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
////-----------------

        startTrack();
    }

    void startTrack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                tv.append("\n" + "Permission Requested");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            } else {
                tv.append("\n" + "Permission Granted");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 150, 0, locationListener);
            }
        } else
        {
            tv.append("\n" + "No need to ask for Permission");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 150, 0, locationListener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startTrack();
            else tv.append("\n" + "Permission Denied!");
        else tv.append("\n" + "Permission Denied!");
    }
}
