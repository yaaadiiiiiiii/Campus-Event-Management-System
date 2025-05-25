package model;

import java.util.ArrayList;
import java.util.List;


public class Student extends User {
    private List<Event> registeredEvents = new ArrayList<>();
    public Student(String id, String name, String password) {
        super(id, name, password);
    }
    @Override
    public void displayMenu() { /* 不用特別寫內容，可留空 */ }
}

