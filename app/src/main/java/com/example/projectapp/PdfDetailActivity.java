package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.projectapp.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    //view binding

    private ActivityPdfDetailBinding binding;

    //pdf id get from intent
    String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent e.g. bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        loadBookDetails();
        //increment book view count, whenever this page starts

        MyApplication.incrementBookCount(bookId);

        //handle click, go back
        binding.backBtn.setOnClickListener((view)-> {
            onBackPressed();
        });

    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Projects");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        String url = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryIv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+url,
                                ""+title,
                                binding.pdfView,
                                binding.progressBar
                        );

                        MyApplication.loadPdfSize(
                                ""+url,
                                ""+title,
                                binding.sizeIv
                        );

                        //set data
                        binding.titleIv.setText(title);
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
}