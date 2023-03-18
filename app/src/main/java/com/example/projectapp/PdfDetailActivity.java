package com.example.projectapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.projectapp.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class PdfDetailActivity extends AppCompatActivity implements PaymentResultListener{

    //view binding

    private ActivityPdfDetailBinding binding;

    //pdf id get from intent
    String bookId,bookTitle,bookUrl;
    private static final String TAG_DOWNLOAD = "TAG_DOWNLOAD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(binding.getRoot());

        //get data from intent e.g. bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        binding.downloadBookBtn.setVisibility(View.GONE);

        loadBookDetails();
        //increment book view count, whenever this page starts

        MyApplication.incrementBookCount(bookId);

        //handle click, go back
        binding.backBtn.setOnClickListener((view)-> {
            onBackPressed();
        });

        binding.readBookBtn.setOnClickListener((view)->{
            Intent intent1 = new Intent(PdfDetailActivity.this,PdfViewActivity.class);
            intent1.putExtra("bookId", bookId);
            startActivity(intent1);

        });
        Checkout.preload(PdfDetailActivity.this);
        //handle click , download pdf
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });
    }

    private void download() {
        Log.d(TAG_DOWNLOAD, "onClick: Checking Permission");
        if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG_DOWNLOAD, "onClick: permission already granted,can download book");
            MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);
        }
        else {
            Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission");
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    // request storage permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted ->{
               if (isGranted ){
                   Log.d(TAG_DOWNLOAD, "Permission Granted");
                   MyApplication.downloadBook(this,""+bookId,""+bookTitle,""+bookUrl);
               }
               else {
                   Log.d(TAG_DOWNLOAD, "Permission was dennied!! : ");
                   Toast.makeText(this, "Perm                     " +
                           "" +
                           "ission was dennied!!", Toast.LENGTH_SHORT).show();
               }
            });
    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Projects");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();


                        //required data is loaded, show download button
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryIv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar
                        );

                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeIv
                        );

                        //set data
                        binding.titleIv.setText(bookTitle);
                        binding.descriptionIv.setText(description);
                        binding.viewsIv.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsIv.setText(downloadsCount.replace("null","N/A"));
                        binding.dateIv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_36LAOETlm6Di6i");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name","Razorpay Demo");
            jsonObject.put("description","This is only for Testing purpose");
//            jsonObject.put("","");
            jsonObject.put("theme.color","#3399cc");
            jsonObject.put("currency","INR");
            jsonObject.put("amount","4000");

            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled",true);
            retryObj.put("max_count",4);


            jsonObject.put("retry",retryObj);


            checkout.open(PdfDetailActivity.this,jsonObject);

        }catch (Exception e){
            Toast.makeText(PdfDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(this, "Payment Successful..", Toast.LENGTH_SHORT).show();
        download();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed due to "+s, Toast.LENGTH_SHORT).show();

    }
}