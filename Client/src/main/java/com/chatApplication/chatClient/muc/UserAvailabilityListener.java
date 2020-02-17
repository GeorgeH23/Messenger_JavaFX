package com.chatApplication.chatClient.muc;

public interface UserAvailabilityListener {

    public void availabilityStatus(String login, String newStatus);
}
