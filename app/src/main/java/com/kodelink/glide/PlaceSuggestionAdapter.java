package com.kodelink.glide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.ArrayList;
import java.util.List;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.SuggestionViewHolder> {
    
    private List<AutocompletePrediction> suggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;
    
    public interface OnSuggestionClickListener {
        void onSuggestionClick(AutocompletePrediction prediction);
    }
    
    public PlaceSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }
    
    public void updateSuggestions(List<AutocompletePrediction> newSuggestions) {
        this.suggestions.clear();
        this.suggestions.addAll(newSuggestions);
        notifyDataSetChanged();
    }
    
    public void clearSuggestions() {
        this.suggestions.clear();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        AutocompletePrediction prediction = suggestions.get(position);
        
        // Set primary text (main place name)
        String primaryText = prediction.getPrimaryText(null).toString();
        holder.tvPrimaryText.setText(primaryText);
        
        // Set secondary text (address details)
        String secondaryText = prediction.getSecondaryText(null).toString();
        holder.tvSecondaryText.setText(secondaryText);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(prediction);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return suggestions.size();
    }
    
    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceIcon;
        TextView tvPrimaryText;
        TextView tvSecondaryText;
        
        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceIcon = itemView.findViewById(R.id.ivPlaceIcon);
            tvPrimaryText = itemView.findViewById(R.id.tvPrimaryText);
            tvSecondaryText = itemView.findViewById(R.id.tvSecondaryText);
        }
    }
}
