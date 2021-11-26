package com.chatApplication.chatClient.muc;

public interface UserAvailabilityListener {

    void availabilityStatus(String login, String newStatus);
}
