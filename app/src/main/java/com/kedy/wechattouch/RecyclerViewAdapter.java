package com.kedy.wechattouch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<Integer> mPlanID;
    private ArrayList<String> mPlanTitles;
    private ArrayList<Integer> mPlanIcon;
    private ArrayList<String> mPlanTime;
    private ArrayList<String> mPlanDescription;
    private OnItemClickListener mOnItemClickListener;

    public interface  OnItemClickListener{
        void onItemClick(View view ,int position);
        void onItemLongClick(View view,int position);
    }

    RecyclerViewAdapter() {
        mPlanTitles = new ArrayList<>();
        mPlanIcon = new ArrayList<>();
        mPlanID = new ArrayList<>();
        mPlanTime = new ArrayList<>();
        mPlanDescription = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.plan_icon.setImageResource(mPlanIcon.get(position));
        holder.plan_title.setText(mPlanTitles.get(position));

        if (mOnItemClickListener != null) {
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            });

            holder.item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPlanTitles.size();
    }

    void update(DatabaseHelper dbHelper, String dateStr, String status) {
        mPlanID.clear();
        mPlanTitles.clear();
        mPlanIcon.clear();
        mPlanTime.clear();
        mPlanDescription.clear();

        Map<String, List> plans = dbHelper.getDayPlans(dateStr, status);

        List IDList = plans.get("ID");
        List TitleList = plans.get("Titles");
        List IconList = plans.get("Icons");
        List TimeList = plans.get("Times");
        List DescriptionList = plans.get("Descriptions");

        assert IDList != null;
        assert TitleList != null;
        assert IconList != null;
        assert TimeList != null;
        assert DescriptionList != null;
        for (int i = 0; i < TitleList.size(); i++) {
            mPlanID.add((Integer) IDList.get(i));
            mPlanIcon.add((Integer) IconList.get(i));
            mPlanTitles.add((String) TitleList.get(i));
            mPlanTime.add((String) TimeList.get(i));
            mPlanDescription.add((String) DescriptionList.get(i));
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

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    void setStatus(DatabaseHelper dbHelper, int position, Integer status) {
        dbHelper.setPlanStatus(mPlanID.get(position), status);
        mPlanID.remove(position);
        mPlanTitles.remove(position);
        mPlanIcon.remove(position);
        mPlanTime.remove(position);
        mPlanDescription.remove(position);
        notifyItemRemoved(position);
    }

    void deletePlan(DatabaseHelper dbHelper, int position) {
        dbHelper.deletePlan(mPlanID.get(position));
        mPlanID.remove(position);
        mPlanTitles.remove(position);
        mPlanIcon.remove(position);
        mPlanTime.remove(position);
        mPlanDescription.remove(position);
        notifyItemRemoved(position);
    }

    String[] getPlanTimeAndDescription(int position) {
        return new String[] {mPlanTime.get(position), mPlanDescription.get(position)};
    }
}
