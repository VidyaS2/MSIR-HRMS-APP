package com.attendance.clockme;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private List<CalendarDate> calendarDates;
    private Context context;

    public CalendarAdapter(Context context, List<CalendarDate> calendarDates) {
        this.context = context;
        this.calendarDates = calendarDates;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarDate calendarDate = calendarDates.get(position);
        holder.dateTextView.setText(calendarDate.getDate());

        switch (calendarDate.getStatus()) {
            case "leave":
                holder.itemView.setBackgroundColor(Color.RED);
                break;
            case "present":
                holder.itemView.setBackgroundColor(Color.GREEN);
                break;
            case "half_day":
                holder.itemView.setBackgroundColor(Color.YELLOW);
                break;
            case "holiday":
                holder.itemView.setBackgroundColor(Color.BLUE);
                break;
            default:
                holder.itemView.setBackgroundColor(Color.GRAY);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return calendarDates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
