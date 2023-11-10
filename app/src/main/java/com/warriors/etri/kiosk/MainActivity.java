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
import android.content.Intent;

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

    Button dictbutton;
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

        dictbutton = findViewById(R.id.dictBtn);

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
        dictbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("사전이올시다");
                MenuDictionary.showBottomSheet(MainActivity.this, intent, MainActivity.this);
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
                BottomSheetHandler.showBottomSheet(MainActivity.this, intent, MainActivity.this);


            }
        });

    }


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

    public void  addOrder(String answer) {
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

        Log.d("MainActivity", "주문 추가: " + answer);
    }
}
