package main.java;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class Database {
    Database(String dbLocation)
    {
        DB db = DBMaker.fileDB(dbLocation).fileMmapEnable().closeOnJvmShutdown().make();

        HTreeMap<String, String> paraMap = db.hashMap("para_map", Serializer.STRING, Serializer.STRING).createOrOpen();
        HTreeMap<String, String> entityMap = db.hashMap("entity_map", Serializer.STRING, Serializer.STRING).createOrOpen();
        HTreeMap<String, String> e2eDistMap = db.hashMap("e2e_dist", Serializer.STRING, Serializer.STRING).createOrOpen();
        HTreeMap<String, String> p2eDistMap  = db.hashMap("p2e_dist", Serializer.STRING, Serializer.STRING).createOrOpen();
        HTreeMap<String, Double> weightMap  = db.hashMap("weight_map", Serializer.STRING, Serializer.DOUBLE).createOrOpen();
    }
}
