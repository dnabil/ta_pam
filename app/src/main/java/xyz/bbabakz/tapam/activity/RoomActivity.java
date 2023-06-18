package xyz.bbabakz.tapam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.adapter.PresenceAdapter;
import xyz.bbabakz.tapam.model.Presence;
import xyz.bbabakz.tapam.model.Room;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;

public class RoomActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    CollectionReference roomCollection;
    CollectionReference presenceCollection;

    private ImageView imgBarBack, imgBarMenu;
    private TextView tvBarTitle;
    String roomId;

    TextView tvRoomName, tvRoomDescription;
    ImageView imgRoomInfo;

    RecyclerView rvPresence;
    List<Presence> presenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }
        // check class id
        roomId = getIntent().getStringExtra("id");
        if (roomId == null || roomId == "") {
            Toast.makeText(getApplicationContext(), "Room doesn't exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        db = FirebaseFirestore.getInstance();
        roomCollection = db.collection(Room.COLLECTION_NAME);
        presenceCollection = db.collection(Presence.COLLECTION_NAME);

        tvRoomName = findViewById(R.id.tv_roomName);
        tvRoomDescription = findViewById(R.id.tv_roomDescription);
        imgRoomInfo = findViewById(R.id.img_roomInfo);
        imgRoomInfo.setOnClickListener(view -> {
            Intent toRoomInfoActivity = new Intent(RoomActivity.this, RoomInfoActivity.class);
            toRoomInfoActivity.putExtra("roomId", roomId);
            toRoomInfoActivity.putExtra("roomId", roomId);
            startActivity(toRoomInfoActivity);
        });

        rvPresence = findViewById(R.id.rv_presence);
        rvPresence.setHasFixedSize(true);

        // set topbar
        imgBarBack = findViewById(R.id.img_back);
        imgBarBack.setOnClickListener(view -> finish());
        imgBarMenu = findViewById(R.id.img_menu);
        imgBarMenu.setOnClickListener(this::showOwnerMenu);
        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Room");
        // ---
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }
        roomCollection
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(getApplicationContext(), "Room doesn't exists", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Room room = documentSnapshot.toObject(Room.class);
                    tvRoomName.setText(room.getName());
                    tvRoomDescription.setText(room.getDescription());

                    // load presences
                    loadPresenceList();
                })
                .addOnFailureListener(e -> {
                    String msg = "failed to get room data";
                    Log.e("tapam", msg + e);
                    Toast.makeText(RoomActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // show menu for topbar
    private void showOwnerMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_room_activity, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.item_createPresence) {
                    Intent toCreatePresenceActivity = new Intent(RoomActivity.this, CreatePresenceActivity.class);
                    toCreatePresenceActivity.putExtra("roomId", roomId);
                    startActivity(toCreatePresenceActivity);

                } else if (menuItem.getItemId() == R.id.item_deleteRoom) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
                    builder.setMessage("Are you sure you want to delete this room?");
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        //validate if it's the owner


                        // TODO: Delete action
                        // delete co-owners
                        // delete presence
                        // delete members

                        // delete room
                        roomCollection
                                .document(roomId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    Room room = documentSnapshot.toObject(Room.class);

                                    if (room.getUserId().equals(mAuth.getCurrentUser().getUid())){
                                        roomCollection
                                                .document(roomId)
                                                .delete()
                                                .addOnSuccessListener(unused -> {
                                                    String msg = "Room deleted";
                                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    String msg = "Failed deleting room, please try again later";
                                                    Log.e("tapam", String.format("%s: %s", msg, e));
                                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                                    finish();
                                                });
                                    } else {
                                        String msg = "You are not the owner of this room";
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    String msg = "Failed deleting room, please try again later";
                                    Log.e("tapam", String.format("%s: %s", msg, e));
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return false;
            }
        });
        popupMenu.show();
    }


    private void loadPresenceList() {
        presenceList = new ArrayList<>();

        presenceCollection
                .whereEqualTo("roomId", roomId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() <= 0){
                        // TODO: handle emptiness here...
                        return;
                    }

                    presenceList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Presence p = doc.toObject(Presence.class);
                        presenceList.add(p);
                    }

                    PresenceAdapter presenceAdapter = new PresenceAdapter(this, presenceList);
                    presenceAdapter.setMoreBtnOnClickListener((position, v) -> {
                        // TODO: Implement more button
                        Toast.makeText(this, "not yet implemented", Toast.LENGTH_SHORT).show();
                    });
                    presenceAdapter.setCardViewClickListener(((position, v) -> {
                        Intent toPresenceDetail = new Intent(this, PresenceDetailActivity.class);
                        toPresenceDetail.putExtra("id", presenceList.get(position).getId());
                        startActivity(toPresenceDetail);
                    }));
                    rvPresence.setAdapter(presenceAdapter);
                    rvPresence.setLayoutManager(new LinearLayoutManager(this));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to attendance data" + e, Toast.LENGTH_SHORT).show();
                    Log.e("tapam", e.toString());
                    // TODO: handle error yg lebih bagus dong
                });
        // dummy
//        Date currentDate = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(currentDate);
//        Date startAt = calendar.getTime();
//        calendar.add(Calendar.DAY_OF_YEAR, 1);
//        Date endAt = calendar.getTime();
//        for (int i = 0; i < 4; i++) {
//            presenceList.add(new Presence("id", String.format("%s%d", "Title", (i+1)), "description", "roomId",
//                    new Timestamp(startAt), new Timestamp(endAt)));
//        }
    }
}