package xyz.bbabakz.tapam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Room;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private final Context context;
    FirebaseAuth mAuth;
    private final List<Room> roomList;

    public RoomAdapter(Context ctx, List<Room> roomList, FirebaseAuth mAuth) {
        this.context = ctx;
        this.roomList = roomList;
        this.mAuth = mAuth;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_room,
                        parent,
                        false
                );
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        if (roomList != null) {
            String ownerId = roomList.get(position).getUserId();
            // TODO: cari nama owner dari id diatas, lalu letak di bawah ini
            holder.tvOwnerName.setText("Owner's name");
            holder.tvDescription.setText(roomList.get(position).getDescription());
            holder.tvRoomName.setText(roomList.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tvRoomName;
        TextView tvDescription;
        TextView tvOwnerName;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tv_roomName);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvOwnerName = itemView.findViewById(R.id.tv_ownerName);
            cv = itemView.findViewById(R.id.cv_itemRoom);

            cv.setOnClickListener(view -> {
                if (clickListener != null)
                    clickListener.onClick(getAdapterPosition(), view);
            });
        }
    }

    public interface OnClickListener{
        void onClick(int position, View v);
    }

    static OnClickListener clickListener; // when clicked, goes to the room details activity
    public void setOnClickListener(OnClickListener onClickListener) {
        clickListener = onClickListener;
    }
}
