package com.hfad.fosadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hfad.fosadmin.Common.Common;
import com.hfad.fosadmin.Interface.ItemClickListener;
import com.hfad.fosadmin.Model.Request;
import com.hfad.fosadmin.ViewHolder.OrderViewHolder;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderSatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;
    FirebaseDatabase db;
    DatabaseReference request;

    MaterialSpinner spinner;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_satus);

        //Firebase
        db = FirebaseDatabase.getInstance();
        request = db.getReference("Request");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders(); //load all Orders


    }

    private void loadOrders(){

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                request
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Just Implement it to fix Crash when Click to this item

                        Intent trackingOrder = new Intent(OrderSatus.this,TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });

            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    //Press Ctrl + O


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE))
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        else if(item.getTitle().equals(Common.DELETE))
            deleteOrder(adapter.getRef(item.getOrder()).getKey());

        return super.onContextItemSelected(item);
    }


    private void showUpdateDialog (String key, final Request item){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderSatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please Choose Status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = (MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","On my way","Shipped");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                request.child(localKey).setValue(item);
            }

        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();

    }


    private void deleteOrder(String key){
        request.child(key).removeValue();

    }


}
