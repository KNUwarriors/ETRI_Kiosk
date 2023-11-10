package com.warriors.etri.kiosk;



import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.app.Activity;
import android.view.ViewGroup;
import android.util.DisplayMetrics;

import com.google.android.material.bottomsheet.BottomSheetBehavior;



public class MenuDictionary {
    private static BottomSheetDialog bottomSheetDialog;
    private static SpeechRecognizer mRecognizer;
    private static RecognitionListener listener;
    private static Intent intent;

    private static TextView drawer_result;

    private static ImageButton btnMIC;
    private static Button btnClose;


    private static String fullResult = "";
    private static String answer = "";

    public static void showBottomSheet(Context context, Intent recognizerIntent, MainActivity mainActivity) {
        intent = recognizerIntent;


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dictsheetview = inflater.inflate(R.layout.dictionary_sheet, null, false);

        // Get the display metrics to calculate the height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // Set the height of the BottomSheet to 80% of the screen height
        int bottomSheetHeight = (int) (screenHeight * 0.8);

        // Set the layout parameters for the BottomSheet's view
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                bottomSheetHeight
        );
        dictsheetview.setLayoutParams(layoutParams);

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(dictsheetview);

        // Get the BottomSheetBehavior from the View
        View bottomSheetView = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

        // Set the height of the BottomSheet when it is in the collapsed state (peek height)
        bottomSheetBehavior.setPeekHeight(bottomSheetHeight);

        btnMIC = dictsheetview.findViewById(R.id.btnMIC);

        drawer_result = dictsheetview.findViewById(R.id.drawerResult);

        btnClose = dictsheetview.findViewById(R.id.btnClose);

        btnMIC.setOnClickListener(new View.OnClickListener(){
              @Override
              public void onClick(View v) {
                  mRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

                  mRecognizer.setRecognitionListener(new RecognitionListener() {
                      @Override
                      public void onReadyForSpeech(Bundle params) {
                          Toast.makeText(context, "음성인식 시작", Toast.LENGTH_SHORT).show();
                      }

                      @Override
                      public void onBeginningOfSpeech() {
                      }

                      @Override
                      public void onRmsChanged(float rmsdB) {
                      }

                      @Override
                      public void onBufferReceived(byte[] buffer) {
                      }

                      @Override
                      public void onEndOfSpeech() {
                      }

                      @Override
                      public void onError(int error) {
                          String message;

                          switch (error) {
                              case SpeechRecognizer.ERROR_AUDIO:
                                  message = "오디오 에러";
                                  break;
                              case SpeechRecognizer.ERROR_CLIENT:
                                  message = "클라이언트 에러";
                                  break;
                              case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                  message = "퍼미션 없음";
                                  break;
                              case SpeechRecognizer.ERROR_NETWORK:
                                  message = "네트워크 에러";
                                  break;
                              case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                  message = "네트웍 타임아웃";
                                  break;
                              case SpeechRecognizer.ERROR_NO_MATCH:
                                  message = "찾을 수 없음";
                                  break;
                              case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                  message = "RECOGNIZER 가 바쁨";
                                  break;
                              case SpeechRecognizer.ERROR_SERVER:
                                  message = "서버가 이상함";
                                  break;
                              case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                  message = "말하는 시간초과";
                                  break;
                              default:
                                  message = "알 수 없는 오류임";
                                  break;
                          }

                          Toast.makeText(context, "에러 발생: " + message, Toast.LENGTH_SHORT).show();
                      }

                      @Override
                      public void onResults(Bundle results) {
                          ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                          StringBuilder allResults = new StringBuilder();


                          for (int i = 0; i < matches.size(); i++) {
                              String result = matches.get(i);
                              allResults.append(result).append(" ");
                          }

                          fullResult = allResults.toString();

                          drawer_result.setText(fullResult);
                          Log.d("Recognized Text", fullResult);
                          System.out.println(fullResult);


                          ETRIApiHandler.OnETRIApiResultListener onETRIApiResultListener = new ETRIApiHandler.OnETRIApiResultListener() {
                              @Override
                              public void onApiResult(String result, String responseBody) {
                                  try {
                                      JSONObject responseJSON = new JSONObject(responseBody);
                                      JSONObject returnObject = responseJSON.getJSONObject("return_object");
                                      JSONObject mrcInfo = returnObject.getJSONObject("MRCInfo");
                                      answer = mrcInfo.getString("answer");

                                      // "answer" 값을 출력 또는 처리
                                      Log.d("Extracted Answer", answer);

                                      // 필요에 따라 결과를 출력하거나 다른 작업을 수행합니다.
                                      //String displayText = answer;
                                      Log.d("API 결과와 응답 본문1", answer);

                                  } catch (JSONException e) {
                                      e.printStackTrace();
                                  }
                              }
                          };

                          ETRIApiHandler.queryETRIApi(fullResult, "'녹차라떼 한잔 주세요' : '녹차라떼' , '딸기스무디 한잔 주세요' : '딸기스무디' , '레몬에이드 한잔 주세요' : '레몬에이드' , '망고스무디 한잔 주세요' : '망고스무디' , '밀크티 한잔 주세요' : '밀크티' , '아메리카노 한잔 주세요' : '아메리카노' , '자몽에이드 한잔 주세요' : '자몽에이드' , '초코라떼 한잔 주세요' : '초코라떼' , '카페라떼 한잔 주세요' : '카페라떼'", onETRIApiResultListener);
                          drawer_result.setText(answer);

                      }

                      @Override
                      public void onPartialResults(Bundle bundle) {

                      }

                      @Override
                      public void onEvent(int i, Bundle bundle) {

                      }

                  });

                  mRecognizer.startListening(intent);
              }
          });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();

    }

}
