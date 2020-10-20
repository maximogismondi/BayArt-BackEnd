package bayart;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.*;


@RequestMapping("/api")
@RestController

public class BayArtController {


    //Acceso a mongo
    AccesoMongoDB accesoABase = new AccesoMongoDB();

    //lista compartida de imagenes
    Map<Artist, Image> listaImages = new HashMap<>();

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
    @RequestMapping(path = "/register/{username} {eMail} {confirmEMail} {password} {confirmPassword} {day} {month} {year} {type}", method = RequestMethod.POST)
    public ResponseEntity<Object> confirmRegisterData(@PathVariable String username,
                                                      @PathVariable String eMail,
                                                      @PathVariable String confirmEMail,
                                                      @PathVariable String password,
                                                      @PathVariable String confirmPassword,
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

    Map<Artist, Image> mergeSort(Map<Artist, Image> array){
        Map<Artist, Image> splitArrayL = new HashMap<>();
        Map<Artist, Image> splitArrayR = new HashMap<>();
        if(array.size() == 1){
            return array;
        } else {
            int i = 0, m = array.size()/2;
            for(Map.Entry<Artist, Image> artistImage : array.entrySet()){
                if(i < m){
                    splitArrayL.put(artistImage.getKey(),artistImage.getValue());
                } else {
                    splitArrayR.put(artistImage.getKey(),artistImage.getValue());
                }
                i++;
            }
            splitArrayL = mergeSort(splitArrayL);
            splitArrayR = mergeSort(splitArrayR);
        }

        int indexL = 0;
        int indexR = 0;

        array.clear();

        while(indexL+indexR < splitArrayL.size()+splitArrayR.size()-1){
            int i = 0;
            if(indexL == splitArrayL.size()){
                for (Map.Entry<Artist, Image> artistImage : splitArrayR.entrySet()){
                    if (i >= indexR) {
                        array.put(artistImage.getKey(),artistImage.getValue());
                    }
                    i++;
                }
                indexR++;
            } else if (indexR == splitArrayR.size()){
                for (Map.Entry<Artist, Image> artistImage : splitArrayL.entrySet()){
                    if (i >= indexL) {
                        array.put(artistImage.getKey(),artistImage.getValue());
                    }
                    i++;
                }
                indexL++;
            } else if (splitArrayL.get(indexL).getPostDate().compareTo(splitArrayR.get(indexR).getPostDate()) >= 0) {
                for (Map.Entry<Artist, Image> artistImage : splitArrayL.entrySet()){
                    if (i == indexL) {
                        array.put(artistImage.getKey(),artistImage.getValue());
                        break;
                    }
                    i++;
                }
                indexL++;
            } else {
                for (Map.Entry<Artist, Image> artistImage : splitArrayR.entrySet()){
                    if (i == indexR) {
                        array.put(artistImage.getKey(),artistImage.getValue());
                        break;
                    }
                    i++;
                }
                indexR++;
            }
        }
        return array;
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
                            listaImages.put(artista, dato); //la agrega a la variable que va a retornar

                        }
                    }
                }
            }
            listaImages = mergeSort(listaImages);
            infoResponse.put("artists", subscriptions);
        }

        Map<Artist, Image> listaImagesIndex = new HashMap<>();
        int start = 30 * (index - 1), lim = 30 * index, i = 0;

        for(Map.Entry<Artist, Image> artistImage : listaImages.entrySet()){
            if(i >= start && i < lim) listaImagesIndex.put(artistImage.getKey(),artistImage.getValue());
            i++;
        }
        infoResponse.put("images", listaImagesIndex);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

    private ArrayList<String> desconcatenarFiltros(String filters){

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

    //Store

    private Image imagenesDisponible(Artist artista,  ArrayList<String> listaFiltros, Integer maxPrice){

        for(Image imagenAux : artista.getImageList()){
            if (!listaImages.containsValue(imagenAux) && ((maxPrice == 0 && imagenAux.getPrice() == 0) ||
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

        Map<String, String> parametros = new HashMap<>();

        ArrayList<String> listaFiltros = desconcatenarFiltros(filters);

        //conseguir los bookmarks del usuario
        if(index == 1){
            Map <Artist, Image> listaImagesBookmaks = new HashMap<>();

            parametros.put("idUser",idUser);
            ArrayList<Integer> idsImagenesBookmark = accesoABase.obtenerUsuarios(parametros).get(0).getBookmarks();

            for(Integer idImage : idsImagenesBookmark) {
                parametros.clear();
                parametros.put("idImage", Integer.toString(idImage));
                Image imagenBookmark = (Image) accesoABase.obtenerImagen(parametros).get(0);
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

                    Image imagenAMostrar = imagenesDisponible(artista, listaFiltros, maxPrice);
                    if(imagenAMostrar.getIdImage() != null){
                        listaImages.put(artista,imagenAMostrar);
                    }

                }
            }
        }

        infoResponse.put("imageListStore",listaImages);

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

                    Image imagenAMostrar = imagenesDisponible(artista, listaFiltros, 0);
                    if(imagenAMostrar.getIdImage() != null){
                        listaImages.put(artista,imagenAMostrar);
                    }

                }
            }
        }

        infoResponse.put("imageListBrowse",listaImages);

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);

    }

    //Search


    //Individual Image
    @RequestMapping(path = "/individualImage/{idImage}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImageInformation(@PathVariable String idImage){

        Map<String, Object> infoResponse = new HashMap<>();

        Map<String, String> parametros = new HashMap<>();
                            parametros.put("idImage",idImage);

        Image imagen = (Image) accesoABase.obtenerImagen(parametros).get(0);

        parametros.clear();
        parametros.put("idArtist",Integer.toString(imagen.getIdUser()));

        Artist artista = accesoABase.obtenerArtistas(parametros).get(0);

        infoResponse.put("artist", artista);
        infoResponse.put("image",imagen);

        return new ResponseEntity<>(infoResponse,HttpStatus.OK);
    }

    @RequestMapping(path = "/individualImage/{idImage} {idUser} action = {action}", method = RequestMethod.POST)
    public ResponseEntity<Object> addBookmark_BuyImage(@PathVariable String idImage,
                                                       @PathVariable String idUser,
                                                       @PathVariable String action){

        Map<String, Object> infoResponse = new HashMap<>();

        accesoABase.addImage(idUser,idImage,action);

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

    @RequestMapping(path="/seach/{word} viewAs = {userType}",method=RequestMethod.GET)
    public ResponseEntity<Object>getSearchResults(@PathVariable String word,
                                                  @PathVariable User userType){

        Map<String,Object> infoResponse = new HashMap<>();
        List<Image>        resultImages = new ArrayList<>();

        accesoABase.conectarAColeccion("Images");

        resultImages = accesoABase.obtainImages(word);

        infoResponse.put("images",resultImages);

        if(resultImages.isEmpty()){
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(infoResponse, HttpStatus.OK);
    }



    //Library
    @RequestMapping(path="/library/{word} viewAs = {userType}",method=RequestMethod.GET)

    //Settings


    //Upload Image

}