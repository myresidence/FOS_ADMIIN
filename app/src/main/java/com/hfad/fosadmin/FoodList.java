package com.hfad.fosadmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hfad.fosadmin.Common.Common;
import com.hfad.fosadmin.Interface.ItemClickListener;
import com.hfad.fosadmin.Model.Category;
import com.hfad.fosadmin.Model.Food;
import com.hfad.fosadmin.ViewHolder.FoodViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;

import static com.hfad.fosadmin.Common.Common.PICK_IMAGE_REQUEST;
import static com.hfad.fosadmin.R.id.btnSelect1;
import static com.hfad.fosadmin.R.id.btnUpload1;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;


    RelativeLayout rootLayout;


    FloatingActionButton fab;


    //Firebase
    FirebaseDatabase db;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;


    String categoryId="";


    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //Add New Food
    MaterialEditText edtName, edtDescription, edtPrice,edtDiscount;
    FButton btnSelect,btnUpload;

    Food newFood;

    Uri saveUri;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);


        //Firebase
        db = FirebaseDatabase.getInstance();
        foodList = db.getReference("Food");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        //Init
        recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout =(RelativeLayout)findViewById(R.id.rootLayout);


        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Code Late
                showAddFoodDialog();

            }
        });

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if (!categoryId.isEmpty())
            loadListFood(categoryId);


    }

    private void showAddFoodDialog(){
        //Copy Code From Home activity

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Add new Food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = (MaterialEditText)add_menu_layout.findViewById(R.id.edtName1);
        edtDescription = (MaterialEditText)add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = (MaterialEditText)add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = (MaterialEditText)add_menu_layout.findViewById(R.id.edtDiscount);

        btnSelect = (FButton)add_menu_layout.findViewById(btnSelect1);
        btnUpload = (FButton)add_menu_layout.findViewById(btnUpload1);

        //Event for Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();  //Copy From Home Activity
            }
        });



        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();  //Copy From Home Activity
            }
        });




        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.cart);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Here , just create new category
                if (newFood != null){

                    foodList.push().setValue(newFood);
                    //Toast.makeText(Home.this, "New category "+newCategory, Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootLayout,"New Category "+newFood.getName()+" was added",Snackbar.LENGTH_SHORT).show();
                }


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



    private void uploadImage(){

        if(saveUri != null){

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this,"Uploaded !!!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Set value for newCategory if image upload and we can get download link
                            newFood = new Food();
                            newFood.setName(edtName.getText().toString());
                            newFood.setDescription(edtDescription.getText().toString());
                            newFood.setPrice(edtPrice.getText().toString());
                            newFood.setDiscount(edtDiscount.getText().toString());
                            newFood.setMenuId(categoryId);
                            newFood.setImage(uri.toString());


                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //Dont worry about this error

                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded "+progress+"%");
                }
            });

        }



    }



    private void chooseImage(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);







    }



    private void loadListFood(String categoryId){
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.food_image);


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Code Late
                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


    //Press Crtl+O


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            saveUri = data.getData();
            btnSelect.setText("Image Selected !");


        }

    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));


        }else if (item.getTitle().equals(Common.DELETE)){

            deleteFood(adapter.getRef(item.getOrder()).getKey());


        }
        return super.onContextItemSelected(item);
    }


    private void showUpdateFoodDialog(final String key, final Food item){

        //Just Copy Code from ShowCreateFood method

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Edit Food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = (MaterialEditText)add_menu_layout.findViewById(R.id.edtName1);
        edtDescription = (MaterialEditText)add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = (MaterialEditText)add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = (MaterialEditText)add_menu_layout.findViewById(R.id.edtDiscount);

        //Set default value for view
        edtName.setText(item.getName());
        edtDiscount.setText(item.getDiscount());
        edtPrice.setText(item.getPrice());
        edtDescription.setText(item.getDescription());

        btnSelect = (FButton)add_menu_layout.findViewById(btnSelect1);
        btnUpload = (FButton)add_menu_layout.findViewById(btnUpload1);

        //Event for Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();  //Copy From Home Activity
            }
        });



        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);  //Copy From Home Activity
            }
        });




        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.cart);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();


                //Here , just create new category



                    //Update information
                    item.setName(edtName.getText().toString());
                    item.setPrice(edtPrice.getText().toString());
                    item.setDiscount(edtDiscount.getText().toString());
                    item.setDescription(edtDescription.getText().toString());



                    foodList.child(key).setValue(item);

                    //Toast.makeText(Home.this, "New category "+newCategory, Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootLayout,"Food "+item.getName()+" was edited",Snackbar.LENGTH_SHORT).show();



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



    private void changeImage(final Food item){

        if(saveUri != null){

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this,"Uploaded !!!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Set value for newCategory if image upload and we can get download link
                            item.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //Dont worry about this error

                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded "+progress+"%");
                }
            });

        }



    }


    private void deleteFood(String key){
        foodList.child(key).removeValue();
    }


}
