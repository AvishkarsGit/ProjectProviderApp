package com.example.projectapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.projectapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //progress dialog

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String name = "", email ="",password="";
    private void validateData() {
        // Before creating account lets do some validation
        name = binding.edtName.getText().toString().trim();
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        String confirmPass = binding.edtConfirmPass.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Enter Your Name", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter Correct Email!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmPass)){
            Toast.makeText(this, "Enter Confirm Password", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmPass)){
            Toast.makeText(this, "password doesn't match!!", Toast.LENGTH_SHORT).show();
        }
        else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progressDialog
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create user in firebase
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account creation success, add in firebase realtime databse
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //account creation failed..
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, " "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");

        //timestamp

        long timestamp = System.currentTimeMillis();

        //get current user id
        String uid = firebaseAuth.getUid();

        //set up data to add in database;

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage","");
        hashMap.put("userType","user");
        hashMap.put("timestamp",timestamp);

        //set data in db
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users");
        dbRef.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data add  to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account created..", Toast.LENGTH_SHORT).show();
                        startActivity( new Intent(RegisterActivity.this, DashBoardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data failed adding to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, " "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}