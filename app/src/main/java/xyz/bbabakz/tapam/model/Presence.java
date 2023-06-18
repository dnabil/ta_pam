package xyz.bbabakz.tapam.model;

import com.google.firebase.Timestamp;

public class Presence {
    static public final String COLLECTION_NAME = "presences";
    private String id;
    private String title;
    private String description;
    private String roomId;
    private Timestamp createdAt;
    private Timestamp startAt;
    private Timestamp endAt;

    public Presence() {
    }

    public Presence(String id, String title, String description, String roomId, Timestamp startAt, Timestamp endAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.roomId = roomId;
        this.createdAt = Timestamp.now();
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static boolean validateTime(Timestamp startAt, Timestamp endAt) {
        return startAt.compareTo(endAt) < 0;
    }

    // getter and setter:

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getStartAt() {
        return startAt;
    }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

}
