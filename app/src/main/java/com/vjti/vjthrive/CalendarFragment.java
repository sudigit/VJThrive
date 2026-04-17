package com.vjti.vjthrive;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vjti.vjthrive.models.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private CalendarView calendarView;
    private RecyclerView rvSelectedDateEvents;
    private TextView tvNoEvents;
    private ProgressBar pbCalendar;
    
    private EventAdapter adapter;
    private List<Event> allEvents;
    private List<Event> selectedDateEvents;
    private FirebaseFirestore db;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        rvSelectedDateEvents = view.findViewById(R.id.rvSelectedDateEvents);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        pbCalendar = view.findViewById(R.id.pbCalendar);

        rvSelectedDateEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        selectedDateEvents = new ArrayList<>();
        adapter = new EventAdapter(selectedDateEvents);
        rvSelectedDateEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        allEvents = new ArrayList<>();

        calendarView.setOnDayClickListener(eventDay -> {
            filterEventsByDate(eventDay.getCalendar());
        });

        fetchEvents();
    }

    private com.google.firebase.firestore.ListenerRegistration eventListener;

    private void fetchEvents() {
        pbCalendar.setVisibility(View.VISIBLE);
        tvNoEvents.setVisibility(View.GONE);

        Log.d(TAG, "Setting up real-time event listener on 'events' collection");

        eventListener = db.collection("events")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        pbCalendar.setVisibility(View.GONE);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        pbCalendar.setVisibility(View.GONE);
                        allEvents.clear();
                        List<EventDay> eventDays = new ArrayList<>();
                        
                        Log.d(TAG, "Number of events fetched for calendar: " + queryDocumentSnapshots.size());

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Event event = doc.toObject(Event.class);
                                allEvents.add(event);
                                
                                Calendar calendar = Calendar.getInstance();
                                if (event.getEventDate() != null) {
                                    calendar.setTime(event.getEventDate().toDate());
                                    
                                    // Normalize calendar to ignore time
                                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                                    calendar.set(Calendar.MINUTE, 0);
                                    calendar.set(Calendar.SECOND, 0);
                                    calendar.set(Calendar.MILLISECOND, 0);
                                    
                                    Log.d(TAG, "Extracted date for marker: " + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH)+1) + "/" + calendar.get(Calendar.YEAR));
                                    
                                    // Add marker to the day
                                    eventDays.add(new EventDay(calendar, R.drawable.ic_dot));
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parsing event document: " + doc.getId(), ex);
                            }
                        }
                        
                        calendarView.setEvents(eventDays);
                        
                        // Show events for current selected day
                        filterEventsByDate(calendarView.getSelectedDate());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private void filterEventsByDate(Calendar selectedDate) {
        selectedDateEvents.clear();
        
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);
        int month = selectedDate.get(Calendar.MONTH);
        int year = selectedDate.get(Calendar.YEAR);

        Log.d(TAG, "Filtering events for date: " + day + "/" + (month+1) + "/" + year);

        for (Event event : allEvents) {
            if (event.getEventDate() != null) {
                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(event.getEventDate().toDate());
                
                if (eventCal.get(Calendar.DAY_OF_MONTH) == day &&
                    eventCal.get(Calendar.MONTH) == month &&
                    eventCal.get(Calendar.YEAR) == year) {
                    selectedDateEvents.add(event);
                }
            }
        }

        adapter.updateData(selectedDateEvents);

        if (selectedDateEvents.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            tvNoEvents.setText("No events for this day");
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }
    }
}
