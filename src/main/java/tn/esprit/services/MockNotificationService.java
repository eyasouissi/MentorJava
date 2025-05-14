package tn.esprit.services;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.entities.Notif;

public class MockNotificationService {
    private static final ObservableList<Notif> notifications = FXCollections.observableArrayList();
    private static final IntegerProperty unreadCount = new SimpleIntegerProperty(0);

    public static void addNotification(Notif notification) {
        notifications.add(notification);
        unreadCount.set(unreadCount.get() + 1);
    }

    public static ObservableList<Notif> getNotifications() {
        return FXCollections.unmodifiableObservableList(notifications);
    }

    public static void markAllAsRead() {
        notifications.forEach(n -> n.setRead(true));
        unreadCount.set(0);
    }

    public static IntegerProperty unreadCountProperty() {
        return unreadCount;
    }
}