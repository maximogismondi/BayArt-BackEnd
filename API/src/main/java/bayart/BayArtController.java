package bayart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;


@RequestMapping("/api")
@RestController

public class BayArtController {

    //Acceso a mongo
    @Autowired
    private AccesoMongoDB accesoABase;

    public BayArtController(){
        this.accesoABase = new AccesoMongoDB();
    }

    //lista compartida de imagenes
    private ArrayList<Image> listaImages = new ArrayList<>();

    //Funciones extra

    private Map<Artist, Image> asociarArtista (ArrayList<Image> imagenes){

        Map<String, String> parametros = new HashMap<>();
        Map<Artist, Image> artistaImagen = new HashMap<>();

        for (Image imagen:imagenes){
            parametros.clear();
            parametros.put("idUser", Integer.toString(imagen.getIdUser()));
            artistaImagen.put(accesoABase.obtenerArtistas(parametros).get(0), imagen);
        }

        return artistaImagen;
    }
    private Map<Artist, Image> asociarImages (ArrayList<Artist> artistas){
        Map<String, String> parametros = new HashMap<>();
        Map<Artist, Image> artistaImagen = new HashMap<>();

        for (Artist artista : artistas){
            parametros.clear();
            parametros.put("idUser", Integer.toString(artista.getIdUser()));
            artistaImagen.put(artista, accesoABase.obtenerImagenes(parametros).get(0));
        }

        return artistaImagen;
    }
    private ArrayList<Image> mergeSort(ArrayList<Image> array){
        ArrayList<Image> splitArrayL = new ArrayList<>();
        ArrayList<Image> splitArrayR = new ArrayList<>();
        if(array.size() == 1){
            return array;
        } else {
            for(int i=0 ; i < array.size()/2; i++){
                splitArrayL.add(array.get(i));
            }
            for(int j=array.size()/2 ; j < array.size(); j++){
                splitArrayR.add(array.get(j));
            }
            splitArrayL = mergeSort(splitArrayL);
            splitArrayR = mergeSort(splitArrayR);
        }

        int indexL = 0;
        int indexR = 0;

        for (int i = 0; i < array.size(); ++i){
            if(indexL == splitArrayL.size()){
                array.set(i,splitArrayR.get(indexR));
                indexR++;
            } else if (indexR == splitArrayR.size()){
                array.set(i,splitArrayL.get(indexL));
                indexL++;
            } else if (splitArrayL.get(indexL).getPostDate().compareTo(splitArrayR.get(indexR).getPostDate()) >= 0) {
                array.set(i,splitArrayL.get(indexL));
                indexL++;
            } else {
                array.set(i,splitArrayR.get(indexR));
                indexR++;
            }
        }
        return array;
    }
    public  ArrayList<String> desconcatenarFiltros(String filters){

        ArrayList<String> filtros = new ArrayList<>();
        String filtro = "";

        for (int i = 0; i < filters.length(); i++) {
            if(!(filters.charAt(i) == ',')){
                filtro.concat(Character.toString(filters.charAt(i)));
            } else{
                filtros.add(filtro);
                filtro = "";
            }
        }
        return filtros;

    }

    //Login
    @RequestMapping(path = "/logIn/{username} {password}", method = RequestMethod.GET)
    public ResponseEntity<Object> confirmLogInData(@PathVariable String username,
                                                   @PathVariable String password) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String,String> parametrosUsuario = new HashMap<>();
        parametrosUsuario.put("username",username);
        parametrosUsuario.put("password",password);
        User usuario = accesoABase.obtenerUsuarios(parametrosUsuario).get(0);

        if(usuario == null){
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        } else {
            infoResponse.put("user",usuario);
            return new ResponseEntity<>(infoResponse, HttpStatus.OK);
        }

    }

    //Register
    @RequestMapping(path = "/register/{username} {eMail} {password} {day} {month} {year} {type}", method = RequestMethod.POST)
    public ResponseEntity<Object> confirmRegisterData(@PathVariable String username,
                                                      @PathVariable String eMail,
                                                      @PathVariable String password,
                                                      @PathVariable String day,
                                                      @PathVariable String month,
                                                      @PathVariable String year,
                                                      @PathVariable String type) {

        Map<String, Object> infoResponse = new HashMap<>();

        HashMap<String,String> parametrosUsuario = new HashMap<>();

        parametrosUsuario.put("username",username);
        if(accesoABase.obtenerUsuarios(parametrosUsuario) == null){
            infoResponse.put("username", "Was an error with the username");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }

        parametrosUsuario.clear();
        parametrosUsuario.put("eMail",eMail);
        if(accesoABase.obtenerUsuarios(parametrosUsuario) == null){
            infoResponse.put("username", "Was an error with the e-mail");
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }


        parametrosUsuario.put("username",username);

        Date fechaIncriptions = new Date(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));

        accesoABase.insertarUsuario(username, eMail, password, fechaIncriptions, type);

        HashMap eMailUsuario = new HashMap<>();
        eMailUsuario.put("eMail",eMail);

        User user = (User) accesoABase.obtenerUsuarios(eMailUsuario).get(0);

        infoResponse.put("user",user);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

    //Homepage
    @RequestMapping(path = "/homepage/{idUser} index = {index}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgHomepage_ArtistSubscriptions(@PathVariable Integer idUser,
                                                                     @PathVariable Integer index){

        Map<String, Object>  infoResponse     = new HashMap<>();
        Map<String,String>   requisitos       = new HashMap<>();

        if(index==1){
            requisitos.put("idUser",Integer.toString(idUser));

            User actualUser = accesoABase.obtenerUsuarios(requisitos).get(0);
            List<Map<Integer,Date>>subscriptions  = (List<Map<Integer, Date>>) actualUser.getSubscriptions();

            if(subscriptions.isEmpty()){
                return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
            }
            else{
                for(Map<Integer,Date>map : subscriptions){  //recorre cada dato de la subscripción
                    for(Map.Entry<Integer, Date> entry : map.entrySet()){

                        HashMap<String,String> datos = new HashMap<>(); //guarda los datos del artista para pasarlos por parámetro
                        datos.put("idUser",Integer.toString(entry.getKey()));

                        Artist artista = (Artist) accesoABase.obtenerArtistas(datos).get(0);
                        ArrayList<Image> imagesArtist = artista.getImageList(); //obtiene los datos de cada imagen del artista en cuestión

                        for(Image dato : imagesArtist){ //recorre para sacar únicamente la imagen

                            //inserta imagenes de artista en el map
                            listaImages.add(dato); //la agrega a la variable que va a retornar

                        }
                    }
                }
            }
            listaImages = mergeSort(listaImages);
            infoResponse.put("artists", subscriptions);
        }

        ArrayList<Image>listaImagesIndex = new ArrayList<>();
        int start = 30 * (index - 1), lim = 30 * index, i = 0;

        for(Image artistImage : listaImages){
            if(i >= start && i < lim) listaImagesIndex.add(artistImage);
            i++;
        }
        infoResponse.put("images", asociarArtista(listaImagesIndex));

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }


    //Store
    private Image imagenDisponible(Artist artista,  ArrayList<String> listaFiltros, Integer maxPrice){

        for(Image imagenAux : artista.getImageList()){
            if (!listaImages.contains(imagenAux) && ((maxPrice == 0 && imagenAux.getPrice() == 0) ||
                 imagenAux.getPrice() <= maxPrice)){

                for(String filtro : listaFiltros){
                    for (String tag : imagenAux.getTags()){
                        if(filtro.equals(tag)){
                            return imagenAux;
                        }
                    }
                }
            }
        }

        return new Image(null,null,null,null,null,null,null,null,null);

    }

    @RequestMapping(path = "/store/{idUser} index = {index} filters = {filters} maxPrice = {maxPrice}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgBookmarks_ImgStore(@PathVariable String idUser,
                                                       @PathVariable Integer index,
                                                       @PathVariable String filters,
                                                       @PathVariable Integer maxPrice){

        listaImages.clear();

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros   = new HashMap<>();

        ArrayList<String>   listaFiltros = desconcatenarFiltros(filters);

        //conseguir los bookmarks del usuario
        if(index == 1){
            Map <Artist, Image> listaImagesBookmaks = new HashMap<>();

            parametros.put("idUser",idUser);
            ArrayList<Integer> idsImagenesBookmark = accesoABase.obtenerUsuarios(parametros).get(0).getBookmarks();

            for(Integer idImage : idsImagenesBookmark) {
                parametros.clear();
                parametros.put("idImage", Integer.toString(idImage));
                Image imagenBookmark = (Image) accesoABase.obtenerImagenes(parametros).get(0);
                parametros.clear();
                parametros.put("idUser", Integer.toString(imagenBookmark.getIdUser()));
                Artist creadorImagen = (Artist) accesoABase.obtenerArtistas(parametros).get(0);

                listaImagesBookmaks.put(creadorImagen, imagenBookmark);
            }

            infoResponse.put("bookmarks",listaImagesBookmaks);
        }

        parametros.clear();

        ArrayList<Artist> listaArtistas = accesoABase.obtenerArtistas(parametros);

        while(listaImages.size() * index < 30 * index){
            boolean findArtist = false;
            while(!findArtist) {
                Artist artista = listaArtistas.get((int) (Math.random() * (listaArtistas.size()-1)));
                if (Math.random() * 100 <= Math.log(artista.getBpoints() / 100) * 5) {

                    findArtist = true;
                    boolean findImage = false;

                    Image imagenAMostrar = imagenDisponible(artista, listaFiltros, maxPrice);
                    if(imagenAMostrar.getIdImage() != null){
                        listaImages.add(imagenAMostrar);
                    }

                }
            }
        }

        infoResponse.put("imageListStore",asociarArtista(listaImages));

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);

    }

    //Browse
    @RequestMapping(path = "/browse/{idUser} index = {index} filters = {filters}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgBrowse(@PathVariable String idUser,
                                                        @PathVariable Integer index,
                                                        @PathVariable String filters){

        listaImages.clear();

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();

        ArrayList<String> listaFiltros = desconcatenarFiltros(filters);

        parametros.clear();

        ArrayList<Artist> listaArtistas = accesoABase.obtenerArtistas(parametros);

        while(listaImages.size() * index < 30 * index){
            boolean findArtist = false;
            while(!findArtist) {
                Artist artista = listaArtistas.get((int) (Math.random() * listaArtistas.size()));
                if (Math.random() * 100 <= Math.log(artista.getBpoints() / 100) * 5) {

                    findArtist = true;
                    boolean findImage = false;

                    Image imagenAMostrar = imagenDisponible(artista, listaFiltros, 0);
                    if(imagenAMostrar.getIdImage() != null){
                        listaImages.add(imagenAMostrar);
                    }

                }
            }
        }

        infoResponse.put("imageListBrowse",asociarArtista(listaImages));

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);

    }

    //Individual Image
    @RequestMapping(path = "/individualImage/{idImage}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImageInformation(@PathVariable String idImage){

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
                            parametros.put("idImage",idImage);

        Image imagen = (Image) accesoABase.obtenerImagenes(parametros).get(0);

        parametros.clear();
        parametros.put("idArtist",Integer.toString(imagen.getIdUser()));

        Artist artista = accesoABase.obtenerArtistas(parametros).get(0);

        infoResponse.put("artist", artista);
        infoResponse.put("image",imagen);

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    //Individual Image Buy/Bookmarks
    @RequestMapping(path = "/individualImage/{idImage} {idUser} action = {action}", method = RequestMethod.POST)
    public ResponseEntity<Object> addBookmark_BuyImage(@PathVariable String idImage,
                                                       @PathVariable String idUser,
                                                       @PathVariable String action){

        Map<String, Object> infoResponse = new HashMap<>();

        accesoABase.addImageToUser(idUser,idImage,action);

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    //Arist Profile
    @RequestMapping(path = "/artistProfile/{idUser}", method = RequestMethod.GET)
    public ResponseEntity<Object> getArtistProfile(@PathVariable String idUser){

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
        parametros.put("idUser",idUser);

        Artist artista = accesoABase.obtenerArtistas(parametros).get(0);

        ArrayList<Image> listaImagenes = artista.getImageList();

        infoResponse.put("artist", artista);
        infoResponse.put("image",listaImagenes);

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    //Busqueda
    @RequestMapping(path="/seach/{idUser} {word} filters = {filters} user = {user} image = {image} viewAs = {userType}",method=RequestMethod.GET)
    public ResponseEntity<Object>getSearchResults(@PathVariable String idUser,
                                                  @PathVariable String word,
                                                  @PathVariable String filters,
                                                  @PathVariable Boolean user,
                                                  @PathVariable Boolean image,
                                                  @PathVariable User userType){

        ArrayList<Image>   resultImages  = new ArrayList<>();
        ArrayList<Artist>  resultArtist  = new ArrayList<>();
        Map<String,Object> infoResponse  = new HashMap<>();
        Map<String,String> requirements  = new HashMap<>();
                           requirements.put("idUser",idUser);

        accesoABase.obtenerUsuarios(requirements).get(0).getHistory().add(word);

        if(image){
            requirements.clear();
            requirements.put("word",word);

            accesoABase.conectarAColeccion("Images");

            if(!filters.isEmpty()) {
                requirements.put("tags", filters);
            }

            resultImages = accesoABase.obtenerImagenes(requirements);

            infoResponse.put("images",asociarArtista(resultImages));
        }

        if(user){
            requirements.clear();
            requirements.put("username",word);

            accesoABase.conectarAColeccion("User");

            resultArtist = accesoABase.obtenerArtistas(requirements);

            infoResponse.put("artist",asociarImages(resultArtist));
        }

        if(resultImages.isEmpty()){
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }

    //Library
    @RequestMapping(path="/library/{idUser} viewAs = {userType} index = {index}",method=RequestMethod.GET)
    public ResponseEntity<Object>getLibrary(@PathVariable String idUser,
                                            @PathVariable String userType,
                                            @PathVariable Integer index){

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
        parametros.put("idUser",idUser);

        User usuario = accesoABase.obtenerUsuarios(parametros).get(0);

        Map<Integer,Map<Date,Boolean>> listaImagenesCompradas = usuario.getPurchased();

        parametros.clear();

        ArrayList<Image> library = new ArrayList<>();

        int i = 0, start = 30 * index-1, lim = 30 * index;

        for(Map.Entry<Integer,Map<Date,Boolean>> imagenComprada : listaImagenesCompradas.entrySet()){
            if(i >= start && i < lim){
                parametros.put("idImage",Integer.toString(imagenComprada.getKey()));
                Image imagen = accesoABase.obtenerImagenes(parametros).get(0);
                library.add(imagen);
            } i++;
        }

        infoResponse.put("library", asociarArtista(library));

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    //Settings POST
    @RequestMapping(path="/setting/{idUser} viewAs = {userType} field = {field} change = {change}",method=RequestMethod.POST)
    public ResponseEntity<Object> modifySettings(@PathVariable String idUser,
                                                 @PathVariable String userType,
                                                 @PathVariable String field,
                                                 @PathVariable String change){

        Map<String,Object> infoResponse = new HashMap<>();

        switch (field){
            case "banner":
            case "profilePicture":
                //accesoABase.changeImgParameters();
                break;
            default:
                accesoABase.changeStringParameters(idUser,field,change);
                break;
        }

        return  new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    //Settings GET
    @RequestMapping(path="/setting/{idUser} viewAs = {userType} field = {field}",method=RequestMethod.GET)
    public ResponseEntity<Object> showInfoSettings(@PathVariable String idUser,
                                                   @PathVariable String userType,
                                                   @PathVariable String field){

        Map<String,Object> infoResponse = new HashMap<>();

        HashMap<String,String> parametros = new HashMap<>();
                               parametros.put("idUser",idUser);

        User usuario = accesoABase.obtenerUsuarios(parametros).get(0);

        if(field.equals("Subscriptions")){
            ArrayList<String> resultado = new ArrayList<>();

            for (Map.Entry<Integer,Date> subscripcion : usuario.getSubscriptions().entrySet()){
                parametros.clear();
                parametros.put("idUser",Integer.toString(subscripcion.getKey()));
                resultado.add(accesoABase.obtenerArtistas(parametros).get(0).getUsername());
            }
        } else {

            parametros.clear();
            parametros.put("idUser",idUser);

            Map<Integer,Map<Date,Boolean>> purchasedImagesMap = accesoABase.obtenerUsuarios(parametros).get(0).getPurchased();

            Map<String,Integer> purchasedImages = new HashMap<>();

            for(Map.Entry<Integer,Map<Date,Boolean>> imagenAux : purchasedImagesMap.entrySet()){

                parametros.clear();
                parametros.put("idImage",Integer.toString(imagenAux.getKey()));
                purchasedImages.put(accesoABase.obtenerImagenes(parametros).get(0).getName(), accesoABase.obtenerImagenes(parametros).get(0).getPrice());
            }

            parametros.clear();
            parametros.put("idUser",idUser);

            ArrayList<Sponsor> sponsors = accesoABase.obtenerUsuarios(parametros).get(0).getSponsors();

            Map<String,Integer> sponsoredArtists = new HashMap<>();

            for(Sponsor sponsorAux : sponsors){
                parametros.clear();
                parametros.put("idUser",Integer.toString(sponsorAux.getIdArtist()));
                sponsoredArtists.put(accesoABase.obtenerArtistas(parametros).get(0).getUsername(),sponsorAux.getInvertedPoints());
            }

            parametros.clear();
            parametros.put("idUser",idUser);

            Map<Integer, Date> subscriptionsMap = accesoABase.obtenerUsuarios(parametros).get(0).getSubscriptions();

            Map<String,Integer> subscriptions = new HashMap<>();

            for(Map.Entry<Integer,Date> subAux : subscriptionsMap.entrySet()){
                parametros.clear();
                parametros.put("idUser",Integer.toString(subAux.getKey()));
                subscriptions.put(accesoABase.obtenerArtistas(parametros).get(0).getUsername(),100);
            }

            Map<String,Map<String,Integer>> resultado = new HashMap<>();
                                            resultado.put("purchasedImages",purchasedImages);
                                            resultado.put("sponsoredArtists",sponsoredArtists);
                                            resultado.put("subscriptions",subscriptions);

            infoResponse.put("resultado", resultado);
        }

        return  new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    //Upload Image
    @RequestMapping(path="/upload-image/{idUser} title = {title} URL = {URL} description = {description} tags = {tags} price = {price}",method=RequestMethod.POST)
    public ResponseEntity<Object> uploadImage(@PathVariable String  idUser,
                                              @PathVariable String  title,
                                              @PathVariable String  URL,
                                              @PathVariable String  description,
                                              @PathVariable String  tags,
                                              @PathVariable Integer price) throws IOException {
        Map<String,Object> infoResponse = new HashMap<>();

        accesoABase.guardarImagenEnMongoDB(URL,title, idUser, price, description, desconcatenarFiltros(tags));


        return  new ResponseEntity<>(infoResponse,HttpStatus.OK);

    }

}