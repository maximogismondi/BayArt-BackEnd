package bayart;

import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

public class Image{
	private Integer idImage;
	private String name;
	private Integer price;
	private Date    postDate;
	private String  description;
	private String  url;
	private ArrayList<String> tags;
	private HashMap<Integer,HashMap<Date,String>> comments;

    public Image(Integer idImage, String name, Integer price, Date postDate, String description, String url, ArrayList<String> tags, HashMap<Integer, HashMap<Date, String>> comments) {
        this.idImage 	 = idImage;
        this.name 		 = name;
        this.price 		 = price;
        this.postDate 	 = postDate;
        this.description = description;
        this.url 		 = url;
        this.tags 		 = tags;
        this.comments	 = comments;
    }

    public Integer getIdImage() {
        return idImage;
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

    public String getUrl() {
        return url;
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setComments(HashMap<Integer, HashMap<Date, String>> comments) {
        this.comments = comments;
    }
}