// src/main/java/mythic/prison/database/MongoManager.java
package mythic.prison.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoManager {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void connect() {
        try {
            String connectionString = "mongodb://" + DatabaseConfig.MONGODB_HOST + ":" + DatabaseConfig.MONGODB_PORT;
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase(DatabaseConfig.MONGODB_DATABASE);
            System.out.println("[MythicPrison] Connected to MongoDB successfully!");
        } catch (Exception e) {
            System.err.println("[MythicPrison] Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("[MythicPrison] Disconnected from MongoDB");
        }
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public MongoDatabase getDatabase() {
        return database;
    }
}