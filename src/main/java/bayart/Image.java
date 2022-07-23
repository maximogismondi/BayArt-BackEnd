package bayart;

import java.util.Date;
import java.util.ArrayList;

public class Image {
    private Integer idImage;
    private Integer idUser;
    private String name;
    private Integer price;
    private Date postDate;
    private String description;
    private ArrayList<String> tags;

    public Image(Integer idImage, Integer idUser, String name, Integer price, Date postDate, String description, ArrayList<String> tags) {
        this.idImage = idImage;
        this.idUser = idUser;
        this.name = name;
        this.price = price;
        this.postDate = postDate;
        this.description = description;
        this.tags = tags;
    }

    public Integer getIdImage() {
        return idImage;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public Date getPostDate() {
        return postDate;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setIdImage(Integer idImage) {
        this.idImage = idImage;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
}