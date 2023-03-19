package com.example.projectapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.projectapp.adapters.AdapterCategory;
import com.example.projectapp.databinding.ActivityDashBoardAdminBinding;
import com.example.projectapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class  DashBoardAdminActivity extends AppCompatActivity {

    //view binding
    private ActivityDashBoardAdminBinding binding;

    //firebase auth
    private FirebaseAuth  firebaseAuth;


    //arrayList to store category
    private ArrayList<ModelCategory> categoryArrayList;
    //adapter
    private AdapterCategory adapterCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();


        //handle click , logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        //handle click, start subject add  screen
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashBoardAdminActivity.this, CategoryAddActivity.class)) ;
            }
        });

        //handle click, start pdf add screen
        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashBoardAdminActivity.this, PdfAddActivity.class));
            }
        });
    }

    private void loadCategories() {
        //init arraylist
        categoryArrayList = new ArrayList<>();

        //get all categories from firebase > Categories
        DatabaseReference  ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist before adding data into it
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    //add to arraylist
                    categoryArrayList.add(model);

                }
                //setup adapter
                adapterCategory = new AdapterCategory(DashBoardAdminActivity.this,categoryArrayList);
                //set adapter to recycle view
                binding.categoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        //getCurrentUser
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //if not logged in , go to the main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else {
            //logged in, get user info
            String email = firebaseUser.getEmail();

            binding.subtitleIv.setText(email);
        }
    }
}