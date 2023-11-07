package com.warriors.etri.kiosk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements ETRIApiHandler.OnETRIApiResultListener {
    TextView textView;
    TextView apiTextView;
    Button button;
    Intent intent;
    SpeechRecognizer mRecognizer;
    final int PERMISSION = 1;
    private String fullResult = "";

    // 파이어 베이스와 연동해서 받아오기
//    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//    DatabaseReference orderRef = database.child("chuckchuck/order");
    private RecyclerView MenuRecyclerView;
    private RecyclerView.Adapter MenuAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Menu> MenuArrayList;
    //파이어베이스 연동하기
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        setContentView(R.layout.activity_main);

        // 안드로이드 6.0버전 이상인지 체크해서 퍼미션 체크
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO
            }, PERMISSION);
        }

        textView = findViewById(R.id.resultTextView);
        apiTextView = findViewById(R.id.apiTextView);
        button = findViewById(R.id.startButton);

        MenuRecyclerView = findViewById(R.id.recyclerView);
        MenuRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        MenuRecyclerView.setLayoutManager(layoutManager);
        MenuArrayList = new ArrayList<>();

        database = FirebaseDatabase.getInstance(); // firebase connection
        databaseReference = database.getReference("chuckchuck/menu/beverage");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MenuArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Menu menu = snapshot.getValue(Menu.class);
                    MenuArrayList.add(menu);
                }
                MenuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", String.valueOf(error.toException()));

            }
        });

        MenuAdapter = new MenuAdapter(MenuArrayList, this);
        MenuRecyclerView.setAdapter(MenuAdapter);


        // RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // 버튼 클릭 시 객체에 Context와 listener를 할당
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(intent);
//                writeNewOrder("1", "Americano", 4000, 3);
            }
        });
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식 시작", Toast.LENGTH_SHORT).show();
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

            Toast.makeText(getApplicationContext(), "에러 발생: " + message, Toast.LENGTH_SHORT).show();
        }

        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            StringBuilder allResults = new StringBuilder();

            for (int i = 0; i < matches.size(); i++) {
                String result = matches.get(i);
                allResults.append(result).append(" ");
            }

            fullResult = allResults.toString();

            textView.setText(fullResult);
            Log.d("Recognized Text", fullResult);
            System.out.println(fullResult);

            final ETRIApiHandler.OnETRIApiResultListener onETRIApiResultListener = new ETRIApiHandler.OnETRIApiResultListener() {
                @Override
                public void onApiResult(String result, String responseBody) {
                    try {
                        JSONObject responseJSON = new JSONObject(responseBody);
                        JSONObject returnObject = responseJSON.getJSONObject("return_object");
                        JSONObject mrcInfo = returnObject.getJSONObject("MRCInfo");
                        String answer = mrcInfo.getString("answer");

                        // "answer" 값을 출력 또는 처리
                        Log.d("Extracted Answer", answer);

                        // 필요에 따라 결과를 출력하거나 다른 작업을 수행합니다.
                        String displayText = answer;
                        apiTextView.setText(displayText);
                        Log.d("API 결과와 응답 본문1", displayText);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("메뉴 확인")
                                .setMessage(answer + "를 장바구니에 담으시겠습니까?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("MyTag", "positive");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("MyTag", "negative");
                                    }
                                })
                                .show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            // ETRIApiHandler를 통해 API 호출 및 결과 표시
            ETRIApiHandler.queryETRIApi(fullResult, "아메리카노 한잔 주세요: 아메리카노, 라떼 한잔 주세요:라뗴, 녹차라떼 한잔 주세요:녹차라떼, 아이스티 한잔 주세요:아이스티, 자몽에이드 한잔 주세요:자몽에이드, 블루베리스무디 한잔 주세요:블루베리스무디, 초코스무디 한잔 주세요:초코스무디, 카모마일 차 한잔 주세요:카모마일 차, 유자차 한잔 주세요:유자차, 홍차 한잔 주세요:홍차", onETRIApiResultListener);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

    };


    @Override
    public void onApiResult(String result, String responseBody) {
        try {
            JSONObject responseJSON = new JSONObject(responseBody);
            JSONObject returnObject = responseJSON.getJSONObject("return_object");
            JSONObject mrcInfo = returnObject.getJSONObject("MRCInfo");
            String answer = mrcInfo.getString("answer");

            // "answer" 값을 출력 또는 처리
            Log.d("Extracted Answer", answer);

            // 필요에 따라 결과를 출력하거나 다른 작업을 수행합니다.
            String displayText = answer;
            apiTextView.setText(displayText);
            Log.d("API 결과와 응답 본문1", displayText);
            // AlertDialog를 생성하여 "answer"를 확인
            new AlertDialog.Builder(this)
                    .setTitle("메뉴 확인")
                    .setMessage(answer + "를 장바구니에 담으시겠습니까?")
                    .show();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}