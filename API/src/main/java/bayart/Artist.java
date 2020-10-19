package bayart;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

public class Artist extends User{
	private ArrayList<Image> imageList;
    private Boolean notificationsNewSub;
    private Boolean notificationsSell;
    private Boolean notificationsSponsor;

    public Artist(ArrayList<Image> imageList, Boolean notificationsNewSub, Boolean notificationsSell, Boolean notificationsSponsor) {

        this.imageList = imageList;
        this.notificationsNewSub = notificationsNewSub;
        this.notificationsSell = notificationsSell;
        this.notificationsSponsor = notificationsSponsor;
    }

    public ArrayList<Image> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<Image> imageList) {
        this.imageList = imageList;
    }

    public Boolean getNotificationsNewSub() {
        return notificationsNewSub;
    }

    public void setNotificationsNewSub(Boolean notificationsNewSub) {
        this.notificationsNewSub = notificationsNewSub;
    }

    public Boolean getNotificationsSell() {
        return notificationsSell;
    }

    public void setNotificationsSell(Boolean notificationsSell) {
        this.notificationsSell = notificationsSell;
    }

    public Boolean getNotificationsSponsor() {
        return notificationsSponsor;
    }

    public void setNotificationsSponsor(Boolean notificationsSponsor) {
        this.notificationsSponsor = notificationsSponsor;
    }
}

