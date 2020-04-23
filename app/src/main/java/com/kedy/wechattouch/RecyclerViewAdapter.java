package com.kedy.wechattouch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mPlanTitles;
    private ArrayList<Integer> mPlanIcon;
    private Context mContext;

    public RecyclerViewAdapter(Context context) {
        mContext = context;
        mPlanTitles = new ArrayList<>();
        mPlanIcon = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.plan_icon.setImageResource(mPlanIcon.get(position));
        holder.plan_title.setText(mPlanTitles.get(position));

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + mPlanTitles.get(position));

                Toast.makeText(mContext, mPlanTitles.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlanTitles.size();
    }

    void update(DatabaseHelper dbHelper, String dateStr) {
        mPlanIcon.clear();
        mPlanTitles.clear();

        Map<String, List> plans = dbHelper.getDayPlans(dateStr);

        List TitleList = plans.get("Titles");
        List IconList = plans.get("Icons");
        List TimeList = plans.get("Times");
        List DescriptionList = plans.get("Descriptions");

        assert TitleList != null;
        assert IconList != null;
        assert TimeList != null;
        assert DescriptionList != null;
        for (int i = 0; i < TitleList.size(); i++) {
            mPlanIcon.add((Integer) IconList.get(i));
            mPlanTitles.add((String) TitleList.get(i));
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView plan_icon;
        TextView plan_title;
        RelativeLayout item;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            plan_icon = itemView.findViewById(R.id.planIcon);
            plan_title = itemView.findViewById(R.id.planTitle);
            item = itemView.findViewById(R.id.dateDetailItem);
        }
    }
}
