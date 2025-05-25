package model;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String id;
    private String title;        // 標題
    private String location;     // 地點
    private String time;         // 時間
    private Organizer organizer;   // 主辦單位
    private int capacity;        // 名額
    private List<Student> participants;

    // 建構子
    public Event(String id, String title, String location, String time, int capacity,Organizer organizer) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.time = time;
        this.capacity = capacity;
        this.organizer = organizer;
        this.participants = new ArrayList<>();
    }


    // Getter 和 Setter 方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Organizer getOrganizer() { return organizer; }
    public void setOrganizer(Organizer organizer) { this.organizer = organizer; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<Student> getParticipants() { return participants; }
    public void setParticipants(List<Student> participants) { this.participants = participants; }
}