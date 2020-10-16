package bayart;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.*;


@RequestMapping("/api")
@RestController

public class Controller {

    //Acceso a mongo
    AccesoMongoDB accesoABase=new AccesoMongoDB();


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

    //Homepage
    @RequestMapping(path = "/homepage/{idUser} viewAs = {userType}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgSubscriptions(@PathVariable Integer idUser,
                                                      @PathVariable User userType){

        Map<Integer,Image>   subscriptionsImg = new HashMap<>();
        Map<String, Object> infoResponse     = new HashMap<>();
        Map<String,String>   requisitos       = new HashMap<>();

        requisitos.put("idUser",Integer.toString(idUser));

        User actualUser = accesoABase.obtenerUsuarios(requisitos).get(0);
        List<Map<Integer,Date>>subscriptions = (List<Map<Integer, Date>>) actualUser.getSubscriptions();

        if(subscriptions.isEmpty()){
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }
        else{
            for(Map<Integer,Date>map : subscriptions){  //recorre cada dato de la subscripción
                for(Map.Entry<Integer, Date> entry : map.entrySet()){

                    HashMap<String,String> datos = new HashMap<>(); //guarda los datos del artista para pasarlos por parámetro
                    datos.put("idUser",Integer.toString(entry.getKey()));

                    Artist artista = (Artist) accesoABase.obtenerArtistas(datos).get(0);
                    HashMap<Image,String>imagesArtist = artista.getImageList(); //obtiene los datos de cada imagen del artista en cuestión

                    for(Map.Entry<Image,String>dato : imagesArtist.entrySet()){ //recorre para sacar únicamente la imagen

                        //inserta imagenes de artista en el map
                        subscriptionsImg.put(artista.getIdUser(),dato.getKey()); //la agrega a la variable que va a retornar

                    }
                }
            }
        }

        infoResponse.put("artists", subscriptions);
        infoResponse.put("images", subscriptionsImg);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }

    //Store

    //los artistas con mas puntos
    //Los nuevos artistas
    //Mas comentadas
    @RequestMapping(path = "/store/{idUser} viewAs = {userType} index = {index}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgBookmarksStore(@PathVariable Integer idUser,
                                                       @PathVariable User userType,
                                                       @PathVariable User userType){

        Map<Object, Object> infoResponse = new HashMap<>();



    }

    //Browse


    //Search


    //Individual Image


    //Arist Profile


    //Library


    //Settings


    //Upload Image

}