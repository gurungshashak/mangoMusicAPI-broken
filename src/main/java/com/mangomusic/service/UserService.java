package com.mangomusic.service;

import com.mangomusic.dao.UserDao;
import com.mangomusic.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userDao.searchUsers(searchTerm);
    }

    public List<User> getUsersByCountry(String country) {
        return userDao.getUsersByCountry(country);
    }

    public List<User> getUsersBySubscriptionType(String subscriptionType) {
        return userDao.getUsersBySubscriptionType(subscriptionType);
    }

    public User createUser(User user) {
        validateUser(user);

        if (user.getSignupDate() == null) {
            user.setSignupDate(LocalDate.now());
        }

        return userDao.createUser(user);
    }

    public User updateUser(int userId, User user) {
        validateUser(user);
        return userDao.updateUser(userId, user);
    }

    public boolean deleteUser(int userId) {
        return userDao.deleteUser(userId);
    }

    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (user.getSubscriptionType() != null) {
            String subType = user.getSubscriptionType().toLowerCase();
            if (!subType.equals("free") && !subType.equals("premium")) {
                throw new IllegalArgumentException("Subscription type must be 'free' or 'premium'");
            }
        }

        if (user.getCountry() == null || user.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
    }


    public Map<String, Object> getListeningStreak(int userId) {

        User user = userDao.getUserById(userId);
        if (user == null) return null;

        List<LocalDate> playDates = userDao.getUserPlayDates(userId);

        if (playDates.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("username", user.getUsername());
            result.put("currentStreak", 0);
            result.put("longestStreak", 0);
            result.put("lastPlayDate", null);
            return result;
        }

        int currentStreak = 1;
        int longestStreak = 1;

        LocalDate previous = playDates.get(0);
        LocalDate today = LocalDate.now();


        if (!previous.equals(today)) currentStreak = 0;

        for (int i = 1; i < playDates.size(); i++) {
            LocalDate date = playDates.get(i);

            if (previous.minusDays(1).equals(date)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = today.equals(previous) ? 1 : 0;
            }

            previous = date;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", user.getUsername());
        result.put("currentStreak", currentStreak);
        result.put("longestStreak", longestStreak);
        result.put("lastPlayDate", playDates.get(0).toString());

        return result;
    }

}