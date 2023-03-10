package com.example.projectapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.MyApplication;
import com.example.projectapp.databinding.RowPdfAdminBinding;
import com.example.projectapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> {

    //context
    private Context context;

    //arraylist to hold list of data of type ModelPdf
    private ArrayList<ModelPdf> pdfArrayList;

    //view binding row_pdf_admin.xml
    private RowPdfAdminBinding binding;

    //constructor
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind layout using view binding
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        /* Get data, set data, handle clicks  etc. */

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        long timestamp = model.getTimestamp();

        //we need to convert timestamp  to dd/mm/yyyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleIv.setText(title);
        holder.descriptionIv.setText(description);
        holder.dateIv.setText(formattedDate);

        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);
        loadPdfSize(model, holder);

    }
    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder) {

        String pdfUrl = model.getUrl();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes
                       double bytes = storageMetadata.getSizeBytes();
                       //convert bytes into KB, MB
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb >= 1){
                            holder.sizeIv.setText(String.format("%.2f",mb)+" MB");
                        }
                        else if (kb >= 1){
                            holder.sizeIv.setText(String.format("%.2f",kb)+" KB");
                        }
                        else {
                            holder.sizeIv.setText(String.format("%.2f",bytes)+" Bytes");
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
    }

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    /* view holder class for row_pdf_admin.xml */
    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        //  UI views of row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleIv,descriptionIv,categoryIv,sizeIv,dateIv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            //init ui views
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleIv = binding.titleIv;
            descriptionIv = binding.descriptionIv;
            categoryIv = binding.categoryIv;
            sizeIv = binding.sizeIv;
            dateIv = binding.dateIv;
            moreBtn = binding.moreBtn;
        }
    }
}
