package com.attendance.clockme;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView header;
    private ImageView prevButton, nextButton;
    private Calendar calendar;
    private int currentYear, currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        gridLayout = findViewById(R.id.gridLayout);
        header = findViewById(R.id.header);
        prevButton = findViewById(R.id.calendar_previous);
        nextButton = findViewById(R.id.calendar_next);

        calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        // Initial calendar display
        displayCalendar(currentYear, currentMonth);

        // Set up button listeners
        prevButton.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11; // December
                currentYear--; // Previous year
            }
            displayCalendar(currentYear, currentMonth);
        });

        nextButton.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0; // January
                currentYear++; // Next year
            }
            displayCalendar(currentYear, currentMonth);
        });
    }

    private void displayCalendar(int year, int month) {
        gridLayout.removeAllViews(); // Clear previous views
        header.setText(String.format("%tB %d", calendar, year)); // Display month and year

        calendar.set(year, month, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday, etc.

        // Add empty views for days before the first day of the month
        for (int i = 1; i < firstDayOfWeek; i++) {
            TextView emptyCell = new TextView(this);
            emptyCell.setLayoutParams(new GridLayout.LayoutParams());
            gridLayout.addView(emptyCell); // Empty cell
        }

        // Add views for each day in the month
        for (int day = 1; day <= daysInMonth; day++) {
            CardView dayCard = new CardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(8, 8, 8, 8); // Add margins for spacing
            dayCard.setLayoutParams(params);
            dayCard.setCardElevation(8);
            dayCard.setRadius(16);
            dayCard.setBackgroundColor(Color.WHITE); // Set the background color

            TextView dayView = new TextView(this);
            dayView.setLayoutParams(new GridLayout.LayoutParams());
            dayView.setText(String.valueOf(day)); // Set day number
            dayView.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center the text
            dayView.setTextSize(20); // Adjust text size
            dayCard.addView(dayView); // Add TextView to CardView
            gridLayout.addView(dayCard); // Add CardView to GridLayout
        }
    }
}

