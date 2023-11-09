package com.warriors.etri.kiosk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
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
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetHandler {
    private static BottomSheetDialog bottomSheetDialog;
    private static SpeechRecognizer mRecognizer;
    private static RecognitionListener listener;
    private static Intent intent;

    private static TextView drawer_question;
    private static TextView drawer_result;
    private static ImageButton btnMIC;
    private static Button btnClose;

    private static int order_in = 0;
    private static int cnt = 0;

    private static String fullResult = "";
    private static String answer = "";

    public static void showBottomSheet(Context context, RecognitionListener recognitionListener, Intent recognizerIntent, MainActivity mainActivity) {
        listener = recognitionListener;
        intent = recognizerIntent;

        order_in = 0;
        cnt = 0;


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View bottomSheetView = inflater.inflate(R.layout.bottom_sheet, null, false);
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(bottomSheetView);

        btnMIC = bottomSheetView.findViewById(R.id.btnMIC);
        drawer_question = bottomSheetView.findViewById(R.id.qtext);
        drawer_result = bottomSheetView.findViewById(R.id.drawerResult);
        btnClose = bottomSheetView.findViewById(R.id.btnClose);

        btnMIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(order_in);
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

                        // 음성인식 결과에 따라 order_in 업데이트
                        if (order_in == 0) {
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
                                        String displayText = answer;
                                        Log.d("API 결과와 응답 본문1", displayText);

                                        displayText =  answer + "를 주문하시겠습니까?\n(예, 아니오)";
                                        drawer_question.setText(displayText);

                                        // Assuming you want to change order_in after successful API call
                                        order_in = 1;


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            ETRIApiHandler.queryETRIApi(fullResult, "'녹차라떼 한잔 주세요' : '녹차라떼' , '딸기스무디 한잔 주세요' : '딸기스무디' , '레몬에이드 한잔 주세요' : '레몬에이드' , '망고스무디 한잔 주세요' : '망고스무디' , '밀크티 한잔 주세요' : '밀크티' , '아메리카노 한잔 주세요' : '아메리카노' , '자몽에이드 한잔 주세요' : '자몽에이드' , '초코라떼 한잔 주세요' : '초코라떼' , '카페라떼 한잔 주세요' : '카페라떼'", onETRIApiResultListener);
                            drawer_result.setText(fullResult);

                        } else if (order_in == 1) {
                            // Update Drawer Question and Drawer Result for order_in = 1
                            System.out.println(fullResult);

                            ETRIApiHandler.OnETRIApiResultListener onETRIApiResultListener = new ETRIApiHandler.OnETRIApiResultListener() {
                                @Override
                                public void onApiResult(String result, String responseBody) {
                                    try {
                                        JSONObject responseJSON = new JSONObject(responseBody);
                                        JSONObject returnObject = responseJSON.getJSONObject("return_object");
                                        JSONObject mrcInfo = returnObject.getJSONObject("MRCInfo");
                                        String YorN = mrcInfo.getString("answer");

                                        // "answer" 값을 출력 또는 처리
                                        Log.d("Extracted Answer", YorN);

                                        // 필요에 따라 결과를 출력하거나 다른 작업을 수행합니다.
                                        String displayText = YorN;
                                        Log.d("API 결과와 응답 본문1", displayText);

                                        // API 결과에 따라 다르게 처리
                                        if ("1".equals(YorN)) {
                                            // API 결과가 1인 경우: 해당 메뉴를 DB에 추가하고 bottom sheet를 닫습니다.
                                            System.out.println("1입니다링고");
                                            order_in = 0;
                                            // TODO: 해당 메뉴를 DB에 추가하는 로직을 작성하세요.
                                            mainActivity.addOrder(answer);

                                            bottomSheetDialog.dismiss();
                                        } else {
                                            System.out.println("0입니다링고");
                                            // API 결과가 0인 경우: 다시 초기 상태로 돌아갑니다.
                                            displayText =  "주문하실 메뉴를 말해주세요\n";
                                            drawer_question.setText(displayText);
                                            // Reset to the initial state and increase cnt
                                            order_in = 0;
                                            cnt++;

                                            if (cnt == 10) {
                                                // Handle the case when cnt reaches 5
                                                // Close the drawer or perform other actions
                                                bottomSheetDialog.dismiss();
                                            }
                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            ETRIApiHandler.queryETRIApi(fullResult, "'예' : '1' , '아니오': '0'", onETRIApiResultListener);
                            drawer_result.setText(fullResult);
                            // Handle unsuccessful response for order_in = 1
                            // Reset to the initial state and increase cnt
                        }

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

