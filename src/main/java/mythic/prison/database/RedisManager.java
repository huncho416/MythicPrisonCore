// src/main/java/mythic/prison/database/RedisManager.java
package mythic.prison.database;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.async.RedisAsyncCommands;

public class RedisManager {
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private RedisAsyncCommands<String, String> asyncCommands;

    public void connect() {
        try {
            RedisURI redisUri = RedisURI.Builder
                    .redis(DatabaseConfig.REDIS_HOST, DatabaseConfig.REDIS_PORT)
                    .withAuthentication(DatabaseConfig.REDIS_USERNAME, DatabaseConfig.REDIS_PASSWORD)
                    .build();

            redisClient = RedisClient.create(redisUri);
            connection = redisClient.connect();
            syncCommands = connection.sync();
            asyncCommands = connection.async();

            System.out.println("[MythicPrison] Connected to Redis successfully!");
        } catch (Exception e) {
            System.err.println("[MythicPrison] Failed to connect to Redis: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        System.out.println("[MythicPrison] Disconnected from Redis");
    }

    public RedisCommands<String, String> getSyncCommands() {
        return syncCommands;
    }

    public RedisAsyncCommands<String, String> getAsyncCommands() {
        return asyncCommands;
    }
}