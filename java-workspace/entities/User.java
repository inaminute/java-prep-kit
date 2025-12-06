package entities;

import java.util.Optional;

public class User {
    private String name;
    private Optional<String> email;

    public User(String name, String email) {
        this.name = name;
        this.email = Optional.ofNullable(email);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getEmail() {
        return email;
    }
}