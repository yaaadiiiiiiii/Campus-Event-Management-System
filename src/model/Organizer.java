package model;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class Organizer extends User {
    private List<Event> hostedEvents = new ArrayList<>();
    public Organizer(String id, String name, String password) {
        super(id, name, password);
    }

    public void createEvent(String id, String title, String location, String time, int capacity) {
        // 1. 建立活動物件，將自己(主辦人)作為 organizer 傳入
        Event event = new Event(id, title, location, time, capacity, this);

        // 2. 加進自己的活動列表
        hostedEvents.add(event);

        // 3. 同步寫入 events.csv
        try (FileWriter fw = new FileWriter("events.csv", true)) {
            // 格式：活動ID,標題,地點,時間,容量,主辦人學號
            String line = id + "," + title + "," + location + "," + time + "," + capacity + "," + this.getId() + "\n";
            fw.write(line);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void editEvent(String eventId, String newTitle, String newLocation, String newTime, int newCapacity) {
        // 1. 先在自己的活動清單找到該活動，修改屬性
        for (Event event : hostedEvents) {
            if (event.getId().equals(eventId)) {
                event.setTitle(newTitle);
                event.setLocation(newLocation);
                event.setTime(newTime);
                event.setCapacity(newCapacity);
                break;
            }
        }

        // 2. 重新寫入 events.csv（這裡用全檔覆蓋的方式較簡單）
        try {
            // 2-1. 先把所有活動從 events.csv 讀出
            List<String> allEvents = new ArrayList<>();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("events.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    // 檔案格式：活動ID,標題,地點,時間,容量,主辦人學號
                    if (data[0].equals(eventId) && data[5].equals(this.getId())) {
                        // 這就是要編輯的那筆，換成新的
                        line = eventId + "," + newTitle + "," + newLocation + "," + newTime + "," + newCapacity + "," + this.getId();
                    }
                    allEvents.add(line);
                }
            }
            // 2-2. 覆蓋寫回 events.csv
            try (FileWriter fw = new FileWriter("events.csv", false)) {
                for (String l : allEvents) {
                    fw.write(l + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void displayMenu() { /* 不用特別寫內容，可留空 */ }
}
