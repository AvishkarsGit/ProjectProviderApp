package com.example.projectapp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.Filter.FilterPdfAdmin;
import com.example.projectapp.MyApplication;
import com.example.projectapp.activities.PdfDetailActivity;
import com.example.projectapp.activities.PdfEditActivity;
import com.example.projectapp.databinding.RowPdfAdminBinding;
import com.example.projectapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>  implements Filterable {

    //context
    private Context context;

    //arraylist to hold list of data of type ModelPdf
    public ArrayList<ModelPdf> pdfArrayList,filterList;

    //view binding row_pdf_admin.xml
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    //progress bar
    private ProgressDialog progressDialog;


    //constructor
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind layout using view binding
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        /* Get data, set data, handle clicks  etc. */

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();

        //we need to convert timestamp  to dd/mm/yyyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleIv.setText(title);
        holder.descriptionIv.setText(description);
        holder.dateIv.setText(formattedDate);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryIv);

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeIv);


        //handle click , show dialog with options 1) edit 2) delete
        holder.moreBtn.setOnClickListener((view)-> {
            moreOptionsDialog(model, holder);
        });

        //handle pdf click, open pdf details page, pass pdf id to get the detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });

    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        //options to show in dialog
        String[] options = {"Edit","Delete"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog options click
                        if (which == 0) {
                            //Edit clicked, open pdfEditActivity
                            Intent i = new Intent(context, PdfEditActivity.class);
                            i.putExtra("bookId",bookId);
                            context.startActivity(i);
                        }
                        else if (which == 1){
                            //Delete clicked
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
                        }
                    }
                })
                .show();
    }



    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
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
