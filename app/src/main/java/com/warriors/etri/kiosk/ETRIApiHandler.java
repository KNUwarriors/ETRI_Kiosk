package com.warriors.etri.kiosk;

import android.os.AsyncTask;
import android.speech.RecognitionListener;
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


public class ETRIApiHandler {
    private static final String OPEN_API_URL = "http://aiopen.etri.re.kr:8000/MRCServlet";
    private static final String ACCESS_KEY = "mykey";
    private static String responseBody;

    public interface OnETRIApiResultListener {
        void onApiResult(String result, String responseBody);
    }

    public static void queryETRIApi(String passage, String question, OnETRIApiResultListener listener) {
        new ETRIApiAsyncTask(listener).execute(passage, question);
    }

    private static class ETRIApiAsyncTask extends AsyncTask<String, Void, String> {
        private OnETRIApiResultListener listener;

        ETRIApiAsyncTask(OnETRIApiResultListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String passage = params[1];
            String question = params[0];

            Gson gson = new Gson();

            Map<String, Object> request = new HashMap<>();
            Map<String, String> argument = new HashMap<>();

            argument.put("question", question);
            argument.put("passage", passage);

            request.put("argument", argument);

            URL url;
            Integer responseCode = null;

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

                Log.d("ETRI API Response Code", responseCode.toString());
                Log.d("ETRI API Response Body", responseBody);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseBody;
        }
        @Override
        protected void onPostExecute(String result) {
            // 코드는 ETRIApiHandler 클래스 내의 이 부분을 설명하는 코드입니다.
            // ETRI API의 결과를 처리하고 결과를 listener를 통해 전달합니다.
            listener.onApiResult(result, responseBody);
        }

    }
}
