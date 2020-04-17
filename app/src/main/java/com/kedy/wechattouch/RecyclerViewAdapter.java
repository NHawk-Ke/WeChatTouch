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

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mPlanDescriptions = new ArrayList<>();
    private ArrayList<Integer> mPlanIcon = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> planDescriptions, ArrayList<Integer> planIcon) {
        mContext = context;
        mPlanDescriptions = planDescriptions;
        mPlanIcon = planIcon;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_detail_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.plan_icon.setImageResource(mPlanIcon.get(position));
        holder.plan_description.setText(mPlanDescriptions.get(position));

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + mPlanDescriptions.get(position));

                Toast.makeText(mContext, mPlanDescriptions.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlanDescriptions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView plan_icon;
        TextView plan_description;
        RelativeLayout item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plan_icon = itemView.findViewById(R.id.plan_icon);
            plan_description = itemView.findViewById(R.id.plan_description);
            item = itemView.findViewById(R.id.date_detail_item);
        }
    }
}
