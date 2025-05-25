package model;

import java.util.ArrayList;
import java.util.List;

public class Organizer extends User {
    private List<Event> hostedEvents = new ArrayList<>();
    public Organizer(String id, String name, String password) {
        super(id, name, password);
    }
    @Override
    public void displayMenu() { /* 不用特別寫內容，可留空 */ }
}
