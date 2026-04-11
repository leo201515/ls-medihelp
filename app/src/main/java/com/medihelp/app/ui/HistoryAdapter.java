package com.medihelp.app.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.medihelp.app.R;
import com.medihelp.app.model.Consultation;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<Consultation> consultations;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<Consultation> consultations) {
        this.consultations = consultations;
    }

    public void updateData(List<Consultation> newData) {
        consultations = newData;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consultation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Consultation consultation = consultations.get(position);
        holder.symptomsText.setText(consultation.getSymptoms());
        holder.adviceText.setText(consultation.getAdvice());
        holder.dateText.setText(dateFormat.format(new Date(consultation.getTimestamp())));
        holder.ageText.setText("Alter: " + consultation.getUserAge() + " Jahre");
        
        String photoPath = consultation.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (bitmap != null) {
                    holder.photoView.setImageBitmap(bitmap);
                    holder.photoContainer.setVisibility(View.VISIBLE);
                } else {
                    holder.photoContainer.setVisibility(View.GONE);
                }
            } else {
                holder.photoContainer.setVisibility(View.GONE);
            }
        } else {
            holder.photoContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return consultations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symptomsText;
        TextView adviceText;
        TextView dateText;
        TextView ageText;
        ImageView photoView;
        FrameLayout photoContainer;

        ViewHolder(View itemView) {
            super(itemView);
            symptomsText = itemView.findViewById(R.id.symptomsText);
            adviceText = itemView.findViewById(R.id.adviceText);
            dateText = itemView.findViewById(R.id.dateText);
            ageText = itemView.findViewById(R.id.ageText);
            photoView = itemView.findViewById(R.id.photoView);
            photoContainer = itemView.findViewById(R.id.photoContainer);
        }
    }
}