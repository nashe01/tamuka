package com.kodelink.glide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.RideHistoryViewHolder> {
    private List<RideHistoryItem> rideHistoryList;

    public RideHistoryAdapter(List<RideHistoryItem> rideHistoryList) {
        this.rideHistoryList = rideHistoryList;
    }

    @NonNull
    @Override
    public RideHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new RideHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideHistoryViewHolder holder, int position) {
        RideHistoryItem item = rideHistoryList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return rideHistoryList.size();
    }

    public static class RideHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvTime;
        private TextView tvPickupLocation;
        private TextView tvDestinationLocation;
        private TextView tvStatus;
        private TextView tvPeople;
        private TextView tvPrice;
        private TextView tvUserInfo;

        public RideHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            tvDestinationLocation = itemView.findViewById(R.id.tvDestinationLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPeople = itemView.findViewById(R.id.tvPeople);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvUserInfo = itemView.findViewById(R.id.tvUserInfo);
        }

        public void bind(RideHistoryItem item) {
            // Format date and time
            Date date = new Date(item.timestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            tvDate.setText(dateFormat.format(date));
            tvTime.setText(timeFormat.format(date));

            // Set locations
            tvPickupLocation.setText(item.pickupLocation);
            tvDestinationLocation.setText(item.destinationLocation);

            // Set status with enhanced color handling
            tvStatus.setText(item.getStatusDisplayText());
            setStatusColor(item.status);

            // Set people and price
            tvPeople.setText(item.people + " person" + (item.people > 1 ? "s" : ""));
            
            // Enhanced price display
            if (item.totalPrice != null && !item.totalPrice.isEmpty()) {
                tvPrice.setText("Total: $" + item.totalPrice);
            } else {
                tvPrice.setText("$" + String.format("%.2f", item.priceEach) + " each");
            }

            // Set user info with names if available
            tvUserInfo.setText(item.getOtherPartyName());
        }
        
        private void setStatusColor(String status) {
            int color;
            switch (status.toLowerCase()) {
                case "completed":
                    color = itemView.getContext().getColor(android.R.color.holo_green_dark);
                    break;
                case "declined":
                    color = itemView.getContext().getColor(android.R.color.holo_red_dark);
                    break;
                case "cancelled":
                    color = itemView.getContext().getColor(android.R.color.holo_red_dark);
                    break;
                case "timeout":
                    color = itemView.getContext().getColor(android.R.color.holo_orange_dark);
                    break;
                case "pending":
                    color = itemView.getContext().getColor(android.R.color.holo_orange_dark);
                    break;
                case "accepted":
                    color = itemView.getContext().getColor(android.R.color.holo_blue_dark);
                    break;
                default:
                    color = itemView.getContext().getColor(android.R.color.black);
                    break;
            }
            tvStatus.setTextColor(color);
        }
    }
}
