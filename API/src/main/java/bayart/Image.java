package bayart;

import java.io.File;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

public class Image{
	private Integer idImage;
	private Integer idImageFile;
	private Integer idUser;
    private String  name;
	private Integer price;
	private Date    postDate;
	private String  description;
	private ArrayList<String> tags;
	private HashMap<Integer,HashMap<Date,String>> comments;

    public Image(Integer idImage,Integer idImageFile, Integer idUser, String name, Integer price, Date postDate, String description, ArrayList<String> tags, HashMap<Integer, HashMap<Date, String>> comments) {
        this.idImage 	 = idImage;
        this.idImageFile = idImageFile;
        this.idUser      = idUser;
        this.name 		 = name;
        this.price 		 = price;
        this.postDate 	 = postDate;
        this.description = description;
        this.tags 		 = tags;
        this.comments	 = comments;
    }

    public Integer getIdImage() {
        return idImage;
    }

    public Integer getIdImageFile() {
        return idImageFile;
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

    public HashMap<Integer, HashMap<Date, String>> getComments() {
        return comments;
    }

    public void setIdImage(Integer idImage) {
        this.idImage = idImage;
    }

    public void setIdImageFile(Integer idImageFile) {
        this.idImageFile = idImageFile;
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

    public void setComments(HashMap<Integer, HashMap<Date, String>> comments) {
        this.comments = comments;
    }
}