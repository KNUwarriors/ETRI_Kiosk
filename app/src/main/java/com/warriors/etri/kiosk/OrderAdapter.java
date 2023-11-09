package com.warriors.etri.kiosk;


import static com.warriors.etri.kiosk.MainActivity.payButton;
import static com.warriors.etri.kiosk.MainActivity.totalPrice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import android.widget.Button; // 추가

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private ArrayList<Order> arrayList;
    private Context context;

    private FirebaseDatabase database;
    private DatabaseReference orderDatabase;

    public OrderAdapter(ArrayList<Order> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list, parent, false);
        OrderViewHolder holder = new OrderViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.name.setText(String.valueOf(arrayList.get(position).getName()));
        holder.price.setText(String.valueOf(arrayList.get(position).getPrice()));
        holder.count.setText(String.valueOf(arrayList.get(position).getCount()));

        // 이미지 버튼 리스너 설정
        holder.increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                orderDatabase = database.getReference("chuckchuck/order");
                int currentPosition = holder.getAdapterPosition();
                Order currentOrder = arrayList.get(currentPosition);

                String currentOrderName = currentOrder.getName();

                Query query = orderDatabase.orderByChild("name").equalTo(currentOrderName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // 여기서 name 필드를 기준으로 데이터를 찾아 조작합니다.
                            Order order = snapshot.getValue(Order.class);
                            if (order != null) {
                                int newCount = order.getCount() + 1;

                                // 다른 조작 내용을 추가하거나 변경할 수 있습니다.

                                order.setCount(newCount);
                                snapshot.getRef().setValue(order);

                                currentOrder.setCount(newCount);
                                notifyDataSetChanged(); // 어댑터를 업데이트하여 변경된 내용을 반영
                                totalPrice += currentOrder.getPrice();
                                payButton.setText(totalPrice + "원\n\n결제하기");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 처리 중에 오류가 발생한 경우 처리
                    }
                });
            }
        });

        holder.decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                orderDatabase = database.getReference("chuckchuck/order");
                int currentPosition = holder.getAdapterPosition();
                Order currentOrder = arrayList.get(currentPosition);

                String currentOrderName = currentOrder.getName();

                Query query = orderDatabase.orderByChild("name").equalTo(currentOrderName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // 여기서 name 필드를 기준으로 데이터를 찾아 조작합니다.
                            Order order = snapshot.getValue(Order.class);
                            if (order != null) {
                                int newCount = order.getCount() - 1;

                                if (newCount >= 0) {
                                    if (newCount == 0) {
                                        snapshot.getRef().removeValue(); // count가 0이면 해당 데이터를 DB에서 제거
                                        arrayList.remove(currentOrder); // arrayList에서도 해당 데이터 제거
                                        notifyDataSetChanged(); // 어댑터를 업데이트하여 변경된 내용을 반영
                                    } else {
                                        order.setCount(newCount);
                                        snapshot.getRef().setValue(order);
                                    }
                                    currentOrder.setCount(newCount);
                                    notifyDataSetChanged(); // 어댑터를 업데이트하여 변경된 내용을 반영
                                    totalPrice -= currentOrder.getPrice();
                                    payButton.setText(totalPrice + "원\n\n결제하기");

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 처리 중에 오류가 발생한 경우 처리
                    }
                });
            }
        });
    }

    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView price;
        TextView count;

        Button increaseButton; // '+' 버튼
        Button decreaseButton; // '-' 버튼

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.price = itemView.findViewById(R.id.price);
            this.count = itemView.findViewById(R.id.count);
            this.increaseButton = itemView.findViewById(R.id.increaseButton);
            this.decreaseButton = itemView.findViewById(R.id.decreaseButton);
        }
    }
}
