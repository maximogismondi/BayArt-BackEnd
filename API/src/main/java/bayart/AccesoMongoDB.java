package bayart;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.lang.Exception;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Service
public class AccesoMongoDB {
    private MongoDatabase baseDeDatos;
    private MongoCollection<Document> coleccion;
    private String host;
    private int puerto;

    public AccesoMongoDB(){
        this.host="localhost";
        this.puerto=27017;
        this.conectarABaseDeDatos("Bayart");
        this.conectarAColeccion("Users");
    }

    public void conectarABaseDeDatos(String nombreBaseDeDatos){
        try {
            MongoClient mongo= new MongoClient(host,puerto);
            this.baseDeDatos=mongo.getDatabase(nombreBaseDeDatos);

        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    public void conectarAColeccion(String nombreDeColeccion) {
        if(existeLaColeccion(nombreDeColeccion)){
            this.coleccion = baseDeDatos.getCollection(nombreDeColeccion);
        } else {
            baseDeDatos.createCollection(nombreDeColeccion);
            this.coleccion = baseDeDatos.getCollection(nombreDeColeccion);
        }
    }

    public long obtenerCantidadDeDocumentos(){
        long cantidadDeRegistros= coleccion.count();
        return cantidadDeRegistros;
    }

    //Recibe los parametros que quieras (de usuarios) y te devuelve la lista de usuarios que cumplan los requisitos
    public ArrayList<User> obtenerUsuarios(Map<String,String> valoresRequeridos) {

        conectarAColeccion("Users");

        //almacena los requisitos en la lista de bson
        List<Bson>      filtros    = new ArrayList<>();
        ArrayList<User> foundUsers = new ArrayList<>();

        for (Map.Entry<String,String> atributo : valoresRequeridos.entrySet()) {
            Bson equivalencia = Filters.eq(atributo.getKey(),atributo.getValue());
            filtros.add(equivalencia);
        }


        //Forma un Archivo BSON con los filtros anteriormente formados
        Bson requisitosACumplir = and(filtros);

        //Consigue las tuplas de la base que coincidan con los valores del BSON anteriormente creado
        FindIterable resultados = coleccion.find(requisitosACumplir);

        //Crea un cursor con el cual recorrer los resultados
        MongoCursor iterador = resultados.iterator();


        //Va recorriendo las diferentes tuplas obtenidas en "resultados"
        //creando objetos de tipo "User" y agregandolos al array "foundUsers"
        while(iterador.hasNext()){

            Document document        = (Document) iterador.next();

            Integer  idUser          = document.getInteger("idUser");
            String   userName        = document.getString("username");
            String   eMail           = document.getString("eMail");
            String   password        = document.getString("password");
            Date     birthDate       = document.getDate("birthdate");
            Date     inscriptionDate = document.getDate("inscriptionDate");
            Integer  bpoints         = document.getInteger("bpoints");
            String   profilePicture  = document.getString("profilePicture");
            Boolean  libraryPrivacy =  document.getBoolean("libraryPrivacy");
            Boolean  historyStore    = document.getBoolean("historyStorage");
            Boolean  theme           = document.getBoolean("theme");
            String   language        = document.getString("language");
            Boolean  notificationsNewPublication                 = document.getBoolean("notifications-new-publication");
            Boolean  notificationsSubEnding                      = document.getBoolean("notifications-sub-ending");
            Boolean  notificationsBuyAlert                       = document.getBoolean("notifications-buy-alert");
            Boolean  notificationsInformSponsor                  = document.getBoolean("notifications-inform-sponsor");
            HashMap<Integer,Date>                  subscriptions = (HashMap<Integer, Date>)                   document.get("subscriptions");
            HashMap<Integer,HashMap<Integer,Date>> sponsors      = (HashMap<Integer, HashMap<Integer, Date>>) document.get("sponsors");
            ArrayList<Integer>                     bookmarks     = (ArrayList<Integer>)                       document.get("bookmarks");
            ArrayList<String>                      history       = (ArrayList<String>)                        document.get("history");
            ArrayList<Integer>                     purchased     = (ArrayList<Integer>)                       document.get("purchased");

            User usuario = new User(idUser,userName,eMail,password,birthDate,inscriptionDate,
                                   bpoints,profilePicture,libraryPrivacy,historyStore,theme,
                                   language,notificationsNewPublication,notificationsSubEnding,
                                   notificationsBuyAlert,notificationsInformSponsor,subscriptions,
                                   sponsors, bookmarks,history,purchased);

            foundUsers.add(usuario);
        }

        return  foundUsers;

    }

    //Recibe los parametros que quieras (de artistas) y te devuelve la lista de artistas que cumplan los requisitos
    public ArrayList<Artist> obtenerArtistas(Map<String,String> valoresRequeridos) {

        conectarAColeccion("Artists");

        //Almacena los requisitos en la lista de bson
        ArrayList<Artist> foundArtists = new ArrayList<>();
        List<Bson>        filtros      = new ArrayList<>();

        for (Map.Entry<String,String> atributo : valoresRequeridos.entrySet()) {
            Bson equivalencia = Filters.eq(atributo.getKey(),atributo.getValue());
            filtros.add(equivalencia);
        }


        //Forma un Archivo BSON con los filtros anteriormente formados
        Bson requisitosACumplir = and(filtros);

        //Consigue las tuplas de la base que coincidan con los valores del BSON anteriormente creado
        FindIterable resultados = coleccion.find(requisitosACumplir);

        //Crea un cursor con el cual recorrer los resultados
        MongoCursor iterador = resultados.iterator();


        //Va recorriendo las diferentes tuplas obtenidas en "resultados"
        //creando objetos de tipo "Artist" y agregandolos al array "foundArtists"
        while(iterador.hasNext()){

            Document document        = (Document) iterador.next();

            Integer  idUser          = document.getInteger("idUser");
            String   userName        = document.getString("username");
            String   eMail           = document.getString("eMail");
            String   password        = document.getString("password");
            Date     birthDate       = document.getDate("birthdate");
            Date     inscriptionDate = document.getDate("inscriptionDate");
            Integer  bpoints         = document.getInteger("bpoints");
            String   profilePicture  = document.getString("profilePicture");
            Boolean  libraryPrivacy  = document.getBoolean("libraryPrivacy");
            Boolean  historyStorage  = document.getBoolean("historyStorage");
            Boolean  theme           = document.getBoolean("theme");
            String   language        = document.getString("language");
            Boolean  notificationsNewPublication  = document.getBoolean("notifications-new-publication");
            Boolean  notificationsSubEnding       = document.getBoolean("notifications-sub-ending");
            Boolean  notificationsBuyAlert        = document.getBoolean("notifications-buy-alert");
            Boolean  notificationsInformSponsor   = document.getBoolean("notifications-inform-sponsor");
            HashMap<Integer,Date>                  subscriptions = (HashMap<Integer, Date>)                   document.get("subscriptions");
            HashMap<Integer,HashMap<Integer,Date>> sponsors      = (HashMap<Integer, HashMap<Integer, Date>>) document.get("sponsors");
            HashMap<Image,String>                  imageList     = (HashMap<Image,String>)                    document.get("imageList");
            ArrayList<String>                      history       = (ArrayList<String>)                        document.get("history");
            ArrayList<Integer>                     purchased     = (ArrayList<Integer>)                       document.get("purchased");
            ArrayList<Integer>                     bookmarks     = (ArrayList<Integer>)                       document.get("bookmarks");
            Boolean  notificationsNewSub  = document.getBoolean("notifications-new-sub");
            Boolean  notificationsSell    = document.getBoolean("notifications-sell");
            Boolean  notificationsSponsor = document.getBoolean("notifications-sponsor");

            Artist artista = new Artist(idUser,userName,eMail,password,birthDate,inscriptionDate,
                    bpoints,profilePicture,libraryPrivacy,historyStorage,theme,
                    language,notificationsNewPublication,notificationsSubEnding,
                    notificationsBuyAlert,notificationsInformSponsor,subscriptions,sponsors,
                    bookmarks,history,purchased,imageList,notificationsNewSub,notificationsSell,notificationsSponsor);

            foundArtists.add(artista);
        }

        return  foundArtists;

    }

    public void insertarUsuario(User usuario, HashMap<Image,String> imageList ){

        conectarAColeccion("Users");

        Document nuevoDocumento = new Document();
        nuevoDocumento.append("idUser",null);
        nuevoDocumento.append("username",usuario.getUsername());
        nuevoDocumento.append("password",usuario.getPassword());
        nuevoDocumento.append("email",usuario.geteMail());
        nuevoDocumento.append("birthDate",usuario.getBirthDate());
        nuevoDocumento.append("inscriptionDate",usuario.getInscriptionDate());
        nuevoDocumento.append("bpoints",usuario.getBpoints());
        nuevoDocumento.append("libraryPrivacy",usuario.getLibraryPrivacy());
        nuevoDocumento.append("historyStorage",usuario.getHistoryStore());
        nuevoDocumento.append("theme",usuario.getTheme());
        nuevoDocumento.append("language",usuario.getLanguage());
        nuevoDocumento.append("notifications-new-publication",true);
        nuevoDocumento.append("notifications-sub-ending",true);
        nuevoDocumento.append("notifications-buy-alert",true);
        nuevoDocumento.append("notifications-inform-sponsor",true);
        nuevoDocumento.append("subscriptions",usuario.getSubscriptions());
        nuevoDocumento.append("sponsors",usuario.getSponsors());
        nuevoDocumento.append("bookmarks",usuario.getBookmarks());
        nuevoDocumento.append("history",usuario.getHistory());
        nuevoDocumento.append("purchased",usuario.getPurchased());

        coleccion.insertOne(nuevoDocumento);

        if(imageList != null){
            conectarAColeccion("Artists");

            HashMap<String,String> parametrosUsuario = new HashMap<>();
            parametrosUsuario.put("eMail",usuario.geteMail());
            Integer idUser = obtenerUsuarios(parametrosUsuario).get(0).getIdUser();

            Document nuevoDocumentoArtista = new Document();

            nuevoDocumentoArtista.append("idUser",idUser);
            nuevoDocumentoArtista.append("notifications-new-sub",true);
            nuevoDocumentoArtista.append("notifications-sell",true);
            nuevoDocumentoArtista.append("notifications-sponsor",true);
            nuevoDocumentoArtista.append("imageList",imageList);

            coleccion.insertOne(nuevoDocumentoArtista);
        }

    }//en el php el archivo tiene que crear un .json con todos los datos

    public boolean existeLaColeccion(String nombreDeColeccion) {
        MongoIterable<String> nombreDeColecciones = baseDeDatos.listCollectionNames();

        for (String nombre:nombreDeColecciones) {
            if(nombre.equals(nombreDeColeccion)){
                return true;
            }
        }
        return false;
    }
}