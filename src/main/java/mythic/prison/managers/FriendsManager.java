package mythic.prison.managers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import mythic.prison.MythicPrison;
import mythic.prison.data.social.Friend;
import mythic.prison.data.social.FriendRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FriendsManager {
    private final Map<String, List<Friend>> friendsCache = new ConcurrentHashMap<>();
    private final Map<String, List<FriendRequest>> requestsCache = new ConcurrentHashMap<>();
    private MongoCollection<Document> friendsCollection;
    private MongoCollection<Document> requestsCollection;

    public FriendsManager() {
        CompletableFuture.runAsync(() -> {
            try {
                if (MythicPrison.getInstance().getMongoManager() != null) {
                    this.friendsCollection = MythicPrison.getInstance().getMongoManager()
                            .getDatabase().getCollection("friends");
                    this.requestsCollection = MythicPrison.getInstance().getMongoManager()
                            .getDatabase().getCollection("friend_requests");
                    System.out.println("[FriendsManager] Connected to MongoDB collections");
                }
            } catch (Exception e) {
                System.err.println("[FriendsManager] Failed to initialize MongoDB: " + e.getMessage());
            }
        });
    }

    public void initializePlayer(Player player) {
        String uuid = player.getUuid().toString();
        loadFriendsFromDatabase(uuid).thenAccept(friends -> {
            friendsCache.put(uuid, friends != null ? friends : new ArrayList<>());
        });

        loadRequestsFromDatabase(uuid).thenAccept(requests -> {
            requestsCache.put(uuid, requests != null ? requests : new ArrayList<>());
        });
    }

    public void removePlayer(Player player) {
        String uuid = player.getUuid().toString();

        // Update online status for friends
        updatePlayerOnlineStatus(uuid, false, null);

        // Save and remove from cache
        saveFriendsToDatabase(uuid, friendsCache.remove(uuid));
        saveRequestsToDatabase(uuid, requestsCache.remove(uuid));
    }

    // Friend Request Methods
    public CompletableFuture<Boolean> sendFriendRequest(Player sender, String targetUsername) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get target player's profile to get UUID
                Player targetPlayer = findPlayerByUsername(targetUsername);
                String targetUuid;

                if (targetPlayer != null) {
                    targetUuid = targetPlayer.getUuid().toString();
                } else {
                    // Try to find offline player
                    targetUuid = findOfflinePlayerUuid(targetUsername);
                    if (targetUuid == null) {
                        sender.sendMessage(Component.text("§cPlayer not found!"));
                        return false;
                    }
                }

                String senderUuid = sender.getUuid().toString();

                // Check if already friends
                if (areFriends(senderUuid, targetUuid)) {
                    sender.sendMessage(Component.text("§cYou are already friends with " + targetUsername + "!"));
                    return false;
                }

                // Check if request already exists
                if (hasExistingRequest(senderUuid, targetUuid)) {
                    sender.sendMessage(Component.text("§cFriend request already sent to " + targetUsername + "!"));
                    return false;
                }

                // Create and store friend request
                FriendRequest request = new FriendRequest(senderUuid, sender.getUsername(), targetUuid, targetUsername);

                List<FriendRequest> senderRequests = requestsCache.computeIfAbsent(senderUuid, k -> new ArrayList<>());
                List<FriendRequest> targetRequests = requestsCache.computeIfAbsent(targetUuid, k -> new ArrayList<>());

                senderRequests.add(request);
                targetRequests.add(request);

                // Save to database
                saveRequestToDatabase(request);

                // Notify players
                sender.sendMessage(Component.text("§aFriend request sent to " + targetUsername + "!"));

                if (targetPlayer != null) {
                    Component acceptButton = Component.text("[ACCEPT]")
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.runCommand("/friend accept " + sender.getUsername()))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to accept friend request")));

                    Component denyButton = Component.text("[DENY]")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.runCommand("/friend deny " + sender.getUsername()))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to deny friend request")));

                    targetPlayer.sendMessage(Component.text("§e" + sender.getUsername() + " sent you a friend request! ")
                            .append(acceptButton).append(Component.text(" ")).append(denyButton));
                }

                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("§cError sending friend request: " + e.getMessage()));
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> acceptFriendRequest(Player player, String senderUsername) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String playerUuid = player.getUuid().toString();
                String senderUuid = findPlayerUuidByUsername(senderUsername);

                if (senderUuid == null) {
                    player.sendMessage(Component.text("§cPlayer not found!"));
                    return false;
                }

                // Find and remove the request
                FriendRequest request = findAndRemoveRequest(playerUuid, senderUuid);
                if (request == null) {
                    player.sendMessage(Component.text("§cNo pending friend request from " + senderUsername + "!"));
                    return false;
                }

                // Add each other as friends
                Friend playerFriend = new Friend(playerUuid, player.getUsername());
                Friend senderFriend = new Friend(senderUuid, senderUsername);

                List<Friend> playerFriends = friendsCache.computeIfAbsent(playerUuid, k -> new ArrayList<>());
                List<Friend> senderFriends = friendsCache.computeIfAbsent(senderUuid, k -> new ArrayList<>());

                playerFriends.add(senderFriend);
                senderFriends.add(playerFriend);

                // Save to database
                saveFriendsToDatabase(playerUuid, playerFriends);
                saveFriendsToDatabase(senderUuid, senderFriends);

                // Update request status and save
                request.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
                saveRequestToDatabase(request);

                // Notify both players
                player.sendMessage(Component.text("§aYou are now friends with " + senderUsername + "!"));

                Player senderPlayer = findPlayerByUsername(senderUsername);
                if (senderPlayer != null) {
                    senderPlayer.sendMessage(Component.text("§a" + player.getUsername() + " accepted your friend request!"));
                }

                return true;
            } catch (Exception e) {
                player.sendMessage(Component.text("§cError accepting friend request: " + e.getMessage()));
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> denyFriendRequest(Player player, String senderUsername) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String playerUuid = player.getUuid().toString();
                String senderUuid = findPlayerUuidByUsername(senderUsername);

                if (senderUuid == null) {
                    player.sendMessage(Component.text("§cPlayer not found!"));
                    return false;
                }

                FriendRequest request = findAndRemoveRequest(playerUuid, senderUuid);
                if (request == null) {
                    player.sendMessage(Component.text("§cNo pending friend request from " + senderUsername + "!"));
                    return false;
                }

                // Update request status
                request.setStatus(FriendRequest.FriendRequestStatus.DECLINED);
                saveRequestToDatabase(request);

                player.sendMessage(Component.text("§cDenied friend request from " + senderUsername + "."));

                Player senderPlayer = findPlayerByUsername(senderUsername);
                if (senderPlayer != null) {
                    senderPlayer.sendMessage(Component.text("§c" + player.getUsername() + " denied your friend request."));
                }

                return true;
            } catch (Exception e) {
                player.sendMessage(Component.text("§cError denying friend request: " + e.getMessage()));
                return false;
            }
        });
    }

    // Friend Management Methods
    public CompletableFuture<Boolean> removeFriend(Player player, String friendUsername) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String playerUuid = player.getUuid().toString();
                String friendUuid = findPlayerUuidByUsername(friendUsername);

                if (friendUuid == null) {
                    player.sendMessage(Component.text("§cPlayer not found!"));
                    return false;
                }

                // Remove from both friend lists
                List<Friend> playerFriends = friendsCache.get(playerUuid);
                List<Friend> friendFriends = friendsCache.get(friendUuid);

                if (playerFriends != null) {
                    playerFriends.removeIf(f -> f.getUuid().equals(friendUuid));
                    saveFriendsToDatabase(playerUuid, playerFriends);
                }

                if (friendFriends != null) {
                    friendFriends.removeIf(f -> f.getUuid().equals(playerUuid));
                    saveFriendsToDatabase(friendUuid, friendFriends);
                }

                player.sendMessage(Component.text("§cRemoved " + friendUsername + " from your friends list."));

                Player friendPlayer = findPlayerByUsername(friendUsername);
                if (friendPlayer != null) {
                    friendPlayer.sendMessage(Component.text("§c" + player.getUsername() + " removed you from their friends list."));
                }

                return true;
            } catch (Exception e) {
                player.sendMessage(Component.text("§cError removing friend: " + e.getMessage()));
                return false;
            }
        });
    }

    public void showFriendsList(Player player) {
        String uuid = player.getUuid().toString();
        List<Friend> friends = friendsCache.get(uuid);

        if (friends == null || friends.isEmpty()) {
            player.sendMessage(Component.text("§eYou have no friends yet. Use §b/friend add <username> §eto add friends!"));
            return;
        }

        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
        player.sendMessage(Component.text("§e§lYOUR FRIENDS §7(" + friends.size() + ")"));
        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));

        // Sort friends by online status, then favorites, then name
        friends.sort((a, b) -> {
            if (a.isOnline() != b.isOnline()) return Boolean.compare(b.isOnline(), a.isOnline());
            if (a.isFavorite() != b.isFavorite()) return Boolean.compare(b.isFavorite(), a.isFavorite());
            return a.getUsername().compareToIgnoreCase(b.getUsername());
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

        for (Friend friend : friends) {
            String status = friend.isOnline() ? "§a●" : "§7●";
            String favorite = friend.isFavorite() ? "§e★ " : "";
            String server = friend.isOnline() && friend.getCurrentServer() != null ?
                    " §7(" + friend.getCurrentServer() + ")" : "";

            Component friendComponent = Component.text(status + " " + favorite + "§f" + friend.getUsername() + server)
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "§7Friends since: §e" + dateFormat.format(new Date(friend.getFriendSince())) + "\n" +
                                    "§7Status: " + (friend.isOnline() ? "§aOnline" : "§7Offline") + "\n" +
                                    "§7Last seen: §e" + (friend.isOnline() ? "Now" : dateFormat.format(new Date(friend.getLastSeen()))) + "\n\n" +
                                    "§eClick to message")))
                    .clickEvent(ClickEvent.runCommand("/msg " + friend.getUsername() + " "));

            player.sendMessage(friendComponent);
        }

        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
    }

    public void showPendingRequests(Player player) {
        String uuid = player.getUuid().toString();
        List<FriendRequest> requests = requestsCache.get(uuid);

        if (requests == null) requests = new ArrayList<>();

        List<FriendRequest> incoming = requests.stream()
                .filter(r -> r.getToUuid().equals(uuid) && r.getStatus() == FriendRequest.FriendRequestStatus.PENDING)
                .collect(Collectors.toList());

        List<FriendRequest> outgoing = requests.stream()
                .filter(r -> r.getFromUuid().equals(uuid) && r.getStatus() == FriendRequest.FriendRequestStatus.PENDING)
                .collect(Collectors.toList());

        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
        player.sendMessage(Component.text("§e§lFRIEND REQUESTS"));
        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));

        if (!incoming.isEmpty()) {
            player.sendMessage(Component.text("§a§lIncoming Requests:"));
            for (FriendRequest request : incoming) {
                Component acceptButton = Component.text(" §a[ACCEPT]")
                        .clickEvent(ClickEvent.runCommand("/friend accept " + request.getFromUsername()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to accept")));

                Component denyButton = Component.text(" §c[DENY]")
                        .clickEvent(ClickEvent.runCommand("/friend deny " + request.getFromUsername()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to deny")));

                player.sendMessage(Component.text("§7• §f" + request.getFromUsername())
                        .append(acceptButton).append(denyButton));
            }
            player.sendMessage(Component.text(""));
        }

        if (!outgoing.isEmpty()) {
            player.sendMessage(Component.text("§e§lOutgoing Requests:"));
            for (FriendRequest request : outgoing) {
                player.sendMessage(Component.text("§7• §f" + request.getToUsername() + " §7(pending)"));
            }
        }

        if (incoming.isEmpty() && outgoing.isEmpty()) {
            player.sendMessage(Component.text("§7No pending friend requests."));
        }

        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
    }

    // Helper Methods
    private boolean areFriends(String uuid1, String uuid2) {
        List<Friend> friends = friendsCache.get(uuid1);
        return friends != null && friends.stream().anyMatch(f -> f.getUuid().equals(uuid2));
    }

    private boolean hasExistingRequest(String fromUuid, String toUuid) {
        List<FriendRequest> requests = requestsCache.get(fromUuid);
        return requests != null && requests.stream()
                .anyMatch(r -> r.getToUuid().equals(toUuid) && r.getStatus() == FriendRequest.FriendRequestStatus.PENDING);
    }

    private FriendRequest findAndRemoveRequest(String playerUuid, String senderUuid) {
        List<FriendRequest> requests = requestsCache.get(playerUuid);
        if (requests == null) return null;

        FriendRequest found = requests.stream()
                .filter(r -> r.getFromUuid().equals(senderUuid) && r.getToUuid().equals(playerUuid)
                        && r.getStatus() == FriendRequest.FriendRequestStatus.PENDING)
                .findFirst().orElse(null);

        if (found != null) {
            requests.remove(found);
            // Also remove from sender's cache
            List<FriendRequest> senderRequests = requestsCache.get(senderUuid);
            if (senderRequests != null) {
                senderRequests.remove(found);
            }
        }

        return found;
    }

    private Player findPlayerByUsername(String username) {
        return MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    private String findPlayerUuidByUsername(String username) {
        Player player = findPlayerByUsername(username);
        if (player != null) {
            return player.getUuid().toString();
        }

        // Try to find in offline profiles
        return findOfflinePlayerUuid(username);
    }

    private String findOfflinePlayerUuid(String username) {
        // This would query your player profiles database
        // For now, return null - implement based on your ProfileManager
        return null;
    }

    public void updatePlayerOnlineStatus(String uuid, boolean online, String server) {
        // Update in all friend lists that contain this player
        for (List<Friend> friendsList : friendsCache.values()) {
            for (Friend friend : friendsList) {
                if (friend.getUuid().equals(uuid)) {
                    friend.setOnline(online);
                    friend.setCurrentServer(server);
                    if (!online) {
                        friend.setLastSeen(System.currentTimeMillis());
                    }
                }
            }
        }
    }

    // Database Methods
    private CompletableFuture<List<Friend>> loadFriendsFromDatabase(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (friendsCollection == null) return new ArrayList<>();

                Document doc = friendsCollection.find(Filters.eq("uuid", uuid)).first();
                if (doc != null) {
                    List<Document> friendDocs = doc.getList("friends", Document.class);
                    if (friendDocs != null) {
                        return friendDocs.stream()
                                .map(this::documentToFriend)
                                .collect(Collectors.toList());
                    }
                }
            } catch (Exception e) {
                System.err.println("[FriendsManager] Error loading friends for " + uuid + ": " + e.getMessage());
            }
            return new ArrayList<>();
        });
    }

    private CompletableFuture<List<FriendRequest>> loadRequestsFromDatabase(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (requestsCollection == null) return new ArrayList<>();

                List<Document> docs = requestsCollection.find(
                        Filters.or(Filters.eq("fromUuid", uuid), Filters.eq("toUuid", uuid))
                ).into(new ArrayList<>());

                return docs.stream()
                        .map(this::documentToRequest)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("[FriendsManager] Error loading requests for " + uuid + ": " + e.getMessage());
            }
            return new ArrayList<>();
        });
    }

    private void saveFriendsToDatabase(String uuid, List<Friend> friends) {
        if (friendsCollection == null || friends == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                List<Document> friendDocs = friends.stream()
                        .map(this::friendToDocument)
                        .collect(Collectors.toList());

                Document doc = new Document("uuid", uuid).append("friends", friendDocs);

                friendsCollection.replaceOne(
                        Filters.eq("uuid", uuid),
                        doc,
                        new ReplaceOptions().upsert(true)
                );
            } catch (Exception e) {
                System.err.println("[FriendsManager] Error saving friends for " + uuid + ": " + e.getMessage());
            }
        });
    }

    private void saveRequestsToDatabase(String uuid, List<FriendRequest> requests) {
        if (requestsCollection == null || requests == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                for (FriendRequest request : requests) {
                    saveRequestToDatabase(request);
                }
            } catch (Exception e) {
                System.err.println("[FriendsManager] Error saving requests for " + uuid + ": " + e.getMessage());
            }
        });
    }

    private void saveRequestToDatabase(FriendRequest request) {
        if (requestsCollection == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Document doc = requestToDocument(request);
                requestsCollection.replaceOne(
                        Filters.and(
                                Filters.eq("fromUuid", request.getFromUuid()),
                                Filters.eq("toUuid", request.getToUuid())
                        ),
                        doc,
                        new ReplaceOptions().upsert(true)
                );
            } catch (Exception e) {
                System.err.println("[FriendsManager] Error saving request: " + e.getMessage());
            }
        });
    }

    // Document conversion methods
    private Friend documentToFriend(Document doc) {
        Friend friend = new Friend();
        friend.setUuid(doc.getString("uuid"));
        friend.setUsername(doc.getString("username"));
        friend.setFriendSince(doc.getLong("friendSince") != null ? doc.getLong("friendSince") : System.currentTimeMillis());
        friend.setOnline(doc.getBoolean("online") != null ? doc.getBoolean("online") : false);
        friend.setCurrentServer(doc.getString("currentServer"));
        friend.setLastSeen(doc.getLong("lastSeen") != null ? doc.getLong("lastSeen") : System.currentTimeMillis());
        friend.setFavorite(doc.getBoolean("favorite") != null ? doc.getBoolean("favorite") : false);
        return friend;
    }

    private Document friendToDocument(Friend friend) {
        return new Document("uuid", friend.getUuid())
                .append("username", friend.getUsername())
                .append("friendSince", friend.getFriendSince())
                .append("online", friend.isOnline())
                .append("currentServer", friend.getCurrentServer())
                .append("lastSeen", friend.getLastSeen())
                .append("favorite", friend.isFavorite());
    }

    private FriendRequest documentToRequest(Document doc) {
        FriendRequest request = new FriendRequest();
        request.setFromUuid(doc.getString("fromUuid"));
        request.setFromUsername(doc.getString("fromUsername"));
        request.setToUuid(doc.getString("toUuid"));
        request.setToUsername(doc.getString("toUsername"));
        request.setSentAt(doc.getLong("sentAt") != null ? doc.getLong("sentAt") : System.currentTimeMillis());
        request.setStatus(FriendRequest.FriendRequestStatus.valueOf(
                doc.getString("status") != null ? doc.getString("status") : "PENDING"));
        request.setMessage(doc.getString("message"));
        return request;
    }

    private Document requestToDocument(FriendRequest request) {
        return new Document("fromUuid", request.getFromUuid())
                .append("fromUsername", request.getFromUsername())
                .append("toUuid", request.getToUuid())
                .append("toUsername", request.getToUsername())
                .append("sentAt", request.getSentAt())
                .append("status", request.getStatus().name())
                .append("message", request.getMessage());
    }

    // Public getters for other managers
    public List<Friend> getFriends(String uuid) {
        return friendsCache.getOrDefault(uuid, new ArrayList<>());
    }

    public List<Friend> getOnlineFriends(String uuid) {
        return getFriends(uuid).stream()
                .filter(Friend::isOnline)
                .collect(Collectors.toList());
    }

    public int getFriendCount(String uuid) {
        return getFriends(uuid).size();
    }
}