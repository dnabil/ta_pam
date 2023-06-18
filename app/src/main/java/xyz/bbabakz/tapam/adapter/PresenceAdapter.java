package xyz.bbabakz.tapam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Presence;

public class PresenceAdapter extends RecyclerView.Adapter<PresenceAdapter.PresenceViewHolder> {

    List<Presence> presenceList;
    Context context;

    public PresenceAdapter(Context ctx, List<Presence> presenceList){
        this.context = ctx;
        this.presenceList = presenceList;
    }

    @NonNull
    @Override
    public PresenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_presence, parent, false);
        return new PresenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresenceViewHolder holder, int position) {
        if (presenceList != null) {
            holder.tvTitle.setText(presenceList.get(position).getTitle());
            holder.tvId.setText(presenceList.get(position).getId());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(presenceList.get(position).getCreatedAt().toDate());
            holder.tvPresenceTime.setText(formattedDate);
        }
    }

    @Override
    public int getItemCount() {
        return presenceList.size();
    }

    class PresenceViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvPresenceTime, tvId;
        CardView cardView;
        ImageView btnMore;
        public PresenceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_presenceTitle);
            tvPresenceTime = itemView.findViewById(R.id.tv_presenceTime);
            tvId = itemView.findViewById(R.id.tv_presenceId);
            btnMore = itemView.findViewById(R.id.btn_presenceMore);
            btnMore.setOnClickListener(view -> {
                if (moreBtnClickListener != null)
                    moreBtnClickListener.onClick(getAdapterPosition(), view);
            });

            cardView = itemView.findViewById(R.id.cv_itemRoom);
            cardView.setOnClickListener(view -> {
                if (cardViewClickListener != null){
                    cardViewClickListener.onClick(getAdapterPosition(), view);
                }
            });

        }
    }

    public interface OnClickListener{
        void onClick(int position, View v);
    }

    static PresenceAdapter.OnClickListener moreBtnClickListener; // when clicked, goes to the room details activity
    public void setMoreBtnOnClickListener(PresenceAdapter.OnClickListener onClickListener) {
        moreBtnClickListener = onClickListener;
    }
    static PresenceAdapter.OnClickListener cardViewClickListener; // when clicked, goes to the room details activity
    public void setCardViewClickListener(PresenceAdapter.OnClickListener onClickListener) {
        cardViewClickListener = onClickListener;
    }
}
