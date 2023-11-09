package com.warriors.etri.kiosk;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WikiETRIApiHandler {
    private static final String OPEN_API_URL = "http://aiopen.etri.re.kr:8000/WikiQA";
    private static final String ACCESS_KEY = "c692a8e7-7450-432a-9468-a4049cc27974";  // Replace with your actual access key
    private static final String TYPE = "ENGINE_TYPE";            // Replace with your actual type

    public interface OnWikiETRIApiResultListener {
        void onApiResult(String result, String responseBody);
    }

    public static void queryWikiETRIApi(String question, OnWikiETRIApiResultListener listener) {
        new WikiETRIApiAsyncTask(listener).execute(question);
    }

    private static class WikiETRIApiAsyncTask extends AsyncTask<String, Void, String> {
        private OnWikiETRIApiResultListener listener;

        WikiETRIApiAsyncTask(OnWikiETRIApiResultListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String question = params[0];

            Gson gson = new Gson();

            Map<String, Object> request = new HashMap<>();
            Map<String, String> argument = new HashMap<>();

            argument.put("question", question);
            argument.put("type", TYPE);

            request.put("argument", argument);

            URL url;
            Integer responseCode = null;
            String responseBody = null;

            try {
                url = new URL(OPEN_API_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Authorization", ACCESS_KEY);

                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(gson.toJson(request).getBytes("UTF-8"));
                wr.flush();
                wr.close();

                responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder responseBodyBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBodyBuilder.append(line);
                    }
                    responseBody = responseBodyBuilder.toString();
                } else {
                    // Handle the response code as needed
                }

                Log.d("Wiki API Response Code", responseCode.toString());
                Log.d("Wiki API Response Body", responseBody);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseBody;
        }

        @Override
        protected void onPostExecute(String result) {
            listener.onApiResult(result, result);
        }
    }
}
