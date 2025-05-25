public class Event {
    private String title;        // 標題
    private String location;     // 地點
    private String time;         // 時間
    private String organizer;    // 主辦單位
    private int capacity;        // 名額

    // 建構子
    public Event(String title, String location, String time, String organizer, int capacity) {
        this.title = title;
        this.location = location;
        this.time = time;
        this.organizer = organizer;
        this.capacity = capacity;
    }

    // Getter 和 Setter 方法
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}