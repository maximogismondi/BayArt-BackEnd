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
    public ResponseEntity<Object> confirmUserData(@PathVariable String username,
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
    public ResponseEntity<Object> confirmUserData(@PathVariable String username,
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

        Date fechaIncriptions = new Date(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day))

        User usur = new User(null, username, eMail, password, fechaIncriptions, new Date(), 1000,"",
                true, true, true, "en", true, true,
                             true, true , new HashMap<>(), new HashMap<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        accesoABase.insertarUsuario();

    }

    //Homepage
    @RequestMapping (path="/homepage/{idUser} viewAs = {userType}", method = RequestMethod.GET)
    public ResponseEntity<Object> getImgSubscriptions(@PathVariable Integer idUser,
                                              @PathVariable User userType){

        Map<Integer,Image>   subscriptionsImg = new HashMap<>();
        Map<Integer, Object> infoResponse     = new HashMap<>();
        Map<String,String>   requisitos       = new HashMap<>();

        requisitos.put("idUser",Integer.toString(idUser));

        User newUser = accesoABase.obtenerUsuarios(requisitos).get(0);
        List<Map<Integer,Date>>subscriptions = (List<Map<Integer, Date>>) newUser.getSubscriptions();

        if(subscriptions.isEmpty()){
            return new ResponseEntity<>(infoResponse, HttpStatus.CONFLICT);
        }
        else{
            for(Map<Integer,Date>map : subscriptions){
                for(Map.Entry<Integer, Date> entry : map.entrySet()){

                    HashMap<String,String>datos=new HashMap<>();
                    datos.put("idUser",Integer.toString(entry.getKey()));

                    Artist artista = (Artist)accesoABase.obtenerUsuarios(datos);
                    HashMap<Image,String>imagesArtist=artista.getImageList();

                    for(Map.Entry<Image,String>dato:imagesArtist.entrySet()){

                        //inserta imagenes de artista en el map
                        subscriptionsImg.put(artista.getIdUser(),dato.getKey());

                    }
                }
            }
        }

        infoResponse.put(idUser, subscriptionsImg);

        return new ResponseEntity<>(infoResponse, HttpStatus.OK);

    }


    //Obtiene el id de los artistas a los cuales esta subscripto
    @RequestMapping (path="/homepage/{idUser} viewAs = {userType}",method = RequestMethod.GET)
    public ArrayList<Integer> getSubscriptions(@PathVariable Integer idUser,
                                               @PathVariable User userType){

        ArrayList<Integer>subscripted=new ArrayList<>();
        Map<String,Integer>requisitos=new HashMap<>();

        requisitos.put("idUser",idUser);

        if(accesoABase.containsUser(idUser)){
            User usuario=accesoABase.obtenerUsuario(requisitos);
            HashMap<Integer,Date>subscriptions=usuario.getSubscriptions();

            for(Map)
        }

        return following;

    }




    @RequestMapping(value="/register", method=RequestMethod.POST)
    public ResponseEntity<Object>registrarUsuario(@RequestParam String usuarioJsonString){

        User newUser=gson.formJson(usuarioJsonString,User.class);

        Map<String, Object> infoResponse = new HashMap<>();

        if(accesoABase.containsUser(newUser.getIdUser())){
            response=new ResponseEntity<>(inforResponse,HttpStatus.UNAUTHORIZED);
        }
        else{
            accesoABase.insertarUsuario(newUser);
            response = new ResponseEntity<>(infoResponse, HttpStatus.CREATED);
        }
        return response;
    }


    /* ------- */




}
