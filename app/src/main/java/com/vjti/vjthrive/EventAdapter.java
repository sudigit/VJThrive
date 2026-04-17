package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.vjti.vjthrive.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvSource.setText(event.getClubId());
        holder.tvTitle.setText(event.getTitle());
        holder.tvDateTime.setText(dateTimeFormat.format(new Date(event.getEventDateMillis())));
        holder.tvDescription.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateData(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvSource, tvTitle, tvDateTime, tvDescription;

        EventViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvSource = itemView.findViewById(R.id.tvEventSource);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
        }
    }
}
