package com.example.adhd_monitor;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String username;
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public String securityQuestion;
    public String securityAnswer;


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(){

    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setSecurityQuestion(String question) {
        this.securityQuestion = question;
    }

    public void setSecurityAnswer(String answer) {
        this.securityAnswer = answer;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }
}


