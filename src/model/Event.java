package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Event {
    private final SimpleStringProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty location;
    private final SimpleStringProperty time;
    private final SimpleIntegerProperty capacity;
    private Organizer organizer;

    // 完整建構子
    public Event(String id, String title, String location, String time, int capacity, Organizer organizer) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.location = new SimpleStringProperty(location);
        this.time = new SimpleStringProperty(time);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.organizer = organizer;
    }

    // 簡化建構子（用於對話框）
    public Event(String title, String location, String time, String organizer, int capacity) {
        this("", title, location, time, capacity, new Organizer("", organizer, ""));
    }

    // ID 相關方法
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    // 標題相關方法
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    // 地點相關方法
    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public SimpleStringProperty locationProperty() {
        return location;
    }

    // 時間相關方法
    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public SimpleStringProperty timeProperty() {
        return time;
    }

    // 名額相關方法
    public int getCapacity() {
        return capacity.get();
    }

    public void setCapacity(int capacity) {
        this.capacity.set(capacity);
    }

    public SimpleIntegerProperty capacityProperty() {
        return capacity;
    }

    // 主辦人相關方法
    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    @Override
    public String toString() {
        return String.format("Event{id='%s', title='%s', location='%s', time='%s', capacity=%d, organizer='%s'}",
                getId(), getTitle(), getLocation(), getTime(), getCapacity(),
                organizer != null ? organizer.getName() : "無");
    }
}