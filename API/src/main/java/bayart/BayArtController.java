package bayart;

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

        static final int cantidad = 100;

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
    @RequestMapping(path = "/register", method = RequestMethod.PUT)
    public ResponseEntity<Object> confirmRegisterData(@RequestBody HashMap info) throws IOException {

        Map<String, Object> infoResponse = new HashMap<>();
        HashMap<String, String> parametros = new HashMap<>();

        parametros.put("username", (String) info.get("username"));

        if (accesoABase.obtenerUsuarios(parametros).size() != 0) {
            infoResponse.put("error", "username");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }

        parametros.clear();
        parametros.put("email", (String) info.get("email"));
        if (accesoABase.obtenerUsuarios(parametros).size() != 0) {
            infoResponse.put("error", "email");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }

        parametros.put("username", (String) info.get("username"));

        Date fechaNacimiento = new Date(Integer.parseInt((String) info.get("year")), Integer.parseInt((String) info.get("month")), Integer.parseInt((String) info.get("day")));

        accesoABase.insertarUsuario((String) info.get("username"), (String) info.get("email"), (String) info.get("password"), fechaNacimiento, (String) info.get("userType"));

        parametros.clear();
        parametros.put("email", (String) info.get("email"));

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
    @RequestMapping(path = "/store/{idUser}/{index}/{reset}", method = RequestMethod.POST)
    public ResponseEntity<Object> getImgBookmarks_ImgStore(@PathVariable String idUser,
                                                           @PathVariable Integer index,
                                                           @PathVariable Boolean reset,
                                                           @RequestBody  HashMap requestBody) {

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();

        ArrayList<Image> bodyImagesIndex = new ArrayList<>();
        HashMap<Integer, String> encodedBodyImages = new HashMap<>();

        ArrayList<Image> bookmarksImages = new ArrayList<>();
        HashMap<Integer, String> encodedBookmarksImages = new HashMap<>();
        HashMap<Integer, String> encodedProfileImages = new HashMap<>();

        parametros.put("idUser", idUser);
        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        if (index == 1) {

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

        if (reset) {

            parametros.clear();

            if (Integer.parseInt((String)requestBody.get("maxPrice")) < 10000) {
                parametros.put("maxPrice", (String) requestBody.get("maxPrice"));
            }

            if (!requestBody.get("tags").equals("null")) {
                parametros.put("tags", (String) requestBody.get("tags"));
            }
            parametros.put("salable", "true");

            listaImages.clear();

            ArrayList<Image> databaseImages = accesoABase.obtenerImagenes(parametros);

            for(Integer idPurchased : user.getPurchased()){
                for (int i = 0; i < databaseImages.size(); i++) {
                    if (databaseImages.get(i).getIdImage() == idPurchased){
                        databaseImages.remove(i);
                        i--;
                    }
                }
            }
            parametros.clear();
            parametros.put("idUser", idUser);

            if(accesoABase. obtenerArtistas(parametros).size() > 0){
                for(Integer idImage : accesoABase. obtenerArtistas(parametros).get(0).getImageList()){
                    for (int i = 0; i < databaseImages.size(); i++) {
                        if (databaseImages.get(i).getIdImage() == idImage){
                            databaseImages.remove(i);
                            i--;
                        }
                    }
                }
            }

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

    //Browse - JOYA
    @RequestMapping(path = "/browse/{idUser}/{index}/{reset}", method = RequestMethod.POST)
    public ResponseEntity<Object> getImgBrowse(@PathVariable String idUser,
                                               @PathVariable Integer index,
                                               @PathVariable Boolean reset,
                                               @RequestBody  HashMap tags) {

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();

        ArrayList<Image> bodyImagesIndex = new ArrayList<>();
        HashMap<Integer, String> encodedBodyImages = new HashMap<>();
        HashMap<Integer, String> encodedProfileImages = new HashMap<>();

        if (reset) {

            parametros.clear();
            if (!tags.get("tags").equals("null")) {
                parametros.put("tags", (String) tags.get("tags"));
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

        parametros.clear();
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
    @RequestMapping(path = "/search/{idUser}/{word}/{index}/{reset}", method = RequestMethod.POST)
    public ResponseEntity<Object> getSearchResults(@PathVariable String  idUser,
                                                   @PathVariable String  word,
                                                   @PathVariable Integer index,
                                                   @PathVariable Boolean reset,
                                                   @RequestBody  HashMap tags){

        Map<String, Object> infoResponse = new HashMap<>();
        Map<String, String> parametros = new HashMap<>();

        word = word.toLowerCase();

        if (reset) {
            parametros.clear();
            parametros.put("word", word);

            if (!tags.get("tags").equals("null")) {
                parametros.put("tags", (String) tags.get("tags"));
            }

            listaImages = accesoABase.obtenerImagenes(parametros);
            reverse(listaImages);

            infoResponse.put("maxIndex", Math.ceil(Float.valueOf(listaImages.size()) / Float.valueOf(cantidad)));
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
        infoResponse.put("imagesEncoded", encodedImages);

        ArrayList<User> artists = asociarArtistas(listaImagesIndex);
        HashMap<Integer, String> encodedProfilePictures = new HashMap<>();

        for (User artist : artists) {
            encodedProfilePictures.put(artist.getIdUser(), obtenerImagenPerfil(artist.getIdUser()));
        }

        infoResponse.put("imageArtists", artists);

        listaArtists = accesoABase.obtenerUsuarios(new HashMap<>());

        for(int i = 0; i < listaArtists.size(); i++){
            parametros.clear();
            parametros.put("idUser",Integer.toString(listaArtists.get(i).getIdUser()));
            if (accesoABase.obtenerArtistas(parametros).size() == 0){
                listaArtists.remove(i);
                i--;
            }
        }

        ArrayList<User> auxArtists = new ArrayList<>();

        for (int i = 0; i < listaArtists.size(); i++) {
            if (listaArtists.get(i).getUsername().toLowerCase().contains(word)) {
                auxArtists.add(listaArtists.get(i));
                encodedProfilePictures.put(listaArtists.get(i).getIdUser(), obtenerImagenPerfil(listaArtists.get(i).getIdUser()));
            }
        }

        infoResponse.put("artistsCarrousel", auxArtists);
        infoResponse.put("encodedProfilePictures", encodedProfilePictures);

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

            reverse(listaImages);

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

    //Download - JOYA
    @RequestMapping(path = "/download/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> download(@PathVariable String idUser) {

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idUser);
        User usuario = accesoABase.obtenerUsuarios(parametros).get(0);

        listaImages.clear();

        for (Integer id : usuario.getPurchased()) {
            parametros.clear();
            parametros.put("idImage", Integer.toString(id));
            listaImages.add(accesoABase.obtenerImagenes(parametros).get(0));
        }

        reverse(listaImages);

        HashMap<Integer, String> encondedImages = new HashMap<>();

        for (int i = 0; i < listaImages.size(); i++) {
            encondedImages.put(listaImages.get(i).getIdImage(), accesoABase.obtenerImagenEnMongoDB(listaImages.get(i).getName()));
        }

        ArrayList<User> artists = asociarArtistas(listaImages);
        HashMap<Integer, String> encodedProfilePictures = new HashMap<>();

        for (User artist : artists) {
            encodedProfilePictures.put(artist.getIdUser(), obtenerImagenPerfil(artist.getIdUser()));
        }

        infoResponse.put("images", listaImages);
        infoResponse.put("encodedImages", encondedImages);
        infoResponse.put("artists", artists);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Settings POST - JOYA
    @RequestMapping(path = "/settings/{idUser}/{field}", method = RequestMethod.POST)
    public ResponseEntity<Object> modifySettings(@PathVariable String idUser,
                                                 @PathVariable String field,
                                                 @RequestBody HashMap change) throws IOException {

        Map<String, String> parametros = new HashMap<>();

        if (field.equals("username") || field.equals("email") || field.equals("password")) {
            if (!field.equals("password")) {

                parametros.put(field, (String) change.get(field));

                if (accesoABase.obtenerUsuarios(parametros).size() != 0) {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }

                if (field.equals("username")) {
                    accesoABase.modificarBpoints(Integer.parseInt(idUser), -200);
                }
            }
            accesoABase.cambiarUsername_Email_Password(idUser, field, (String) change.get(field));
        }
        else if(field.equals("banner") || field.equals("imageProfile")){

            String nombre = "";

            if (field.equals("banner")) {
                nombre = "imageBanner" + idUser;
            } else {
                nombre = "imageProfile" + idUser;
            }

            accesoABase.borrarImagenMongo(nombre);
            accesoABase.guardarImagenMongo((String) change.get(field), nombre);
            accesoABase.modificarBpoints(Integer.parseInt(idUser), -200);
        }
        else{
            accesoABase.cambiarNotificaciones(idUser, field, Boolean.toString((Boolean) change.get("change")));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Settings GET - JOYA
    @RequestMapping(path = "/settings/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> showInfoSettings(@PathVariable String idUser) {


        HashMap<String, String> parametros = new HashMap<>();
        HashMap<String, Object> infoResponse = new HashMap<>();

        parametros.put("idUser", idUser);

        User user = accesoABase.obtenerUsuarios(parametros).get(0);

        ArrayList<String> artists = new ArrayList<>();
        ArrayList<String> sponsorsArtists = new ArrayList<>();
        ArrayList<Float> sponsorsPercentage = new ArrayList<>();
        ArrayList<String> subscriptors = new ArrayList<>();
        ArrayList<String> sponsors = new ArrayList<>();

        for (Integer idSub : user.getSubscriptions()) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(idSub));
            artists.add(accesoABase.obtenerUsuarios(parametros).get(0).getUsername());
        }

        for (ArrayList<Integer> sponsor : user.getSponsors()) {
            parametros.clear();
            parametros.put("idUser", Integer.toString(sponsor.get(0)));
            sponsorsArtists.add(accesoABase.obtenerUsuarios(parametros).get(0).getUsername());
            sponsorsPercentage.add(Float.valueOf(sponsor.get(1)) / 100);
        }

        parametros.clear();

        for (User userAux : accesoABase.obtenerUsuarios(parametros)){
            for (ArrayList<Integer> sponsor : userAux.getSponsors()){
                for (Integer idArtistSponsored : sponsor){
                    if(idArtistSponsored == Integer.parseInt(idUser)){
                        sponsors.add(userAux.getUsername());
                        break;
                    }
                }
            }
        }

        for (User userAux : accesoABase.obtenerUsuarios(parametros)){
            for (Integer subscription : userAux.getSubscriptions()) {
                if (subscription == Integer.parseInt(idUser)) {
                    subscriptors.add(userAux.getUsername());
                    break;
                }
            }
        }

        infoResponse.put("subscriptions", artists);
        infoResponse.put("sponsoredArtists", sponsorsArtists);
        infoResponse.put("sponsorsPercentage", sponsorsPercentage);
        infoResponse.put("subscriptors", subscriptors);
        infoResponse.put("sponsors", sponsors);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Upload Image - JOYA
    @RequestMapping(path = "/uploadImage/{idUser}", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadImage(@PathVariable Integer idUser,
                                              @RequestBody  HashMap requestBody) throws IOException {

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("title", (String) requestBody.get("title"));

        if (accesoABase.obtenerImagenes(parametros).size() != 0) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        accesoABase.modificarBpoints(idUser, -500);

        accesoABase.guardarImagen((String) requestBody.get("encodedImage"), (String) requestBody.get("title"), idUser, Integer.parseInt((String) requestBody.get("price")), (String) requestBody.get("description"), desconcatenarFiltros((String) requestBody.get("tags")));
        return new ResponseEntity<>(HttpStatus.OK);

    }

    //Buy/Bookmarks - JOYA
    @RequestMapping(path = "/action/{idUser}/{idImage}", method = RequestMethod.POST)
    public ResponseEntity<Object> addBookmark_BuyImage(@PathVariable String idUser,
                                                       @PathVariable String idImage,
                                                       @RequestBody  HashMap action) {

        accesoABase.addImageToUser(idUser, idImage, (String) action.get("action"));

        if (action.get("action").equals("buy")) {
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

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Add sub - JOYA
    @RequestMapping(path = "/subscribe/{idUser}/{idArtist}", method = RequestMethod.POST)
    public ResponseEntity<Object> addSubscription(@PathVariable Integer idUser,
                                                  @PathVariable Integer idArtist) {

        accesoABase.addSub(idUser, idArtist);
        accesoABase.modificarBpoints(idUser, -200);
        accesoABase.modificarBpoints(idArtist, 200);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Stop sub - JOYA
    @RequestMapping(path = "/stopSubscription/{idUser}/{idArtist}", method = RequestMethod.POST)
    public ResponseEntity<Object> stopSubscription(@PathVariable String idUser,
                                                   @PathVariable String idArtist) {

        accesoABase.stopSub(idUser, Integer.parseInt(idArtist));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Add sponsor - JOYA
    @RequestMapping(path = "/sponsor/{idUser}/{idArtist}", method = RequestMethod.POST)
    public ResponseEntity<Object> addSponsor(@PathVariable String idUser,
                                             @PathVariable String idArtist,
                                             @RequestBody  HashMap bpoints) {

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idArtist);

        User artist = accesoABase.obtenerUsuarios(parametros).get(0);

        Integer percentage = Integer.parseInt(Long.toString(Long.parseLong((String) bpoints.get("bpoints")) * 10000 / (Long.parseLong(Integer.toString(artist.getBpoints())) + Long.parseLong((String)bpoints.get("bpoints")))));

        accesoABase.modificarBpoints(Integer.parseInt(idUser), -Integer.parseInt((String)bpoints.get("bpoints")));
        accesoABase.modificarBpoints(Integer.parseInt(idArtist), Integer.parseInt((String)bpoints.get("bpoints")));

        accesoABase.addSponsor(idUser, idArtist, percentage);

        return new ResponseEntity<>(HttpStatus.OK);
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

    //Obtener Info Usuario - JOYA
    @RequestMapping(path = "/infoUsuario/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> getInfoUsuario(@PathVariable String idUser) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idUser);

        infoResponse.put("infoUsuario", accesoABase.obtenerUsuarios(parametros).get(0));

        User usuario = accesoABase.obtenerUsuarios(parametros).get(0);
        infoResponse.put("user", usuario);
        infoResponse.put("encodedProfilePicture", obtenerImagenPerfil(usuario.getIdUser()));

        parametros.clear();
        parametros.put("idUser", idUser);
        if (accesoABase.obtenerArtistas(parametros).size() != 0) {
            infoResponse.put("artist", accesoABase.obtenerArtistas(parametros).get(0));
            infoResponse.put("encodedBanner", obtenerImagenBanner(usuario.getIdUser()));
        }

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

}