package bayart;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

public class Artist extends User{
	private HashMap<Image,String> imageList;
    private Boolean notificationsNewSub;
    private Boolean notificationsSell;
    private Boolean notificationsSponsor;

    public Artist(Integer idUser, String username, String eMail, String password, Date birthDate, Date inscriptionDate, Integer bpoints, String profilePicture, Boolean libraryPrivacy, Boolean historyStore, Boolean theme, String language,Boolean notificationsNewPublication, Boolean notificationsSubEnding, Boolean notificationsBuyAlert, Boolean notificationsInformSponsor, HashMap<Integer, Date> subscriptions, HashMap<Integer, HashMap<Integer, Date>> sponsors, ArrayList<Integer> bookmarks, ArrayList<String> history, ArrayList<Integer> purchased, HashMap<Image, String> imageList, Boolean notificationsNewSub, Boolean notificationsSell, Boolean notificationsSponsor) {
        super(idUser, username, eMail, password, birthDate, inscriptionDate, bpoints, profilePicture, libraryPrivacy, historyStore, theme, language, notificationsNewPublication, notificationsSubEnding, notificationsBuyAlert, notificationsInformSponsor, subscriptions, sponsors, bookmarks, history, purchased);
        this.imageList = imageList;
        this.notificationsNewSub = notificationsNewSub;
        this.notificationsSell = notificationsSell;
        this.notificationsSponsor = notificationsSponsor;
    }

    public HashMap<Image, String> getImageList() {
        return imageList;
    }

    public void setImageList(HashMap<Image, String> imageList) {
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

