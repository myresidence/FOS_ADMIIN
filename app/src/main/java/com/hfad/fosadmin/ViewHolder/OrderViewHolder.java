package com.hfad.fosadmin.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.hfad.fosadmin.Interface.ItemClickListener;
import com.hfad.fosadmin.R;

/**
 * Created by icefrog on 3/19/18.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress;


    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView){
        super(itemView);


        txtOrderAddress = (TextView)itemView.findViewById(R.id.order_address);
        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone = (TextView)itemView.findViewById(R.id.order_phone);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view){

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo){
        contextMenu.setHeaderTitle("Select The Action");

        contextMenu.add(0,0,getAdapterPosition(),"Update");
        contextMenu.add(0,1,getAdapterPosition(),"Delete");

    }

}