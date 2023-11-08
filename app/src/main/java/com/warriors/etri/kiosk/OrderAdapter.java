package com.warriors.etri.kiosk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{
    private ArrayList<Order> arrayList;
    private Context context;

    public OrderAdapter(ArrayList<Order> arrayList, Context context){
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list, parent, false);
        OrderViewHolder holder = new OrderViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position){
        holder.name.setText(String.valueOf(arrayList.get(position).getName()));
        holder.price.setText(String.valueOf(arrayList.get(position).getPrice()));
        holder.count.setText(String.valueOf(arrayList.get(position).getCount()));
    }

    public int getItemCount(){
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView price;
        TextView count;

        public OrderViewHolder(@NonNull View itemView){
            super(itemView);

            this.name = itemView.findViewById(R.id.name);
            this.price = itemView.findViewById(R.id.price);
            this.count = itemView.findViewById(R.id.count);
        }
    }
}
