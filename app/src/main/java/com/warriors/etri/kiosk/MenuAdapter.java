package com.warriors.etri.kiosk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private ArrayList<Menu> arrayList;
    private Context context;

    public MenuAdapter(ArrayList<Menu> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_list, parent, false);
        MenuViewHolder holder = new MenuViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(MenuViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getImg())
                .into(holder.menu_img);
        holder.menu_name.setText(String.valueOf(arrayList.get(position).getName()));
        holder.menu_price.setText(String.valueOf(arrayList.get(position).getPrice()));
    }

    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView menu_img;
        TextView menu_name;
        TextView menu_price;

        public MenuViewHolder(View itemView) {
            super(itemView);

            this.menu_img = itemView.findViewById(R.id.menu_img);
            this.menu_name = itemView.findViewById(R.id.menu_name);
            this.menu_price = itemView.findViewById(R.id.menu_price);

        }
    }

}
