package com.android.weatherforecast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androdocs.httprequest.HttpRequest;
import com.android.weathertest.R;
import com.squareup.okhttp.HttpUrl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mDescription;
    private TextView mTemperature;
    private TextView mCity;
    private TextView mPressure;
    private RecyclerView mForecast;
    private WeatherAdapter mAdapter;
    List<WeatherInfo> weatherInfos = new ArrayList();
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDescription = findViewById(R.id.description);
        mTemperature = findViewById(R.id.temperature);
        mCity = findViewById(R.id.cityName);
        mPressure = findViewById(R.id.pressure);
        mForecast = findViewById(R.id.forecast_weather);

        layoutManager = new LinearLayoutManager(this);
        mForecast.setLayoutManager(layoutManager);
        mForecast.setAdapter(mAdapter);
    }

    public void GetData(String s) {

        String cityName = s;

        GetWeatherInfoAsyncTask getWeatherInfoAsyncTask = new GetWeatherInfoAsyncTask();
        getWeatherInfoAsyncTask.execute(cityName.trim(), "forecast");
        mAdapter = new WeatherAdapter(this, weatherInfos);
        mForecast.setAdapter(mAdapter);
    }

    public class GetWeatherInfoAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.openweathermap.org/data/2.5/" + strings[1].trim()).newBuilder();
            urlBuilder.addQueryParameter("q", strings[0]);
            urlBuilder.addQueryParameter("appid", "1aa5d44c3fd5fa724070f5773a7f52d3");
            String response = HttpRequest.excuteGet(urlBuilder.build().toString());

            return response;
        }

        @Override
        protected void onPostExecute(String data) {

            try {
                JSONObject mJsonObj1 = new JSONObject(data);
                JSONArray mList = mJsonObj1.getJSONArray("list");
                JSONObject mJsonObj = mList.getJSONObject(0);
                JSONObject main = mJsonObj.getJSONObject("main");
                JSONObject weather = mJsonObj.getJSONArray("weather").getJSONObject(0);
                String mTemp = main.getString("temp");
                String mWeatherDesc = weather.getString("description");
                String mPressur = main.getString("pressure");

                double mTem = Double.parseDouble(mTemp);
                double mTempCelsius = ToCelcius(mTem);


                mTemperature.setText(String.format("%.1f", mTempCelsius) + " °C");
                mDescription.setText(mWeatherDesc);
                mPressure.setText(mPressur);

                weatherInfos.clear();
                for (int i = 1; i < mList.length(); i++) {

                    JSONObject mObj = mList.getJSONObject(i);
                    JSONObject mainn = (JSONObject) mObj.get("main");
                    JSONArray weatherr = (JSONArray) mObj.get("weather");
                    String dt_time = mObj.getString("dt_txt");

                    if (dt_time.contains("15:00:00")) {

                        String mTmp = mainn.getString("temp");
                        String mPrs = mainn.getString("pressure");

                        String desc = weatherr.getJSONObject(0).getString("description");

                        double temp = Double.parseDouble(mTmp);
                        double tempCels = ToCelcius(temp);
                        int press = Integer.parseInt(mPrs);

                        WeatherInfo weatherInfo = new WeatherInfo();
                        weatherInfo.setTemp(tempCels);
                        weatherInfo.setPressure(press);
                        weatherInfo.setDescription(desc);
                        weatherInfo.setDate(dt_time);
                        weatherInfos.add(weatherInfo);
                    }
                }

                mAdapter = new WeatherAdapter(MainActivity.this, weatherInfos);
                mForecast.setAdapter(mAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void ShowAlertDialogView(View view) {

        AlertDialog.Builder mAlert = new AlertDialog.Builder(this);

        mAlert.setTitle("Choose city");

        final String[] cities = {"Tbilisi", "Kutaisi", "Batumi"};
        mAlert.setItems(cities, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                switch (option) {
                    case 0:
                        GetData((cities[0]));
                        mCity.setText(cities[0]);
                        break;
                    case 1:
                        GetData(cities[1]);
                        mCity.setText(cities[1]);
                        break;
                    case 2:
                        GetData(cities[2]);
                        mCity.setText(cities[2]);
                        break;
                }
            }
        });

        AlertDialog mDialog = mAlert.create();
        mDialog.show();
    }

    public double ToCelcius(double f) {

        return f - 272.15;
    }


    public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> {
        private List<WeatherInfo> weatherInfos;
        private LayoutInflater mInflater;

        public WeatherAdapter(Context context, List<WeatherInfo> data) {

            this.mInflater = LayoutInflater.from(context);
            this.weatherInfos = data;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView mTemp;
            public TextView mPress;
            public TextView mDesc;
            public TextView mDate;


            public MyViewHolder(View v) {
                super(v);
                mTemp = v.findViewById(R.id.temperature1);
                mPress = v.findViewById(R.id.pressure1);
                mDesc = v.findViewById(R.id.description1);
                mDate = v.findViewById(R.id.date_time);
            }
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            try {
                int pressure = weatherInfos.get(position).getPressure();
                String temp = String.format("%.1f", weatherInfos.get(position).getTemp()) + " °C";
                String desc = weatherInfos.get(position).getDescription();
                String date = weatherInfos.get(position).getDate().substring(0, 10);

                holder.mDesc.setText(desc);
                holder.mTemp.setText(temp);
                holder.mPress.setText(String.valueOf(pressure));
                holder.mDate.setText(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public WeatherAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            View v = mInflater.inflate(R.layout.view_weather_item, parent, false);

            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public int getItemCount() {
            return weatherInfos.size();
        }
    }
}

