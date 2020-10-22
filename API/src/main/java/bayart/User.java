package bayart;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.Map;


public class User {
    private Integer idUser;
    private String username;
    private String eMail;
    private String password;
    private Date birthDate;
    private Date inscriptionDate;
    private Integer bpoints;
    private String profilePicture;
    private Boolean libraryPrivacy;
    private Boolean historyStore;
    private Boolean theme;
    private String language;
    private Boolean notificationsNewPublication;
    private Boolean notificationsSubEnding;
    private Boolean notificationsBuyAlert;
    private Boolean notificationsInformSponsor;
    private Map<Integer, Date> subscriptions;
    private ArrayList<Sponsor> sponsors;
    private ArrayList<Integer> bookmarks;
    private ArrayList<String> history;
    private Map<Integer,Map<Date,Boolean>> purchased;

    public User(Integer idUser, String username, String eMail, String password, Date birthDate, Date inscriptionDate, Integer bpoints, String profilePicture, Boolean libraryPrivacy, Boolean historyStore, Boolean theme, String language, Boolean notificationsNewPublication, Boolean notificationsSubEnding, Boolean notificationsBuyAlert, Boolean notificationsInformSponsor, Map<Integer, Date> subscriptions, ArrayList<Sponsor> sponsors, ArrayList<Integer> bookmarks, ArrayList<String> history, Map<Integer,Map<Date,Boolean>> purchased) {
        this.idUser = idUser;
        this.username = username;
        this.eMail = eMail;
        this.password = password;
        this.birthDate = birthDate;
        this.inscriptionDate = inscriptionDate;
        this.bpoints = bpoints;
        this.profilePicture = profilePicture;
        this.libraryPrivacy = libraryPrivacy;
        this.historyStore = historyStore;
        this.theme = theme;
        this.language = language;
        this.notificationsNewPublication = notificationsNewPublication;
        this.notificationsSubEnding = notificationsSubEnding;
        this.notificationsBuyAlert = notificationsBuyAlert;
        this.notificationsInformSponsor = notificationsInformSponsor;
        this.subscriptions = subscriptions;
        this.sponsors = sponsors;
        this.bookmarks = bookmarks;
        this.history = history;
        this.purchased = purchased;
    }

    public User() {
        this.idUser = 0;
        this.username = "a";
        this.eMail = "a@a.a";
        this.password = "aaaaaaaaa";
        this.birthDate = new Date();
        this.inscriptionDate = new Date();
        this.bpoints = 0;
        this.profilePicture = "";
        this.libraryPrivacy = true;
        this.historyStore = true;
        this.theme = true;
        this.language = "";
        this.notificationsNewPublication = true;
        this.notificationsSubEnding = true;
        this.notificationsBuyAlert = true;
        this.notificationsInformSponsor = true;
        this.subscriptions = new HashMap<>();
        this.sponsors = new ArrayList<>();
        this.bookmarks = new ArrayList<>();
        this.history = new ArrayList<>();
        this.purchased = new HashMap<>();
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

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
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

    public Boolean getLibraryPrivacy() {
        return libraryPrivacy;
    }

    public void setLibraryPrivacy(Boolean libraryPrivacy) {
        this.libraryPrivacy = libraryPrivacy;
    }

    public Boolean getHistoryStore() {
        return historyStore;
    }

    public void setHistoryStore(Boolean historyStore) {
        this.historyStore = historyStore;
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

    public Map<Integer, Date> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<Integer, Date> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public ArrayList<Sponsor> getSponsors() {
        return sponsors;
    }

    public void setSponsors(ArrayList<Sponsor> sponsors) {
        this.sponsors = sponsors;
    }

    public ArrayList<Integer> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<Integer> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<String> history) {
        this.history = history;
    }

    public Map<Integer,Map<Date,Boolean>> getPurchased() {
        return purchased;
    }

    public void setPurchased(Map<Integer,Map<Date,Boolean>> purchased) {
        this.purchased = purchased;
    }
}


