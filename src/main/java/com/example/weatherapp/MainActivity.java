package com.example.weatherapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class MainActivity extends AppCompatActivity {

    private double[] dailyTemperatureAverages = new double[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference fo text views
        TextView currentTemp = (TextView) findViewById(R.id.currentTemp);
        TextView dailyHighAndLow = (TextView) findViewById(R.id.dailyHighAndLow);
        TextView currentRainInches = (TextView) findViewById(R.id.currentRainInches);
        TextView currentWindSpeed = (TextView) findViewById(R.id.currentWindSpeed);
        TextView currentHumidity = (TextView) findViewById(R.id.currentHumidity);

        // average daily temperature
        TextView day1Temp = (TextView) findViewById(R.id.day1Temp);
        TextView day2Temp = (TextView) findViewById(R.id.day2Temp);
        TextView day3Temp = (TextView) findViewById(R.id.day3Temp);
        TextView day4Temp = (TextView) findViewById(R.id.day4Temp);
        TextView day5Temp = (TextView) findViewById(R.id.day5Temp);
        TextView day6Temp = (TextView) findViewById(R.id.day6Temp);
        TextView day7Temp = (TextView) findViewById(R.id.day7Temp);

        // each day of week
        TextView day2 = (TextView) findViewById(R.id.day2);
        TextView day3 = (TextView) findViewById(R.id.day3);
        TextView day4 = (TextView) findViewById(R.id.day4);
        TextView day5 = (TextView) findViewById(R.id.day5);
        TextView day6 = (TextView) findViewById(R.id.day6);
        TextView day7 = (TextView) findViewById(R.id.day7);


        // call Open-Meteo Weather Forecast API
        String url = "https://api.open-meteo.com/v1/forecast?latitude=30.2672&longitude=-97.7431&hourly=temperature_2m,relative_humidity_2m,rain,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch";
        // fetch data to calculate and present data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // fetch the temperatures for the next 7-days of the week
                    JSONArray jsonTemperatureArray = response.getJSONObject("hourly").getJSONArray("temperature_2m");
                    // convert JSON array to num array
                    double[] temperatureArray = convertToNumArr(jsonTemperatureArray);

                    // fetch the fahrenheit unit
                    String fahrenheitUnit = response.getJSONObject("hourly_units").getString("temperature_2m");
                    // display current temperature to user
                    String formattedTemp = String.format("%.0f", temperatureArray[0]);
                    currentTemp.setText(formattedTemp + fahrenheitUnit);

                    // fetch high and low temperatures
                    double highTemp = response.getJSONObject("daily").getJSONArray("temperature_2m_max").getDouble(0);
                    double lowTemp = response.getJSONObject("daily").getJSONArray("temperature_2m_min").getDouble(0);
                    // display high and low temperatures for the day to user
                    String formattedHighTemp = String.format("%.0f", highTemp);
                    String formattedLowTemp = String.format("%.0f", lowTemp);
                    dailyHighAndLow.setText("H: " + formattedHighTemp + fahrenheitUnit + "   L: " + formattedLowTemp + fahrenheitUnit);

                    // display inches of rain
                    double currRainInches = response.getJSONObject("hourly").getJSONArray("rain").getDouble(0);
                    String formattedRain = String.format("%.2f", currRainInches);
                    currentRainInches.setText(formattedRain + " in");

                    // display wind speed
                    String windUnit = response.getJSONObject("hourly_units").getString("wind_speed_10m");
                    // display current wind speed to user
                    double currWindSpeed = response.getJSONObject("hourly").getJSONArray("wind_speed_10m").getDouble(0);
                    currentWindSpeed.setText(currWindSpeed + " " + windUnit);

                    // display humidity
                    String humidityUnit = response.getJSONObject("hourly_units").getString("relative_humidity_2m");
                    // display current humidity percentage to user
                    int currHumidity = response.getJSONObject("hourly").getJSONArray("relative_humidity_2m").getInt(0);
                    currentHumidity.setText(currHumidity + humidityUnit);

                    // display 7-day average weather forecast, starting off with today
                    calculateAverageTemperatures(temperatureArray);
                    day1Temp.setText(String.format("%.0f", dailyTemperatureAverages[0]) + fahrenheitUnit);
                    day2Temp.setText(String.format("%.0f", dailyTemperatureAverages[1]) + fahrenheitUnit);
                    day3Temp.setText(String.format("%.0f", dailyTemperatureAverages[2]) + fahrenheitUnit);
                    day4Temp.setText(String.format("%.0f", dailyTemperatureAverages[3]) + fahrenheitUnit);
                    day5Temp.setText(String.format("%.0f", dailyTemperatureAverages[4]) + fahrenheitUnit);
                    day6Temp.setText(String.format("%.0f", dailyTemperatureAverages[5]) + fahrenheitUnit);
                    day7Temp.setText(String.format("%.0f", dailyTemperatureAverages[6]) + fahrenheitUnit);

                    // get today's date
                    LocalDate today = LocalDate.now();
                    day2.setText(today.plusDays(1).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                    day3.setText(today.plusDays(2).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                    day4.setText(today.plusDays(3).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                    day5.setText(today.plusDays(4).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                    day6.setText(today.plusDays(5).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                    day7.setText(today.plusDays(6).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

        }, new Response.ErrorListener() {
            // ERROR CASE
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });


        // call Open-Meteo Weather Forecast API again to access archived forecasts for ML model
        String url2 = "https://api.open-meteo.com/v1/forecast?latitude=30.269146&longitude=-97.75339&hourly=temperature_2m,relative_humidity_2m,rain,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=ms&precipitation_unit=inch&past_days=92";
        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(url2, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                try {
//                    // Load CSV file
//                    CSVLoader loader = new CSVLoader();
//                    loader.setSource(new File("/Users/gabrielbanda/AndroidStudioProjects/WeatherApp2/app/weather_data.csv"));
//                    Instances data = loader.getDataSet();
//
//                    // we want to predict temperature, so we should set class index to the column that contains the temp values
//                    data.setClassIndex(0);
//
//                    // configure classifier
//                    LinearRegression classifier = new LinearRegression();
//                    classifier.buildClassifier(data);
//
//                    Instance tomorrowInstance = new DenseInstance(4); // Assuming 4 attributes (temperature, humidity, rain, wind)
//                    tomorrowInstance.setDataset(data);
//                    // Set attribute values for tomorrow's weather
//                    double prediction = classifier.classifyInstance(tomorrowInstance);
//                    currentTemp.setText("Predicted Tomorrow's Temperature: " + prediction);
//                    currentTemp.setText("TESTING");
//
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        // request queue will call api method and get a response or error
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
        requestQueue.add(jsonObjectRequest2);

    }

    private double[] convertToNumArr(JSONArray jsonArr) throws JSONException {
        // convert JSON array to int array
        double[] numArr = new double[jsonArr.length()];
        for (int i = 0; i < numArr.length; i++) {
            numArr[i] = jsonArr.getDouble(i);
        }
        return numArr;
    }

    private void calculateAverageTemperatures(double[] temperatureArray) throws JSONException {
        // calculate average temperature for each day of the week
        for (int i = 0; i < 7; i++) {
            double sum = 0;
            for (int j = i * 24; j < (i + 1) * 24; j++) {
                sum += temperatureArray[j];
            }
            dailyTemperatureAverages[i] = (sum / 24);
        }
    }
}