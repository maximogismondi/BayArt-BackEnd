package bayart;
import java.util.Date;
import java.util.ArrayList;


public class User {
    private Integer idUser;
    private String username;
    private String email;
    private String password;
    private Date birthDate;
    private Date inscriptionDate;
    private Integer bpoints;
    private String profilePicture;
    private Boolean theme;
    private String language;
    private Boolean notificationsNewPublication;
    private Boolean notificationsSubEnding;
    private Boolean notificationsBuyAlert;
    private Boolean notificationsInformSponsor;
    private ArrayList<Integer> subscriptions;
    private ArrayList<ArrayList<Integer>> sponsors;
    private ArrayList<Integer> bookmarks;
    private ArrayList<Integer> purchased;
    private ArrayList<Integer> dailyRewards;

    public User(Integer idUser, String username, String email, String password, Date birthDate, Date inscriptionDate, Integer bpoints, String profilePicture, Boolean theme, String language, Boolean notificationsNewPublication, Boolean notificationsSubEnding, Boolean notificationsBuyAlert, Boolean notificationsInformSponsor, ArrayList<Integer> subscriptions, ArrayList<ArrayList<Integer>> sponsors, ArrayList<Integer> bookmarks, ArrayList<Integer> purchased, ArrayList<Integer> dailyRewards) {
        this.idUser = idUser;
        this.username = username;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.inscriptionDate = inscriptionDate;
        this.bpoints = bpoints;
        this.profilePicture = profilePicture;
        this.theme = theme;
        this.language = language;
        this.notificationsNewPublication = notificationsNewPublication;
        this.notificationsSubEnding = notificationsSubEnding;
        this.notificationsBuyAlert = notificationsBuyAlert;
        this.notificationsInformSponsor = notificationsInformSponsor;
        this.subscriptions = subscriptions;
        this.sponsors = sponsors;
        this.bookmarks = bookmarks;
        this.purchased = purchased;
        this.dailyRewards = dailyRewards;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String eMail) {
        this.email = eMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getInscriptionDate() {
        return inscriptionDate;
    }

    public void setInscriptionDate(Date inscriptionDate) {
        this.inscriptionDate = inscriptionDate;
    }

    public Integer getBpoints() {
        return bpoints;
    }

    public void setBpoints(Integer bpoints) {
        this.bpoints = bpoints;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Boolean getTheme() {
        return theme;
    }

    public void setTheme(Boolean theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getNotificationsNewPublication() {
        return notificationsNewPublication;
    }

    public void setNotificationsNewPublication(Boolean notificationsNewPublication) {
        this.notificationsNewPublication = notificationsNewPublication;
    }

    public Boolean getNotificationsSubEnding() {
        return notificationsSubEnding;
    }

    public void setNotificationsSubEnding(Boolean notificationsSubEnding) {
        this.notificationsSubEnding = notificationsSubEnding;
    }

    public Boolean getNotificationsBuyAlert() {
        return notificationsBuyAlert;
    }

    public void setNotificationsBuyAlert(Boolean notificationsBuyAlert) {
        this.notificationsBuyAlert = notificationsBuyAlert;
    }

    public Boolean getNotificationsInformSponsor() {
        return notificationsInformSponsor;
    }

    public void setNotificationsInformSponsor(Boolean notificationsInformSponsor) {
        this.notificationsInformSponsor = notificationsInformSponsor;
    }

    public ArrayList<ArrayList<Integer>> getSponsors() {
        return sponsors;
    }

    public void setSponsors(ArrayList<ArrayList<Integer>> sponsors) {
        this.sponsors = sponsors;
    }

    public ArrayList<Integer> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<Integer> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public ArrayList<Integer> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<Integer> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public ArrayList<Integer> getPurchased() {
        return purchased;
    }

    public void setPurchased(ArrayList<Integer> purchased) {
        this.purchased = purchased;
    }

    public ArrayList<Integer> getDailyRewards() {
        return dailyRewards;
    }

    public void setDailyRewards(ArrayList<Integer> dailyRewards) {
        this.dailyRewards = dailyRewards;
    }
}