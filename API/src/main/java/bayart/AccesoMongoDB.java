package bayart;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.lang.Exception;

import static com.mongodb.client.model.Filters.*;

@Service
public class AccesoMongoDB {
    private MongoDatabase baseDeDatos;
    private MongoCollection<Document> coleccion;
    private String host;
    private int puerto;

    public AccesoMongoDB() {
        this.host = "localhost";
        this.puerto = 27017;
        this.conectarABaseDeDatos("Bayart");
        this.conectarAColeccion("Users");
    }

    public void conectarABaseDeDatos(String nombreBaseDeDatos) {
        try {
            MongoClient mongo = new MongoClient(host, puerto);
            this.baseDeDatos = mongo.getDatabase(nombreBaseDeDatos);

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void conectarAColeccion(String nombreDeColeccion) {
        if (existeLaColeccion(nombreDeColeccion)) {
            this.coleccion = baseDeDatos.getCollection(nombreDeColeccion);
        } else {
            baseDeDatos.createCollection(nombreDeColeccion);
            this.coleccion = baseDeDatos.getCollection(nombreDeColeccion);
        }
    }

    //Recibe los parametros que quieras (de usuarios) y te devuelve la lista de usuarios que cumplan los requisitos
    public ArrayList<User> obtenerUsuarios(Map<String, String> valoresRequeridos) {

        conectarAColeccion("Users");

        //almacena los requisitos en la lista de bson
        List<Bson> filtros = new ArrayList<>();
        ArrayList<User> foundUsers = new ArrayList<>();

        for (Map.Entry<String, String> atributo : valoresRequeridos.entrySet()) {
            Bson equivalencia = Filters.eq(atributo.getKey(), atributo.getValue());
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
        while (iterador.hasNext()) {

            Document document = (Document) iterador.next();

            Integer idUser = document.getInteger("idUser");
            String userName = document.getString("username");
            String eMail = document.getString("eMail");
            String password = document.getString("password");
            Date birthDate = document.getDate("birthdate");
            Date inscriptionDate = document.getDate("inscriptionDate");
            Integer bpoints = document.getInteger("bpoints");
            String profilePicture = document.getString("profilePicture");
            Boolean libraryPrivacy = document.getBoolean("libraryPrivacy");
            Boolean historyStore = document.getBoolean("historyStorage");
            Boolean theme = document.getBoolean("theme");
            String language = document.getString("language");
            Boolean notificationsNewPublication = document.getBoolean("notificationsNewPublication");
            Boolean notificationsSubEnding = document.getBoolean("notificationsSubEnding");
            Boolean notificationsBuyAlert = document.getBoolean("notificationsBuyAlert");
            Boolean notificationsInformSponsor = document.getBoolean("notificationsInformSponsor");
            HashMap<Integer, Date> subscriptions = (HashMap<Integer, Date>) document.get("subscriptions");
            HashMap<Integer, HashMap<Integer, Date>> sponsors = (HashMap<Integer, HashMap<Integer, Date>>) document.get("sponsors");
            ArrayList<Integer> bookmarks = (ArrayList<Integer>) document.get("bookmarks");
            ArrayList<String> history = (ArrayList<String>) document.get("history");
            ArrayList<Integer> purchased = (ArrayList<Integer>) document.get("purchased");

            User usuario = new User(idUser, userName, eMail, password, birthDate, inscriptionDate,
                    bpoints, profilePicture, libraryPrivacy, historyStore, theme,
                    language, notificationsNewPublication, notificationsSubEnding,
                    notificationsBuyAlert, notificationsInformSponsor, subscriptions,
                    sponsors, bookmarks, history, purchased);

            foundUsers.add(usuario);
        }

        return foundUsers;

    }

    //Recibe los parametros que quieras (de artistas) y te devuelve la lista de artistas que cumplan los requisitos
    public ArrayList<Artist> obtenerArtistas(Map<String, String> valoresRequeridos) {

        conectarAColeccion("Artists");

        //Almacena los requisitos en la lista de bson
        ArrayList<Artist> foundArtists = new ArrayList<>();
        List<Bson> filtros = new ArrayList<>();

        for (Map.Entry<String, String> atributo : valoresRequeridos.entrySet()) {
            Bson equivalencia = Filters.eq(atributo.getKey(), atributo.getValue());
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
        while (iterador.hasNext()) {

            Document document = (Document) iterador.next();
            ArrayList<Image> imageList = (ArrayList<Image>) document.get("imageList");
            Boolean notificationsNewSub = document.getBoolean("notificationsNewSub");
            Boolean notificationsSell = document.getBoolean("notificationsSell");
            Boolean notificationsSponsor = document.getBoolean("notificationsSponsor");

            Artist artista = new Artist(imageList, notificationsNewSub, notificationsSell, notificationsSponsor);

            foundArtists.add(artista);
        }

        return foundArtists;

    }

    public void insertarUsuario(String username, String eMail, String password, Date inscriptionDate, String artista) {

        conectarAColeccion("Users");

        Document nuevoDocumento = new Document();
        nuevoDocumento.append("idUser", null);
        nuevoDocumento.append("username", username);
        nuevoDocumento.append("password", password);
        nuevoDocumento.append("email", eMail);
        nuevoDocumento.append("inscriptionDate", inscriptionDate);
        nuevoDocumento.append("birthDate", new Date());
        nuevoDocumento.append("bpoints", 1000);
        nuevoDocumento.append("libraryPrivacy", true);
        nuevoDocumento.append("historyStorage", true);
        nuevoDocumento.append("theme", true);
        nuevoDocumento.append("language", "es");
        nuevoDocumento.append("notificationsNewPublication", true);
        nuevoDocumento.append("notificationsSubEnding", true);
        nuevoDocumento.append("notificationsBuyAlert", true);
        nuevoDocumento.append("notificationsInformSponsor", true);
        nuevoDocumento.append("subscriptions", new HashMap<>());
        nuevoDocumento.append("sponsors", new HashMap<>());
        nuevoDocumento.append("bookmarks", new ArrayList<>());
        nuevoDocumento.append("history", new ArrayList<>());
        nuevoDocumento.append("purchased", new ArrayList<>());

        coleccion.insertOne(nuevoDocumento);

        if (artista == "artist") {
            conectarAColeccion("Artists");

            HashMap<String, String> parametrosUsuario = new HashMap<>();
            parametrosUsuario.put("eMail", eMail);
            Integer idUser = obtenerUsuarios(parametrosUsuario).get(0).getIdUser();

            Document nuevoDocumentoArtista = new Document();

            nuevoDocumentoArtista.append("idUser", idUser);
            nuevoDocumentoArtista.append("notificationsNewSub", true);
            nuevoDocumentoArtista.append("notificationsSell", true);
            nuevoDocumentoArtista.append("notificationsSponsor", true);
            nuevoDocumentoArtista.append("imageList", new HashMap<>());

            coleccion.insertOne(nuevoDocumentoArtista);
        }

    }//en el php el archivo tiene que crear un .json con todos los datos

    public void addImage(String idUser, String idImage, String action) {
        conectarAColeccion("Users");

        Map<String, String> parametros = new HashMap<>();
                            parametros.put("idUser",idUser);

        User usuario = obtenerUsuarios(parametros).get(0);

        List<Bson> filtros = new ArrayList<>();

        for (Map.Entry<String, String> atributo : parametros.entrySet()) {
            Bson equivalencia = Filters.eq(atributo.getKey(), atributo.getValue());
            filtros.add(equivalencia);
        }

        Bson requisitosACumplir = and(filtros);

        String json = "{ $push: { " + action + ":{ idImage:" + idImage + ", raspberryDisplay: true}}}";

        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(requisitosACumplir, (Bson)push);
    }

    public boolean existeLaColeccion(String nombreDeColeccion) {
        MongoIterable<String> nombreDeColecciones = baseDeDatos.listCollectionNames();

        for (String nombre : nombreDeColecciones) {
            if (nombre.equals(nombreDeColeccion)) {
                return true;
            }
        }
        return false;
    }
}