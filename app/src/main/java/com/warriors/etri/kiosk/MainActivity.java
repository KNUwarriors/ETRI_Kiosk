package com.warriors.etri.kiosk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.warriors.etri.kiosk.BottomSheetHandler;

public class MainActivity extends AppCompatActivity implements ETRIApiHandler.OnETRIApiResultListener {
    TextView textView;

    Button button;
    static Button payButton;
    static Button drawbtn;


    Intent intent;
    SpeechRecognizer mRecognizer;
    final int PERMISSION = 1;
    private String fullResult = "";

    // menu view
    private RecyclerView MenuRecyclerView;
    private RecyclerView.Adapter MenuAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Menu> MenuArrayList;
    // order view
    private RecyclerView OrderRecyclerView;
    private RecyclerView.Adapter OrderAdapter;
    private RecyclerView.LayoutManager orderlayoutManager;
    private ArrayList<Order> OrderArrayList;
    //파이어베이스 연동하기
    private FirebaseDatabase database;
    private DatabaseReference beverageDatabase;
    private DatabaseReference orderDatabase;

//    int orderId = 0;
//    String orderIdStr = "";
    int orderPrice = 0;
    int orderCount = 0;
    boolean orderExist = false;
    Order orderExistData = new Order();
    static int totalPrice = 0;
    String description = "";

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

        button = findViewById(R.id.startButton);

        payButton = findViewById(R.id.payButton);
        // menu
        MenuRecyclerView = findViewById(R.id.recyclerView);
        // 변경: GridLayoutManager로 변경
        layoutManager = new GridLayoutManager(this, 2); // 2는 열의 수, 필요에 따라 조절
//        MenuRecyclerView.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(this);
        MenuRecyclerView.setLayoutManager(layoutManager);
        MenuArrayList = new ArrayList<>();
        // order
        OrderRecyclerView = findViewById(R.id.orderRecyclerView);
        OrderRecyclerView.setHasFixedSize(true);
        orderlayoutManager = new LinearLayoutManager(this);
        OrderRecyclerView.setLayoutManager(orderlayoutManager);
        OrderArrayList = new ArrayList<>();
        // DB
        database = FirebaseDatabase.getInstance(); // firebase connection
        beverageDatabase = database.getReference("chuckchuck/menu/beverage");
        orderDatabase = database.getReference("chuckchuck/order");

        // menu connection
        beverageDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Log.e("MainActivity Menu", String.valueOf(error.toException()));

            }
        });

        MenuAdapter = new MenuAdapter(MenuArrayList, this);
        MenuRecyclerView.setAdapter(MenuAdapter);

        // order connection
        orderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                OrderArrayList.clear();
                totalPrice = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    OrderArrayList.add(order);
                    totalPrice += order.getPrice() * order.getCount();
                }
                OrderAdapter.notifyDataSetChanged();
                payButton.setText(totalPrice + "원\n\n결제하기");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity Order", String.valueOf(error.toException()));
            }
        });
        OrderAdapter = new OrderAdapter(OrderArrayList, this);
        OrderRecyclerView.setAdapter(OrderAdapter);

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
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialogStyle)
                        .setTitle("결제 확인")
                        .setMessage(totalPrice + "원 결제하였습니다.").show();
                orderDatabase.removeValue();
                OrderAdapter.notifyDataSetChanged();
                refreshOrderList();
            }
        });

        drawbtn = findViewById(R.id.drawBtn);
        drawbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetHandler.showBottomSheet(MainActivity.this, listener, intent);
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

                        // Pass the 'answer' value to WikiETRIApiHandler
                        WikiETRIApiHandler.queryWikiETRIApi(answer, new WikiETRIApiHandler.OnWikiETRIApiResultListener() {
                            @Override
                            public void onApiResult(String result, String responseBody) {
                                try {
                                    JSONObject responseJSON2 = new JSONObject(responseBody);
                                    JSONObject returnObject2 = responseJSON2.getJSONObject("return_object");
                                    JSONObject WikiInfoObject = returnObject2.getJSONObject("WiKiInfo");
                                    JSONArray answerInfoArray = WikiInfoObject.getJSONArray("AnswerInfo");

                                    if (answerInfoArray.length() > 0) {
                                        // Get the "answer" value from the first element in the array
                                        JSONObject answerInfo = answerInfoArray.getJSONObject(0);
                                        description = answerInfo.getString("answer");

                                        Log.d("Description", description);
                                    } else {
                                        // Handle the case where there's no answer info
                                        Log.d("Description", "No answer info available");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // 필요에 따라 결과를 출력하거나 다른 작업을 수행합니다.
                        String displayText = answer;

                        Log.d("API 결과와 응답 본문1", displayText);
                        // ---------------다이얼로그-------------------
                        new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialogStyle)
                                .setTitle("메뉴 확인")
                                .setMessage(answer + "를 장바구니에 담으시겠습니까?")
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("MyTag", "negative");
                                    }
                                })
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("MyTag", "positive");
                                        handleOrderConfirmation(answer);
                                    }
                                })
                                .create()
                                .show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            // ETRIApiHandler를 통해 API 호출 및 결과 표시
            ETRIApiHandler.queryETRIApi(fullResult, "'녹차라떼 한잔 주세요' : '녹차라떼' , '딸기스무디 한잔 주세요' : '딸기스무디' , '레몬에이드 한잔 주세요' : '레몬에이드' , '망고스무디 한잔 주세요' : '망고스무디' , '밀크티 한잔 주세요' : '밀크티' , '아메리카노 한잔 주세요' : '아메리카노' , '자몽에이드 한잔 주세요' : '자몽에이드' , '초코라떼 한잔 주세요' : '초코라떼' , '카페라떼 한잔 주세요' : '카페라떼'", onETRIApiResultListener);
        }

        private void handleOrderConfirmation(String answer) {
            // 주문 데이터베이스를 한 번만 확인
            orderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    orderId = 1;
                    orderExist = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        orderExistData = snapshot.getValue(Order.class);
                        if (orderExistData.getName().equals(answer)) {
                            orderExist = true;
//                            orderIdStr = "" + orderId;
                            Log.e("order is in orderlist!", String.valueOf(orderExist));
                            break;
                        }
//                        orderId += 1;
                    }

                    if (!orderExist) {
                        Log.e("!orderExist", orderExist + answer);
                        // 주문이 없는 경우, 메뉴 데이터베이스에서 가격을 가져와서 새로운 주문을 추가
                        beverageDatabase.child(answer).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Menu menu = dataSnapshot.getValue(Menu.class);
                                orderPrice = menu.getPrice();
                                orderCount = 1;
//                                orderIdStr = orderId + "";
                                writeNewOrder(answer, answer, orderPrice, orderCount);

                                refreshOrderList();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("MainActivity", String.valueOf(error.toException()));
                            }
                        });
                    } else {
                        Log.e("Else about !orderExist", orderExist + answer);
                        // 주문이 이미 있는 경우, 주문을 업데이트
                        orderExistData.setCount(orderExistData.getCount() + 1);
                        orderDatabase.child(answer).setValue(orderExistData);

                        refreshOrderList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", String.valueOf(error.toException()));
                }
            });
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

    };

    public void writeNewOrder(String orderId, String name, int price, int count) {
        Order order = new Order(name, price, count);

        orderDatabase.child(orderId).setValue(order);
    }

    // 주문 목록을 업데이트하는 함수
    private void refreshOrderList() {
        orderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                OrderArrayList.clear();
                totalPrice = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    OrderArrayList.add(order);
                    totalPrice += order.getPrice() * order.getCount();
                }
                OrderAdapter.notifyDataSetChanged();
                payButton.setText(totalPrice + "원\n\n결제하기");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity Order", String.valueOf(error.toException()));
            }
        });
    }

    @Override
    public void onApiResult(String result, String responseBody) {

    }
}
