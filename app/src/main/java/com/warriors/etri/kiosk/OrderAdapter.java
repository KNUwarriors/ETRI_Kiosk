package com.warriors.etri.kiosk;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import android.widget.Button; // 추가

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private ArrayList<Order> arrayList;
    private Context context;

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
                int currentPosition = holder.getAdapterPosition();
                Order currentOrder = arrayList.get(currentPosition);
                currentOrder.setCount(currentOrder.getCount() + 1);
                notifyDataSetChanged(); // 어댑터를 업데이트하여 변경된 내용을 반영
            }
        });

        holder.decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                Order currentOrder = arrayList.get(currentPosition);
                int newCount = currentOrder.getCount() - 1;
                if (newCount >= 0) {
                    currentOrder.setCount(newCount);
                    notifyDataSetChanged(); // 어댑터를 업데이트하여 변경된 내용을 반영
                }
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
