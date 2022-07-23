package bayart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.lang.Exception;
import java.io.File;

import static com.mongodb.client.model.Filters.*;

@Service
public class AccesoMongoDB {
    private String host;
    private int puerto;
    private MongoDatabase baseDeDatos;
    private MongoCollection<Document> coleccion;
    private DB db;

    //JOYA
    public void conectarABaseDeDatos(String nombreBaseDeDatos) {
        try {
            MongoClient mongo = new MongoClient(host, puerto);
            db = new DB(mongo, "bayart");
            this.baseDeDatos = mongo.getDatabase(nombreBaseDeDatos);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //JOYA
    public AccesoMongoDB() {
        this.host = "localhost";
        this.puerto = 27017;
        this.conectarABaseDeDatos("bayart");
        this.conectarAColeccion("users");
    }

    //JOYA
    public boolean existeLaColeccion(String nombreDeColeccion) {
        MongoIterable<String> nombreDeColecciones = baseDeDatos.listCollectionNames();

        for (String nombre : nombreDeColecciones) {
            if (nombre.equals(nombreDeColeccion)) {
                return true;
            }
        }
        return false;
    }

    //JOYA
    public void conectarAColeccion(String nombreDeColeccion) {
        if (existeLaColeccion(nombreDeColeccion)) {
            this.coleccion = baseDeDatos.getCollection(nombreDeColeccion);
        } else {
            baseDeDatos.createCollection(nombreDeColeccion);
            this.coleccion = baseDeDatos.getCollection(nombreDeColeccion);
        }
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
    public void modificarBpoints(Integer idUser, Integer bpoints) {

        Map<String, String> parametros = new HashMap<>();
        parametros.put("idUser", Integer.toString(idUser));

        Integer bpointsUsuario = obtenerUsuarios(parametros).get(0).getBpoints() + bpoints;

        Bson equivalencia = Filters.eq("idUser", idUser);

        String json = "{ $set: { bpoints:" + bpointsUsuario + "}}";
        DBObject push = (DBObject) JSON.parse(json);

        conectarAColeccion("users");
        coleccion.updateOne(equivalencia, (Bson) push);
    }

    //JOYA
    public ArrayList<User> obtenerUsuarios(Map<String, String> valoresRequeridos) {

        conectarAColeccion("users");

        //almacena los requisitos en la lista de bson
        List<Bson> filtros = new ArrayList<>();
        ArrayList<User> foundUsers = new ArrayList<>();

        for (Map.Entry<String, String> atributo : valoresRequeridos.entrySet()) {
            if (atributo.getKey().equals("idUser")) {
                Bson equivalencia = Filters.eq(atributo.getKey(), Integer.parseInt(atributo.getValue()));
                filtros.add(equivalencia);
            } else {
                Bson equivalencia = Filters.eq(atributo.getKey(), atributo.getValue());
                filtros.add(equivalencia);
            }
        }

        //Forma un Archivo BSON con los filtros anteriormente formados

        FindIterable resultados;

        if (filtros.isEmpty()) {
            resultados = coleccion.find();
        } else {
            Bson requisitosACumplir = and(filtros);
            resultados = coleccion.find(requisitosACumplir);
        }

        //Crea un cursor con el cual recorrer los resultados
        MongoCursor iterador = resultados.iterator();

        //Va recorriendo las diferentes tuplas obtenidas en "resultados"
        //creando objetos de tipo "User" y agregandolos al array "foundUsers"
        while (iterador.hasNext()) {
            Document document = (Document) iterador.next();

            Integer idUser = document.getInteger("idUser");
            String userName = document.getString("username");
            String eMail = document.getString("email");
            String password = document.getString("password");
            Date birthDate = document.getDate("birthdate");
            Date inscriptionDate = document.getDate("inscriptionDate");
            Integer bpoints = document.getInteger("bpoints");
            Boolean notificationsNewPublication = document.getBoolean("notificationsNewPublication");
            Boolean notificationsBuyAlert = document.getBoolean("notificationsBuyAlert");
            Boolean notificationsInformSponsor = document.getBoolean("notificationsInformSponsor");
            ArrayList<Integer> subscriptions = (ArrayList<Integer>) document.get("subscriptions");
            ArrayList<ArrayList<Integer>> sponsors = (ArrayList<ArrayList<Integer>>) document.get("sponsors");
            ArrayList<Integer> bookmarks = (ArrayList<Integer>) document.get("bookmarks");
            ArrayList<Integer> purchased = (ArrayList<Integer>) document.get("purchased");
            ArrayList<Integer> dailyRewards = (ArrayList<Integer>) document.get("dailyRewards");

            User usuario = new User(idUser, userName, eMail, password, birthDate, inscriptionDate,
                    bpoints, notificationsNewPublication,
                    notificationsBuyAlert, notificationsInformSponsor, subscriptions,
                    sponsors, bookmarks, purchased, dailyRewards);

            foundUsers.add(usuario);
        }
        return foundUsers;
    }

    //JOYA
    public ArrayList<Artist> obtenerArtistas(Map<String, String> valoresRequeridos) {

        //Almacena los requisitos en la lista de bson
        ArrayList<Artist> foundArtists = new ArrayList<>();
        List<Bson> filtros = new ArrayList<>();

        for (Map.Entry<String, String> atributo : valoresRequeridos.entrySet()) {
            Bson equivalencia = Filters.eq(atributo.getKey(), Integer.parseInt(atributo.getValue()));
            filtros.add(equivalencia);
        }

        //Consigue las tuplas de la base que coincidan con los valores del BSON anteriormente creado
        conectarAColeccion("artists");
        FindIterable resultados;

        if (filtros.isEmpty()) {
            resultados = coleccion.find();
        } else {
            Bson requisitosACumplir = and(filtros);
            resultados = coleccion.find(requisitosACumplir);
        }

        //Crea un cursor con el cual recorrer los resultados
        MongoCursor iterador = resultados.iterator();

        while (iterador.hasNext()) {

            Document document = (Document) iterador.next();

            Integer idUser = document.getInteger("idUser");
            ArrayList<Integer> imageList = (ArrayList<Integer>) document.get("imageList");
            Boolean notificationsNewSub = document.getBoolean("notificationsNewSub");
            Boolean notificationsSell = document.getBoolean("notificationsSell");
            Boolean notificationsSponsor = document.getBoolean("notificationsSponsor");

            Artist artista = new Artist(idUser, imageList, notificationsNewSub, notificationsSell, notificationsSponsor);

            foundArtists.add(artista);
        }

        return foundArtists;

    }

    //JOYA
    public ArrayList<Image> obtenerImagenes(Map<String, String> requiredValue) {

        ArrayList<Image> resultImages = new ArrayList<>();//lista de imágenes que va a retornar
        Map<String, Object> requirements = new HashMap<>();

        for (Map.Entry<String, String> valor : requiredValue.entrySet()) {
            if (valor.getKey().equals("word")) {//si existe un filtro por palabra
                requirements.put("word", valor.getValue());//para buscar por nombre y descripción
            } else if (valor.getKey().equals("tags")) {//si existe un filtro por tag
                requirements.put(valor.getKey(), desconcatenarFiltros(valor.getValue()));
            } else if (valor.getKey().equals("idImage")) {//para buscar por idImage
                requirements.put(valor.getKey(), Integer.parseInt(valor.getValue()));
            } else if (valor.getKey().equals("maxPrice")) {//si existe un filtro por maxPrice
                requirements.put(valor.getKey(), valor.getValue());
            } else if (valor.getKey().equals("salable")) {//si existe un filtro por maxPrice
                requirements.put(valor.getKey(), Boolean.parseBoolean(valor.getValue()));
            } else if (valor.getKey().equals("title")) {
                requirements.put(valor.getKey(), valor.getValue());
            } else if (valor.getKey().equals("idUser")) {
                requirements.put(valor.getKey(), Integer.parseInt(valor.getValue()));
            }
        }

        //almacena los requisitos en la lista de bson
        conectarAColeccion("images");
        Bson filtro = null;
        FindIterable resultados = coleccion.find();

        for (Map.Entry<String, Object> atributo : requirements.entrySet()) {
            if (atributo.getKey().equals("idImage")) {
                filtro = eq("idImage", atributo.getValue());
                resultados = coleccion.find(filtro);
            } else if (atributo.getKey().equals("title")) {
                filtro = eq("name", atributo.getValue());
                resultados = coleccion.find(filtro);
            } else if (atributo.getKey().equals("idUser")) {
                filtro = eq("idUser", atributo.getValue());
                resultados = coleccion.find(filtro);
            }
        }

        //Crea un cursor con el cual recorrer los resultados
        MongoCursor iterador = resultados.iterator();

        while (iterador.hasNext()) {

            Document document = (Document) iterador.next();

            Integer idImage = document.getInteger("idImage");
            Integer idUser = document.getInteger("idUser");
            String name = document.getString("name");
            Integer price = document.getInteger("price");
            Date postDate = document.getDate("postDate");
            String description = document.getString("description");
            ArrayList<String> tags = (ArrayList<String>) document.get("tags");

            Image image = new Image(idImage, idUser, name, price, postDate, description, tags);
            resultImages.add(image);
        }

        ArrayList<Image> imagesRemove = new ArrayList<>();

        for (Map.Entry<String, Object> parametro : requirements.entrySet()) {
            switch (parametro.getKey()) {
                case "tags":
                    ArrayList<String> tags = (ArrayList<String>) parametro.getValue();
                    for (Image image : resultImages) {
                        Boolean contieneTag = false;
                        for (String tag : tags) {
                            if (image.getTags() != null && image.getTags().contains(tag)) {
                                contieneTag = true;
                            }
                        }
                        if (!contieneTag) {
                            imagesRemove.add(image);
                        }
                    }
                    break;

                case "maxPrice":
                    Integer maxPrice = Integer.parseInt((String) parametro.getValue());
                    for (Image image : resultImages) {//recorre las imágenes resultado de los filtros para filtrar por precio
                        if (image.getPrice() > maxPrice) {
                            imagesRemove.add(image);
                        }
                    }
                    break;

                case "word":
                    for (Image image : resultImages) {
                        if (!image.getName().toLowerCase().contains((String) parametro.getValue()) && !image.getDescription().toLowerCase().contains((String) parametro.getValue())) {
                            imagesRemove.add(image);
                        }
                    }
                    break;

                case "salable":
                    if ((Boolean) parametro.getValue()) {
                        for (Image image : resultImages) {
                            if (image.getPrice() == 0) {
                                imagesRemove.add(image);
                            }
                        }
                    } else {
                        for (Image image : resultImages) {
                            if (image.getPrice() > 0) {
                                imagesRemove.add(image);
                            }
                        }
                    }
                    break;

                default:
                    return resultImages;
            }
        }

        for (Image image : imagesRemove) {
            resultImages.remove(image);
        }

        return resultImages;
    }

    //JOYA
    public void insertarUsuario(String username, String email, String password, Date birthDate, String type) throws IOException {

        conectarAColeccion("users");
        Integer idUser = 1 + (int) coleccion.count();

        guardarImagenMongo("iVBORw0KGgoAAAANSUhEUgAAAfUAAAH5CAYAAACYiqdqAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAIGNIUk0AAHolAACAgwAA+f8AAIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAAAZ1SURBVHja7NVBEQAwCMCwMYHYQBoyQQd3iYR+GpU9DwA470sAAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6hIAgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AGDqAGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gCAqQOAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCYOgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDAKYOAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAYOoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQMApg4Apg4AmDoAYOoAgKkDgKkDAKYOAJg6AGDqAICpA4CpAwCmDgCYOgBg6gBg6gCAqQMApg4AmDoAmDoAYOoAgKkDAKYOAJg6AJg6AGDqAICpAwCmDgCmDgCYOgBg6gCAqQOAqQMAxy0AAAD//wMA1xwGTRUGyiIAAAAASUVORK5CYII="
                , "imageProfile" + idUser);

        Document nuevoDocumento = new Document();
        nuevoDocumento.append("idUser", idUser);
        nuevoDocumento.append("username", username);
        nuevoDocumento.append("password", password);
        nuevoDocumento.append("email", email);
        nuevoDocumento.append("inscriptionDate", new Date());
        nuevoDocumento.append("birthDate", birthDate);
        nuevoDocumento.append("bpoints", 10000);
        nuevoDocumento.append("notificationsNewPublication", true);
        nuevoDocumento.append("notificationsBuyAlert", true);
        nuevoDocumento.append("notificationsInformSponsor", true);
        nuevoDocumento.append("subscriptions", new ArrayList<>());
        nuevoDocumento.append("sponsors", new ArrayList<>());
        nuevoDocumento.append("bookmarks", new ArrayList<>());
        nuevoDocumento.append("purchased", new ArrayList<>());
        nuevoDocumento.append("dailyRewards", new ArrayList<>());

        conectarAColeccion("users");
        coleccion.insertOne(nuevoDocumento);

        if (type.equals("artist")) {

            guardarImagenMongo("iVBORw0KGgoAAAANSUhEUgAACWkAAALpCAYAAADL4DZOAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAIGNIUk0AAHolAACAgwAA+f8AAIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAACJWSURBVHja7NoxAQAgDMCwgf8fCcgEFf0SCb275p43AAAAAAAAAAAAJLYEAAAAAAAAAAAAHZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhExaAAAAAAAAAAAAIZMWAAAAAAAAAABAyKQFAAAAAAAAAAAQMmkBAAAAAAAAAACETFoAAAAAAAAAAAAhkxYAAAAAAAAAAEDIpAUAAAAAAAAAABAyaQEAAAAAAAAAAIRMWgAAAAAAAAAAACGTFgAAAAAAAAAAQMikBQAAAAAAAAAAEDJpAQAAAAAAAAAAhD4AAAD//wMAcGkIDzT1KjoAAAAASUVORK5CYII="
                    , "imageBanner" + idUser);

            Document nuevoDocumentoArtista = new Document();

            nuevoDocumentoArtista.append("idUser", idUser);
            nuevoDocumentoArtista.append("imageList", new ArrayList<>());
            nuevoDocumentoArtista.append("notificationsNewSub", true);
            nuevoDocumentoArtista.append("notificationsSell", true);
            nuevoDocumentoArtista.append("notificationsSponsor", true);

            conectarAColeccion("artists");
            coleccion.insertOne(nuevoDocumentoArtista);
        }
    }

    //JOYA
    public void addImageToUser(String idUser, String idImage, String action) {
        conectarAColeccion("users");

        Map<String, String> parametros = new HashMap<>();
        parametros.put("idUser", idUser);

        Bson equivalencia = Filters.eq("idUser", Integer.parseInt(idUser));

        String json = null;

        if (action.equals("removeBookmark")) {
            json = "{ $pull: { bookmarks: { $in: [" + idImage + "]  }}}";
        } else if (action.equals("addBookmark")) {
            json = "{ $push: { bookmarks:" + idImage + "}}";
        } else if (action.equals("buy")) {
            if (obtenerUsuarios(parametros).get(0).getBookmarks().contains(Integer.parseInt(idImage))) {
                addImageToUser(idUser, idImage, "removeBookmark");
            }
            json = "{ $push: { purchased:" + idImage + "}}";
        }

        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(equivalencia, (Bson) push);
    }

    //JOYA
    public void cambiarUsername_Email_Password(String idUser, String field, String change) {
        conectarAColeccion("users");

        Bson filter = Filters.eq("idUser", Integer.parseInt(idUser));

        String json = "{ $set: { " + field + ":'" + change + "'}}";

        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(filter, (Bson) push);
    }

    //JOYA
    public void cambiarNotificaciones(String idUser, String field, String change){
        Bson filter = Filters.eq("idUser", Integer.parseInt(idUser));

        String json = "{ $set: { " + field + ":" + change + "} }";

        DBObject push = (DBObject) JSON.parse(json);

        if (field.equals("notificationsNewPublication") || field.equals("notificationsBuyAlert") || field.equals("notificationsInformSponsor")){
            conectarAColeccion("users");
        } else {
            conectarAColeccion("artists");
        }
        coleccion.updateOne(filter, (Bson) push);
    }

    //JOYA
    public void borrarImagenMongo(String nombreObjetoFile) {
        conectarAColeccion("images.files");

        Bson filter = Filters.eq("filename", nombreObjetoFile);
        FindIterable imageFile = coleccion.find(filter);
        MongoCursor iterador = imageFile.iterator();
        Document document = (Document) iterador.next();

        String idImagen = document.getObjectId("_id").toHexString();

        filter = Filters.eq("_id", new ObjectId(idImagen));

        coleccion.deleteOne(filter);

        conectarAColeccion("images.chunks");

        filter = Filters.eq("files_id", new ObjectId(idImagen));

        coleccion.deleteMany(filter);
    }

    //JOYA
    public void guardarImagenMongo(String encodedString, String nombreObjetoFile) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        FileUtils.writeByteArrayToFile(new File("src/main/resources/tempImages/" + nombreObjetoFile), decodedBytes);

        File imagen = new File("src/main/resources/tempImages/" + nombreObjetoFile);
        GridFS gfsPhoto = new GridFS(db, "images");
        GridFSInputFile gfsFile = gfsPhoto.createFile(imagen);
        gfsFile.setFilename(nombreObjetoFile);
        gfsFile.save();

        imagen.delete();
    }

    //JOYA
    public void guardarImagen(String encodedString, String nombreObjetoFile, Integer idUser, Integer price, String description, ArrayList<String> tags) throws IOException {

        guardarImagenMongo(encodedString, nombreObjetoFile);

        conectarAColeccion("images");
        Integer idImage = 1 + (int) coleccion.count();

        Document nuevoDocumento = new Document();

        nuevoDocumento.append("idImage", idImage);
        nuevoDocumento.append("idUser", idUser);
        nuevoDocumento.append("name", nombreObjetoFile);
        nuevoDocumento.append("price", price);
        nuevoDocumento.append("postDate", new Date());
        nuevoDocumento.append("description", description);
        if (!tags.get(0).equals("null")) {
            nuevoDocumento.append("tags", tags);
        }

        coleccion.insertOne(nuevoDocumento);

        conectarAColeccion("artists");
        Bson equivalenciaAId = Filters.eq("idUser", idUser);

        String json = "{ $push: {imageList: " + idImage + "}}";
        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(equivalenciaAId, (Bson) push);
    }

    //JOYA
    public String obtenerImagenEnMongoDB(String nombreObjetoFile) {
        try {
            GridFS gfsPhoto = new GridFS(db, "images");
            GridFSDBFile imageForOutput = gfsPhoto.findOne(nombreObjetoFile);
            File imagen = new File("src/main/resources/tempImages/xd");
            imageForOutput.writeTo(imagen);

            byte[] fileContent = FileUtils.readFileToByteArray(imagen);
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            imagen.delete();

            return encodedString;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //JOYA
    public void addSub(Integer idUser, Integer idArtist) {

        conectarAColeccion("users");

        Bson equivalencia = eq("idUser", idUser);

        String json = "{ $push : { subscriptions : " + idArtist + " } }";

        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(equivalencia, (Bson) push);
    }

    //JOYA
    public void stopSub(String idUser, Integer idArtist) {

        Bson equivalencia = Filters.eq("idUser", Integer.parseInt(idUser));

        String json = "{ $pull: { subscriptions: { $in: [" + idArtist + "] }}}";

        DBObject push = (DBObject) JSON.parse(json);

        conectarAColeccion("users");
        coleccion.updateOne(equivalencia, (Bson) push);
    }

    //JOYA
    public void addSponsor(String idUser, String idArtist, Integer percentage) {

        conectarAColeccion("users");

        Bson equivalencia = eq("idUser", Integer.parseInt(idUser));

        String json = "{ $push : { sponsors : [" + Integer.parseInt(idArtist) + "," + percentage + "] } }";

        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(equivalencia, (Bson) push);
    }

    //JOYA
    public void stopSponsor(String idUser, Integer idArtist) {

        Bson equivalencia = Filters.eq("idUser", Integer.parseInt(idUser));

        String json = "{ $pull: { sponsors: { $in: [" + idArtist + "] }}}";

        DBObject push = (DBObject) JSON.parse(json);

        conectarAColeccion("users");
        coleccion.updateOne(equivalencia, (Bson) push);
    }

    //JOYA
    public void updateDailyRewards(Integer idUser, ArrayList<Integer> dailyRewards) {
        conectarAColeccion("users");

        Bson filter = Filters.eq("idUser", idUser);

        String json = "{ $set: { dailyRewards:" + dailyRewards + "}}";

        DBObject push = (DBObject) JSON.parse(json);

        coleccion.updateOne(filter, (Bson) push);
    }

}