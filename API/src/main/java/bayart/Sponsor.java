package bayart;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.Map;

public class Sponsor {
    private Integer idSponsor;
    private Integer idArtist;
    private Integer invertedPoints;
    private Float   invertPorcentage;
    private Date    sponsorDate;

    public Sponsor(Integer idSponsor, Integer idArtist, Integer invertedPoints, Float invertPorcentage, Date sponsorDate){
        this.idSponsor 		  = idSponsor;
        this.idArtist 		  = idArtist;
        this.invertedPoints   = invertedPoints;
        this.invertPorcentage = invertPorcentage;
        this.sponsorDate 	  = sponsorDate;
    }

    public Integer getIdSponsor() {
        return idSponsor;
    }

    public Integer getIdArtist() {
        return idArtist;
    }

    public Integer getInvertedPoints() {
        return invertedPoints;
    }

    public Float getInvertPorcentage() {
        return invertPorcentage;
    }

    public Date getSponsorDate() {
        return sponsorDate;
    }

    public void setIdSponsor(Integer idSponsor) {
        this.idSponsor = idSponsor;
    }

    public void setIdArtist(Integer idArtist) {
        this.idArtist = idArtist;
    }

    public void setInvertedPoints(Integer invertedPoints) {
        this.invertedPoints = invertedPoints;
    }

    public void setInvertPorcentage(Float invertPorcentage) {
        this.invertPorcentage = invertPorcentage;
    }

    public void setSponsorDate(Date sponsorDate) {
        this.sponsorDate = sponsorDate;
    }
}