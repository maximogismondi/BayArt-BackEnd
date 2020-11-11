package bayart;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api")
@RestController

public class BayArtController {

    //Acceso a mongo
    @Autowired
    private AccesoMongoDB accesoABase;

    public BayArtController() {
        this.accesoABase = new AccesoMongoDB();
    }

    //lista compartida de imagenes
    private ArrayList<Image> listaImages = new ArrayList<>();

    //lista compartida de artistas
    private ArrayList<User> listaArtists = new ArrayList<>();

    //constante cantidad de imagenes que se devuelven

    static final int cantidad = 10;

    //Funciones extra

    //JOYA
    private ArrayList<User> asociarArtistas(ArrayList<Image> imagenes) {

        Map<String, String> parametros = new HashMap<>();
        ArrayList<User> artistaImagen = new ArrayList<>();

        for (Image imagen : imagenes) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(imagen.getIdUser()));
            for (int i = 0; i <= artistaImagen.size(); i++) {
                if (artistaImagen.size() == i) {
                    artistaImagen.add(accesoABase.obtenerUsuarios(parametros).get(0));
                    break;
                } else if (artistaImagen.get(i).getIdUser() == accesoABase.obtenerUsuarios(parametros).get(0).getIdUser()) {
                    break;
                }
            }
        }

        return artistaImagen;
    }

    //JOYA
    private String obtenerImagenPerfil(Integer idUser) {
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", Integer.toString(idUser));

        return accesoABase.obtenerImagenEnMongoDB("imageProfile" + idUser);
    }

    //JOYA
    private String obtenerImagenBanner(Integer idUser) {
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", Integer.toString(idUser));

        return accesoABase.obtenerImagenEnMongoDB("imageBanner" + idUser);
    }

    //JOYA
    public ArrayList<String> desconcatenarFiltros(String filters) {

        ArrayList<String> filtros = new ArrayList<>();
        String filtro = "";
        filters += ",";

        for (int i = 0; i < filters.length(); i++) {
            if (filters.charAt(i) == ',') {
                filtros.add(filtro);
                filtro = "";
            } else {
                filtro += filters.charAt(i);
            }
        }
        return filtros;

    }

    //JOYA
    private Integer daysFromWeekStart() {
        switch (LocalDate.now().getDayOfWeek().toString()) {
            case "MONDAY":
                return 0;
            case "TUESDAY":
                return 1;
            case "WEDNESDAY":
                return 2;
            case "THURSDAY":
                return 3;
            case "FRIDAY":
                return 4;
            case "SATURDAY":
                return 5;
            case "SUNDAY":
                return 6;
        }
        return 0;
    }

    //Login - JOYA
    @RequestMapping(path = "/logIn/{username}/{password}", method = RequestMethod.GET)
    public ResponseEntity<Object> confirmLogInData(@PathVariable String username,
                                                   @PathVariable String password) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("username", username);
        parametros.put("password", password);

        if (accesoABase.obtenerUsuarios(parametros).size() == 0) {
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        } else {
            User usuario = accesoABase.obtenerUsuarios(parametros).get(0);
            infoResponse.put("user", usuario);
            infoResponse.put("encodedProfilePicture", obtenerImagenPerfil(usuario.getIdUser()));

            parametros.clear();
            parametros.put("idUser", Integer.toString(usuario.getIdUser()));
            if (accesoABase.obtenerArtistas(parametros).size() != 0) {
                infoResponse.put("artist", accesoABase.obtenerArtistas(parametros).get(0));
                infoResponse.put("encodedBanner", obtenerImagenBanner(usuario.getIdUser()));
            }

            return new ResponseEntity<>(infoResponse, HttpStatus.OK);
        }

    }

    //Register - JOYA
    @RequestMapping(path = "/register/{username}/{email}/{password}/{day}/{month}/{year}/{type}", method = RequestMethod.POST)
    public ResponseEntity<Object> confirmRegisterData(@PathVariable String username,
                                                      @PathVariable String email,
                                                      @PathVariable String password,
                                                      @PathVariable String day,
                                                      @PathVariable String month,
                                                      @PathVariable String year,
                                                      @PathVariable String type) throws IOException {

        Map<String, Object> infoResponse = new HashMap<>();
        HashMap<String, String> parametros = new HashMap<>();

        parametros.put("username", username);

        if (accesoABase.obtenerUsuarios(parametros).size() != 0) {
            infoResponse.put("error", "username");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }

        parametros.clear();
        parametros.put("email", email);
        if (accesoABase.obtenerUsuarios(parametros).size() != 0) {
            infoResponse.put("error", "email");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }

        parametros.put("username", username);

        Date fechaNacimiento = new Date(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));

        accesoABase.insertarUsuario(username, email, password, fechaNacimiento, type);

        parametros.clear();
        parametros.put("email", email);

        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        infoResponse.put("user", user);
        infoResponse.put("encodedProfilePicture", obtenerImagenPerfil(user.getIdUser()));

        parametros.clear();
        parametros.put("idUser", Integer.toString(user.getIdUser()));
        if (accesoABase.obtenerArtistas(parametros).size() != 0) {
            infoResponse.put("artist", accesoABase.obtenerArtistas(parametros).get(0));
            infoResponse.put("encodedBanner", obtenerImagenBanner(user.getIdUser()));
        }

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Homepage - JOYA
    @RequestMapping(path = "/homepage/{idUser}/{index}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgHomepage_ArtistSubscriptions(@PathVariable Integer idUser,
                                                                     @PathVariable Integer index) {

        Map<String, Object> infoResponse = new HashMap<>();
        HashMap<String, String> parametros = new HashMap<>();

        parametros.put("idUser", Integer.toString(idUser));

        User actualUser = accesoABase.obtenerUsuarios(parametros).get(0);
        ArrayList<Integer> subscriptions = actualUser.getSubscriptions();
        ArrayList<User> subcriptionsData = new ArrayList<>();
        ArrayList<Image> listaImagesIndex = new ArrayList<>();
        HashMap<Integer, String> listaImagesIndexEncoded = new HashMap<>();
        HashMap<Integer, String> listaProfileImages = new HashMap<>();

        if (index == 1) {

            listaImages.clear();

            ArrayList<Image> images = accesoABase.obtenerImagenes(new HashMap<>());
            reverse(images);

            for (int i = 0; i < images.size(); i++) {
                for (Integer subscription : subscriptions) {
                    if (images.get(i).getIdUser() == subscription) {
                        parametros.clear();
                        parametros.put("idImage", Integer.toString(images.get(i).getIdImage()));
                        listaImages.add(accesoABase.obtenerImagenes(parametros).get(0));
                        break;
                    }
                }
            }
            infoResponse.put("maxIndex", Math.ceil(Float.valueOf(listaImages.size()) / Float.valueOf(cantidad)));
        }

        for (Integer subscription : subscriptions) {
            parametros.clear();
            parametros.put("idUser", (Integer.toString(subscription)));
            subcriptionsData.add(accesoABase.obtenerUsuarios(parametros).get(0));
            listaProfileImages.put(accesoABase.obtenerUsuarios(parametros).get(0).getIdUser(), obtenerImagenPerfil(accesoABase.obtenerUsuarios(parametros).get(0).getIdUser()));
        }

        for (int i = cantidad * (index - 1); i < cantidad * index; i++) {
            if (listaImages.size() > i) {
                listaImagesIndex.add(listaImages.get(i));
                listaImagesIndexEncoded.put(listaImages.get(i).getIdImage(), accesoABase.obtenerImagenEnMongoDB(listaImages.get(i).getName()));
            } else {
                break;
            }
        }

        infoResponse.put("artists", subcriptionsData);
        infoResponse.put("imagesEncoded", listaImagesIndexEncoded);
        infoResponse.put("images", listaImagesIndex);
        infoResponse.put("profileImages", listaProfileImages);
        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

    //DailyRewards - JOYA
    @RequestMapping(path = "/dailyRewards/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgHomepage_ArtistSubscriptions(@PathVariable Integer idUser) {

        Map<String, Object> infoResponse = new HashMap<>();
        HashMap<String, String> parametros = new HashMap<>();

        parametros.put("idUser", Integer.toString(idUser));

        ArrayList<Integer> dailyRewards = accesoABase.obtenerUsuarios(parametros).get(0).getDailyRewards();
        String weekDay = LocalDate.now().getDayOfWeek().toString();

        if (dailyRewards.size() != 0 && LocalDate.now().minusDays(dailyRewards.size() - 4).equals(LocalDate.of(dailyRewards.get(2), dailyRewards.get(1), dailyRewards.get(0)))) {
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        } else if (dailyRewards.size() == 0 || weekDay.equals("MONDAY") || LocalDate.now().isAfter(LocalDate.of(dailyRewards.get(2), dailyRewards.get(1), dailyRewards.get(0)).plusDays(7))) {
            LocalDate weekStartDate = LocalDate.now().minusDays(daysFromWeekStart());
            dailyRewards.clear();
            dailyRewards.add(weekStartDate.getDayOfMonth());
            dailyRewards.add(weekStartDate.getMonthValue());
            dailyRewards.add(weekStartDate.getYear());
        }
        if (weekDay == "SUNDAY" && dailyRewards.size() == 9 && !dailyRewards.contains(0)) {
            dailyRewards.add(200);
            accesoABase.modificarBpoints(idUser, 200);
        } else {
            for (int i = dailyRewards.size(); i < 3 + daysFromWeekStart(); i++) {
                dailyRewards.add(0);
            }
            dailyRewards.add(50);
            accesoABase.modificarBpoints(idUser, 50);
        }

        accesoABase.updateDailyRewards(idUser, dailyRewards);
        dailyRewards.remove(0);
        dailyRewards.remove(0);
        dailyRewards.remove(0);
        infoResponse.put("dailyReward", dailyRewards);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

    //Store - JOYA
    @RequestMapping(path = "/store/{idUser}/{index}/{tags}/{maxPrice}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgBookmarks_ImgStore(@PathVariable String idUser,
                                                           @PathVariable Integer index,
                                                           @PathVariable String tags,
                                                           @PathVariable Integer maxPrice) {

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();

        ArrayList<Image> bookmarksImages = new ArrayList<>();
        HashMap<Integer, String> encodedBookmarksImages = new HashMap<>();

        HashMap<Integer, String> encodedProfileImages = new HashMap<>();

        parametros.put("idUser", idUser);
        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        if (index == 1) {

            listaImages.clear();

            ArrayList<Integer> idImagesBookmarks = user.getBookmarks();

            for (Integer idImage : idImagesBookmarks) {
                parametros.clear();
                parametros.put("idImage", Integer.toString(idImage));

                Image image = accesoABase.obtenerImagenes(parametros).get(0);
                bookmarksImages.add(image);
                encodedBookmarksImages.put(image.getIdImage(), accesoABase.obtenerImagenEnMongoDB(image.getName()));
            }

            infoResponse.put("bookmarksImages", bookmarksImages);
            infoResponse.put("encodedBookmarksImages", encodedBookmarksImages);

        }

        parametros.clear();
        if (maxPrice != -1) {
            parametros.put("maxPrice", Integer.toString(maxPrice));
        }
        if (!tags.equals("null")) {
            parametros.put("tags", tags);
        }
        parametros.put("salable", "true");

        ArrayList<Image> bodyImages = accesoABase.obtenerImagenes(parametros);

        ArrayList<Image> bodyImagesIndex = new ArrayList<>();
        HashMap<Integer, String> encodedBodyImages = new HashMap<>();

        int indice = cantidad * (index - 1);

        while (indice < cantidad * index) {
            if (bodyImages.size() > indice) {
                Image image = bodyImages.get((int) Math.round(Math.random() * (bodyImages.size() - 1)));
                for (int i = 0; i <= listaImages.size(); i++) {
                    if (i == listaImages.size()) {
                        parametros.clear();
                        parametros.put("idUser", Integer.toString(image.getIdUser()));
                        if (Math.random() * 100 <= (Math.log((accesoABase.obtenerUsuarios(parametros).get(0).getBpoints() / 50) + 2) / Math.log(1.1))) {
                            bodyImagesIndex.add(image);
                            listaImages.add(image);
                            encodedBodyImages.put(image.getIdImage(), accesoABase.obtenerImagenEnMongoDB(image.getName()));
                            indice++;
                        }
                        break;
                    }
                    if (listaImages.get(i).getIdImage() == image.getIdImage()) {
                        break;
                    }
                }
            } else {
                break;
            }
        }

        ArrayList<User> artists = asociarArtistas(bodyImagesIndex);

        for (Image image : bookmarksImages) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(image.getIdUser()));
            User artist = accesoABase.obtenerUsuarios(parametros).get(0);

            for (int i = 0; i <= artists.size(); i++) {
                if (i == artists.size()) {
                    artists.add(artist);
                    break;
                } else if (artist.getIdUser() == artists.get(i).getIdUser()) {
                    break;
                }
            }
        }

        for (User artistaAux : artists) {
            encodedProfileImages.put(artistaAux.getIdUser(), obtenerImagenPerfil(artistaAux.getIdUser()));
        }

        infoResponse.put("bodyImagesIndex", bodyImagesIndex);
        infoResponse.put("encodedBodyImages", encodedBodyImages);
        infoResponse.put("artists", artists);
        infoResponse.put("encodedProfileImages", encodedProfileImages);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Browse - JOYA
    @RequestMapping(path = "/browse/{idUser}/{index}/{reset}/{tags}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgBrowse(@PathVariable String idUser,
                                               @PathVariable Integer index,
                                               @PathVariable String tags,
                                               @PathVariable Boolean reset) {

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();

        ArrayList<Image> bodyImagesIndex = new ArrayList<>();
        HashMap<Integer, String> encodedBodyImages = new HashMap<>();
        HashMap<Integer, String> encodedProfileImages = new HashMap<>();

        if (reset) {

            parametros.clear();
            if (!tags.equals("null")) {
                parametros.put("tags", tags);
            }
            parametros.put("salable", "false");

            listaImages.clear();

            ArrayList<Image> databaseImages = accesoABase.obtenerImagenes(parametros);

            while (databaseImages.size() > 0) {
                int indexDatabaseImage = (int) Math.round(Math.random() * (databaseImages.size() - 1));

                for (int i = 0; i <= listaImages.size(); i++) {

                    if (i == listaImages.size()) {
                        parametros.clear();
                        parametros.put("idUser", Integer.toString(databaseImages.get(indexDatabaseImage).getIdUser()));

                        if (Math.random() * 100 <= (Math.log((accesoABase.obtenerUsuarios(parametros).get(0).getBpoints() / 50) + 2) / Math.log(1.1))) {
                            listaImages.add(databaseImages.get(indexDatabaseImage));
                            databaseImages.remove(indexDatabaseImage);
                        }

                        break;
                    }

                    if (listaImages.get(i).getIdImage() == databaseImages.get(indexDatabaseImage).getIdImage()) {
                        break;
                    }

                }
            }

            infoResponse.put("maxIndex", Math.ceil(Float.valueOf(listaImages.size()) / Float.valueOf(cantidad)));
        }

        for (int i = cantidad * (index - 1); i < cantidad * index; i++) {
            if (listaImages.size() > i) {
                bodyImagesIndex.add(listaImages.get(i));
                encodedBodyImages.put(listaImages.get(i).getIdImage(), accesoABase.obtenerImagenEnMongoDB(listaImages.get(i).getName()));
            } else {
                break;
            }
        }

        ArrayList<User> artists = asociarArtistas(bodyImagesIndex);

        for (User artistaAux : artists) {
            encodedProfileImages.put(artistaAux.getIdUser(), obtenerImagenPerfil(artistaAux.getIdUser()));
        }

        infoResponse.put("images", bodyImagesIndex);
        infoResponse.put("imagesEncoded", encodedBodyImages);
        infoResponse.put("artists", artists);
        infoResponse.put("profileImages", encodedProfileImages);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Individual Image - JOYA
    @RequestMapping(path = "/individualImage/{idUser}/{title}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImageInformation(@PathVariable String idUser,
                                                      @PathVariable String title) {

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
        parametros.put("title", title);

        Image imagen = accesoABase.obtenerImagenes(parametros).get(0);
        String encodedImage = accesoABase.obtenerImagenEnMongoDB(imagen.getName());

        parametros.clear();
        parametros.put("idUser", Integer.toString(imagen.getIdUser()));

        User artista = accesoABase.obtenerUsuarios(parametros).get(0);

        parametros.clear();
        parametros.put("idUser", idUser);

        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        infoResponse.put("bookmark", false);
        if (user.getBookmarks().contains(imagen.getIdImage())) {
            infoResponse.put("bookmark", true);
        }

        infoResponse.put("purchased", false);
        for (Integer compra : user.getPurchased()) {
            if (compra == imagen.getIdImage()) {
                infoResponse.put("purchased", true);
                break;
            }
        }
        
        if (artista.getIdUser() == Integer.parseInt(idUser)) {
            infoResponse.put("purchased", true);
        }

        infoResponse.put("subscribed", false);
        if (user.getSubscriptions().contains(imagen.getIdUser())) {
            infoResponse.put("subscribed", true);
        }

        infoResponse.put("artist", artista);
        infoResponse.put("image", imagen);
        infoResponse.put("encodedImage", encodedImage);
        infoResponse.put("encodedProfilePicture", obtenerImagenPerfil(artista.getIdUser()));

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }


    //Arist Profile - JOYA
    @RequestMapping(path = "/artistProfile/{idUser}/{nameArtist}/{index}", method = RequestMethod.GET)
    public ResponseEntity<Object> getArtistProfile(@PathVariable String idUser,
                                                   @PathVariable String nameArtist,
                                                   @PathVariable Integer index) {

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();

        parametros.put("idUser", idUser);
        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        parametros.clear();
        parametros.put("username", nameArtist);
        User userArtist = accesoABase.obtenerUsuarios(parametros).get(0);

        if (index == 1) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(userArtist.getIdUser()));

            listaArtists.clear();
            listaImages = accesoABase.obtenerImagenes(parametros);
            reverse(listaImages);
            parametros.clear();

            infoResponse.put("maxIndex", Math.ceil(Float.valueOf(listaImages.size()) / Float.valueOf(cantidad)));
        }

        ArrayList<User> usuarios = accesoABase.obtenerUsuarios(parametros);
        int subscribers = 0;
        for (User usuarioAux : usuarios) {
            if (usuarioAux.getSubscriptions().contains(userArtist.getIdUser())) {
                subscribers++;
            }
        }
        infoResponse.put("subscribers", subscribers);

        infoResponse.put("sponsor", 0);
        for (ArrayList<Integer> sponsor : user.getSponsors()) {
            if (sponsor.get(0) == userArtist.getIdUser()) {
                parametros.clear();
                parametros.put("idUser", Integer.toString(sponsor.get(0)));
                infoResponse.put("sponsor", sponsor.get(1));
                break;
            }
        }

        infoResponse.put("encodedProfilePicture", obtenerImagenPerfil(userArtist.getIdUser()));
        infoResponse.put("encodedBanner", obtenerImagenBanner(userArtist.getIdUser()));
        infoResponse.put("artist", userArtist);

        infoResponse.put("subscribed", false);
        for (Integer subscrption : user.getSubscriptions()) {
            if (subscrption == userArtist.getIdUser()) {
                infoResponse.put("subscribed", true);
                break;
            }
        }

        ArrayList<Image> images = new ArrayList<>();
        HashMap<Integer, String> encondedImages = new HashMap<>();

        for (int i = cantidad * (index - 1); i < cantidad * index; i++) {
            if (i < listaImages.size()) {
                images.add(listaImages.get(i));
                encondedImages.put(listaImages.get(i).getIdImage(), accesoABase.obtenerImagenEnMongoDB(listaImages.get(i).getName()));
            }
        }

        infoResponse.put("images", images);
        infoResponse.put("encodedImages", encondedImages);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Busqueda - JOYA
    @RequestMapping(path = "/search/{idUser}/{word}/{index}/{tags}/{filterImage}/{filterArtists}", method = RequestMethod.GET)
    public ResponseEntity<Object> getSearchResults(@PathVariable String idUser,
                                                   @PathVariable String word,
                                                   @PathVariable Integer index,
                                                   @PathVariable String tags,
                                                   @PathVariable Boolean filterImage,
                                                   @PathVariable Boolean filterArtists) {

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();


        if (filterImage) {

            if (index == 1) {
                parametros.clear();
                parametros.put("word", word);
                if (!tags.equals("null")) {
                    parametros.put("tags", tags);
                }

                listaImages = accesoABase.obtenerImagenes(parametros);
                reverse(listaImages);

            }

            ArrayList<Image> listaImagesIndex = new ArrayList<>();
            HashMap<Integer, String> encodedImages = new HashMap<>();

            for (int i = cantidad * (index - 1); i < cantidad * index; i++) {
                if (i < listaImages.size()) {
                    listaImagesIndex.add(listaImages.get(i));
                    encodedImages.put(listaImages.get(i).getIdImage(), accesoABase.obtenerImagenEnMongoDB(listaImages.get(i).getName()));
                }
            }

            infoResponse.put("images", listaImagesIndex);
            infoResponse.put("encodedImages", encodedImages);

            ArrayList<User> artists = asociarArtistas(listaImagesIndex);
            HashMap<Integer, String> encodedProfilePictures = new HashMap<>();


            for (User artist : artists) {
                encodedProfilePictures.put(artist.getIdUser(), obtenerImagenPerfil(artist.getIdUser()));
            }

            infoResponse.put("imageArtists", artists);
            infoResponse.put("imageEncodedProfilePictures", encodedProfilePictures);
        }

        if (filterArtists) {
            if (index == 1) {

                listaArtists = accesoABase.obtenerUsuarios(new HashMap<>());

                ArrayList<Artist> auxArtists = accesoABase.obtenerArtistas(new HashMap<>());

                for (int i = 0; i < listaArtists.size(); i++) {
                    if (!listaArtists.get(i).getUsername().contains(word)) {
                        listaArtists.remove(i);
                        i--;
                    } else {
                        for (int j = 0; j <= auxArtists.size(); j++) {
                            if (j == auxArtists.size()) {
                                listaArtists.remove(i);
                                i--;
                            } else if (listaArtists.get(i).getIdUser() == auxArtists.get(j).getIdUser()) {
                                break;
                            }
                        }
                    }
                }
            }

            ArrayList<User> listaArtistsIndex = new ArrayList<>();
            HashMap<Integer, String> encodedProfilePictures = new HashMap<>();
            HashMap<Integer, String> encodedBanners = new HashMap<>();

            for (int i = cantidad * (index - 1); i < cantidad * index; i++) {
                if (i < listaArtists.size()) {
                    listaArtistsIndex.add(listaArtists.get(i));
                    encodedProfilePictures.put(listaArtists.get(i).getIdUser(), obtenerImagenPerfil(listaArtists.get(i).getIdUser()));
                    encodedBanners.put(listaArtists.get(i).getIdUser(), obtenerImagenBanner(listaArtists.get(i).getIdUser()));
                }
            }

            infoResponse.put("artists", listaArtistsIndex);
            infoResponse.put("encodedProfilePictures", encodedProfilePictures);
            infoResponse.put("encodedBanners", encodedBanners);

        }

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Library - JOYA
    @RequestMapping(path = "/library/{idUser}/{index}", method = RequestMethod.GET)
    public ResponseEntity<Object> getLibrary(@PathVariable String idUser,
                                             @PathVariable Integer index) {

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idUser);
        User usuario = accesoABase.obtenerUsuarios(parametros).get(0);

        if (index == 1) {
            listaImages.clear();

            for (Integer id : usuario.getPurchased()) {
                parametros.clear();
                parametros.put("idImage", Integer.toString(id));
                listaImages.add(accesoABase.obtenerImagenes(parametros).get(0));
            }

            infoResponse.put("maxIndex", Math.ceil(Float.valueOf(listaImages.size()) / Float.valueOf(cantidad)));
        }

        HashMap<Integer, String> encondedImages = new HashMap<>();
        ArrayList<Image> imagesIndex = new ArrayList<>();

        for (int i = cantidad * (index - 1); i < cantidad * index; i++) {
            if (i < listaImages.size()) {
                imagesIndex.add(listaImages.get(i));
                encondedImages.put(listaImages.get(i).getIdImage(), accesoABase.obtenerImagenEnMongoDB(listaImages.get(i).getName()));
            }
        }
        reverse(imagesIndex);

        ArrayList<User> artists = asociarArtistas(imagesIndex);
        HashMap<Integer, String> encodedProfilePictures = new HashMap<>();

        for (User artist : artists) {
            encodedProfilePictures.put(artist.getIdUser(), obtenerImagenPerfil(artist.getIdUser()));
        }

        infoResponse.put("images", imagesIndex);
        infoResponse.put("encodedImages", encondedImages);
        infoResponse.put("artists", artists);
        infoResponse.put("encodedProfilePicture", encodedProfilePictures);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Settings POST - JOYA
    @RequestMapping(path = "/settings/{idUser}/{field}", method = RequestMethod.POST)
    public ResponseEntity<Object> modifySettings(@PathVariable String idUser,
                                                 @PathVariable String field,
                                                 @RequestBody String change) throws IOException {

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();

        DBObject DBchange = (DBObject) JSON.parse(change);

        if (field.equals("username") || field.equals("email") || field.equals("password")) {
            if (!field.equals("password")) {

                parametros.put(field, (String) DBchange.get(field));

                if (accesoABase.obtenerUsuarios(parametros).size() != 0) {
                    return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
                }

                if (field.equals("username")) {
                    accesoABase.modificarBpoints(Integer.parseInt(idUser), -200);
                }
            }
            accesoABase.cambiarUsername_Email_Password(idUser, field, (String) DBchange.get(field));
        } else {

            String nombre = "";

            if (field.equals("banner")) {
                nombre = "imageBanner" + idUser;
            } else {
                nombre = "imageProfile" + idUser;
            }

            accesoABase.borrarImagenMongo(nombre);
            accesoABase.guardarImagenMongo((String) DBchange.get(field), nombre);
            accesoABase.modificarBpoints(Integer.parseInt(idUser), -500);
        }

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Settings GET - JOYA
    @RequestMapping(path = "/settings/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> showInfoSettings(@PathVariable String idUser) {


        HashMap<String, String> parametros = new HashMap<>();
        HashMap<String, Object> infoResponse = new HashMap<>();

        parametros.put("idUser", idUser);

        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        ArrayList<User> artists = new ArrayList<>();
        ArrayList<Image> images = new ArrayList<>();
        ArrayList<User> sponsorsArtists = new ArrayList<>();
        ArrayList<Float> sponsorsPercentage = new ArrayList<>();

        for (Integer idSub : user.getSubscriptions()) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(idSub));
            artists.add(accesoABase.obtenerUsuarios(parametros).get(0));
        }

        for (Integer idImage : user.getPurchased()) {
            parametros.clear();
            parametros.put("idImage", Integer.toString(idImage));
            images.add(accesoABase.obtenerImagenes(parametros).get(0));
        }

        for (ArrayList<Integer> sponsor : user.getSponsors()) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(sponsor.get(0)));
            sponsorsArtists.add(accesoABase.obtenerUsuarios(parametros).get(0));
            sponsorsPercentage.add(Float.valueOf(sponsor.get(1)) / 100);
        }

        infoResponse.put("subscriptions", artists);
        infoResponse.put("purchasedImages", images);
        infoResponse.put("sponsorsArtists", sponsorsArtists);
        infoResponse.put("sponsorsPercentage", sponsorsPercentage);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Upload Image - JOYA
    @RequestMapping(path = "/uploadImage/{idUser}/{title}/{tags}/{price}", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadImage(@PathVariable Integer idUser,
                                              @PathVariable String title,
                                              @PathVariable String tags,
                                              @PathVariable Integer price,
                                              @RequestBody HashMap requestBody) throws IOException {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("title", title);

        if (accesoABase.obtenerImagenes(parametros).size() != 0) {
            infoResponse.put("error", "title");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }

        accesoABase.modificarBpoints(idUser, -500);

        accesoABase.guardarImagen((String) requestBody.get("encodedImage"), title, idUser, price, (String) requestBody.get("description"), desconcatenarFiltros(tags));
        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

    //Buy/Bookmarks - JOYA
    @RequestMapping(path = "/action/{idUser}/{idImage}/{action}", method = RequestMethod.POST)
    public ResponseEntity<Object> addBookmark_BuyImage(@PathVariable String idUser,
                                                       @PathVariable String idImage,
                                                       @PathVariable String action) {

        Map<String, Object> infoResponse = new HashMap<>();

        accesoABase.addImageToUser(idUser, idImage, action);

        if (action.equals("buy")) {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("idImage", idImage);

            Integer idArtist = accesoABase.obtenerImagenes(parametros).get(0).getIdUser();

            parametros.clear();
            parametros.put("idUser", idUser);

            if (accesoABase.obtenerUsuarios(parametros).get(0).getSubscriptions().contains(idArtist)) {
                parametros.clear();
                parametros.put("idImage", idImage);
                accesoABase.modificarBpoints(Integer.parseInt(idUser), (int) -(accesoABase.obtenerImagenes(parametros).get(0).getPrice() * 0.8));
                accesoABase.modificarBpoints(idArtist, (int) (accesoABase.obtenerImagenes(parametros).get(0).getPrice() * 0.8));

            } else {
                parametros.clear();
                parametros.put("idImage", idImage);
                accesoABase.modificarBpoints(Integer.parseInt(idUser), -accesoABase.obtenerImagenes(parametros).get(0).getPrice());
                accesoABase.modificarBpoints(idArtist, accesoABase.obtenerImagenes(parametros).get(0).getPrice());
            }
        }

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Add sub - JOYA
    @RequestMapping(path = "/subscribe/{idUser}/{idArtist}", method = RequestMethod.POST)
    public ResponseEntity<Object> addSubscription(@PathVariable Integer idUser,
                                                  @PathVariable Integer idArtist) {

        Map<String, Object> infoResponse = new HashMap<>();

        accesoABase.addSub(idUser, idArtist);
        accesoABase.modificarBpoints(idUser, -200);
        accesoABase.modificarBpoints(idArtist, 200);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Stop sub - JOYA
    @RequestMapping(path = "/stopSubscription/{idUser}/{idArtist}", method = RequestMethod.POST)
    public ResponseEntity<Object> stopSubscription(@PathVariable String idUser,
                                                   @PathVariable String idArtist) {

        Map<String, Object> infoResponse = new HashMap<>();

        accesoABase.stopSub(idUser, Integer.parseInt(idArtist));

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Add sponsor - JOYA
    @RequestMapping(path = "/sponsor/{idUser}/{idArtist}/{bpoints}", method = RequestMethod.POST)
    public ResponseEntity<Object> addSponsor(@PathVariable String idUser,
                                             @PathVariable String idArtist,
                                             @PathVariable Integer bpoints) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idArtist);

        User artist = accesoABase.obtenerUsuarios(parametros).get(0);

        Integer percentage = (bpoints * 10000) / (artist.getBpoints() + bpoints);

        accesoABase.modificarBpoints(Integer.parseInt(idUser), -bpoints);
        accesoABase.modificarBpoints(Integer.parseInt(idArtist), bpoints);

        accesoABase.addSponsor(idUser, idArtist, percentage);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Stop sponsor - JOYA
    @RequestMapping(path = "/stopSponsor/{idUser}/{idArtist}", method = RequestMethod.POST)
    public ResponseEntity<Object> stopSponsor(@PathVariable String idUser,
                                              @PathVariable String idArtist) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idUser);

        Integer percentage = 0;
        ArrayList<ArrayList<Integer>> sponsors = accesoABase.obtenerUsuarios(parametros).get(0).getSponsors();

        for (ArrayList<Integer> sponsor : sponsors) {

            if (sponsor.get(0) == Integer.parseInt(idArtist)) {
                parametros.clear();
                parametros.put("idUser", idArtist);
                percentage = sponsor.get(1);
                infoResponse.put("profit", percentage);

                //Linea legendaria

                //profit    = Integer.parseInt(Double.toString(Double.parseDouble(Integer.toString(accesoABase.obtenerUsuarios(parametros).get(0).getBpoints())) * (Double) sponsor.get(1) / 100));

                //No tocar

                break;
            }
        }

        Integer profit = percentage * accesoABase.obtenerUsuarios(parametros).get(0).getBpoints() / 10000;

        accesoABase.stopSponsor(idUser, Integer.parseInt(idArtist));

        accesoABase.modificarBpoints(Integer.parseInt(idUser), profit);
        accesoABase.modificarBpoints(Integer.parseInt(idArtist), -profit);


        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Obtener Bpoints - JOYA
    @RequestMapping(path = "/bpoints/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> getBpoints(@PathVariable String idUser) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idUser);

        infoResponse.put("bpoints", accesoABase.obtenerUsuarios(parametros).get(0).getBpoints());

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

}