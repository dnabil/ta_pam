package xyz.bbabakz.tapam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.adapter.RoomAdapter;
import xyz.bbabakz.tapam.model.Room;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ImageView imgProfile, imgLeftMenu;
    LoadingDialogUtil loadingDialog;

    FirebaseFirestore db;
    CollectionReference roomCollection;

    RecyclerView rvRoom;
    List<Room> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }

        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        roomCollection = db.collection(Room.COLLECTION_NAME);

        loadingDialog = new LoadingDialogUtil(this);

        // room recycler view
        rvRoom = findViewById(R.id.rv_room);
        rvRoom.setHasFixedSize(true);

        imgProfile = findViewById(R.id.img_profile);
        imgLeftMenu = findViewById(R.id.img_menu);

        // ocl pergi ke profile page
        imgProfile.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // ocl tampilkan menu create room/join room
        imgLeftMenu.setOnClickListener(this::showMenu);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }

        // load profile img
        Glide.with(this)
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .circleCrop()
                .into(imgProfile);

        // load rooms data
        loadRooms();
    }
    // [END on_start_check_user]


    // (three line) top left popup menu
    private void showMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(HomeActivity.this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_home_activity, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.item_createRoom) {
                    Intent toCreateRoomActivity = new Intent(HomeActivity.this, CreateRoomActivity.class);
                    startActivity(toCreateRoomActivity);

                } else if (menuItem.getItemId() == R.id.item_joinRoom) {
                    Intent toJoinRoomActivity = new Intent(HomeActivity.this, JoinRoomActivity.class);
                    startActivity(toJoinRoomActivity);
                }
                return false;
            }
        });
        popupMenu.show();
    }


    private void loadRooms() {
        roomList = new ArrayList<Room>();

        loadingDialog.startLoading();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Task<QuerySnapshot> roomsIOwnTask = roomCollection.whereEqualTo("userId", mAuth.getCurrentUser().getUid()).get();
        Task<QuerySnapshot> roomsIJoinedTask = roomCollection.whereArrayContains("membersId", mAuth.getCurrentUser().getUid()).get();

        executor.execute(() -> {
            Tasks.whenAll(roomsIOwnTask, roomsIJoinedTask)
                    .addOnSuccessListener(unused -> {
                        QuerySnapshot roomsIOwn = roomsIOwnTask.getResult();
                        QuerySnapshot roomsIJoined = roomsIJoinedTask.getResult();

                        for (QueryDocumentSnapshot d : roomsIOwn)
                            roomList.add(d.toObject(Room.class));

                        for (QueryDocumentSnapshot d : roomsIJoined)
                            roomList.add(d.toObject(Room.class));

                        handler.post(() -> {
                            RoomAdapter roomAdapter = new RoomAdapter(HomeActivity.this, roomList, mAuth);
                            roomAdapter.setOnClickListener((position, v) -> {
                                Intent toRoomActivity = new Intent(HomeActivity.this, RoomActivity.class);
                                toRoomActivity.putExtra("id", roomList.get(position).getId());
                                startActivity(toRoomActivity);
                            });
                            rvRoom.setAdapter(roomAdapter);
                            rvRoom.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                            loadingDialog.stopLoading();
                        });
                    })
                    .addOnFailureListener(e -> {
                        handler.post(() -> {
                            Toast.makeText(getApplicationContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
                            Log.e("tapam", e.toString());
                            loadingDialog.stopLoading();
                        });
                    });
        }); // end of execute
        executor.shutdown();
    }

}