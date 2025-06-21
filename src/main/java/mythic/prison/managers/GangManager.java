package mythic.prison.managers;

import mythic.prison.data.gangs.Gang;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.entity.Player;
import net.minestom.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GangManager {

    private final Map<String, Gang> gangs = new ConcurrentHashMap<>();
    private final Map<String, String> playerGangs = new ConcurrentHashMap<>(); // PlayerUUID -> GangID
    private final Map<String, String> pendingInvites = new ConcurrentHashMap<>(); // PlayerUUID -> GangID

    public void initializePlayer(Player player) {
        // Player initialization if needed
    }

    public void initializePlayer(Object player) {
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public List<Gang> getTopGangs(int limit) {
        return gangs.values().stream()
                .sorted((g1, g2) -> {
                    // Sort by level first, then by member count, then by experience
                    int levelCompare = Integer.compare(g2.getLevel(), g1.getLevel());
                    if (levelCompare != 0) return levelCompare;
                    
                    int memberCompare = Integer.compare(g2.getMemberCount(), g1.getMemberCount());
                    if (memberCompare != 0) return memberCompare;
                    
                    return Long.compare(g2.getExperience(), g1.getExperience());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void sendGangChat(Player sender, String message) {
        Gang gang = getPlayerGang(sender);
        if (gang == null) {
            ChatUtil.sendError(sender, "You are not in a gang!");
            return;
        }

        // Format the message
        Gang.GangRole senderRole = gang.getMemberRole(sender.getUuid().toString());
        String roleColor = senderRole != null ? senderRole.getColor() : "§7";
        String formattedMessage = "§d[Gang] " + roleColor + sender.getUsername() + " §f» §7" + message;

        // Send to all gang members
        for (String memberUUID : gang.getMembers()) {
            Player member = getPlayerByUUID(memberUUID);
            if (member != null) {
                member.sendMessage(formattedMessage);
            }
        }
    }

    public void kickPlayer(Player kicker, String targetName) {
        Gang gang = getPlayerGang(kicker);
        if (gang == null) {
            ChatUtil.sendError(kicker, "You are not in a gang!");
            return;
        }

        // Check if kicker has permission to kick
        Gang.GangRole kickerRole = gang.getMemberRole(kicker.getUuid().toString());
        if (kickerRole != Gang.GangRole.LEADER && kickerRole != Gang.GangRole.MODERATOR) {
            ChatUtil.sendError(kicker, "You don't have permission to kick players!");
            return;
        }

        // Find target player by name
        Player target = getPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(kicker, "Player '" + targetName + "' not found!");
            return;
        }

        String targetUUID = target.getUuid().toString();

        // Check if target is in the same gang
        if (!gang.isMember(targetUUID)) {
            ChatUtil.sendError(kicker, targetName + " is not in your gang!");
            return;
        }

        // Check if target is the owner
        if (gang.getOwnerUUID().equals(targetUUID)) {
            ChatUtil.sendError(kicker, "You cannot kick the gang owner!");
            return;
        }

        // Check role hierarchy - moderators can't kick other moderators or leaders
        Gang.GangRole targetRole = gang.getMemberRole(targetUUID);
        if (kickerRole == Gang.GangRole.MODERATOR && targetRole != Gang.GangRole.MEMBER) {
            ChatUtil.sendError(kicker, "You can only kick regular members!");
            return;
        }

        // Remove player from gang
        gang.removeMember(targetUUID);
        playerGangs.remove(targetUUID);

        // Notify kicker
        ChatUtil.sendSuccess(kicker, "Successfully kicked " + targetName + " from the gang!");

        // Notify target
        ChatUtil.sendError(target, "You have been kicked from " + gang.getName() + " by " + kicker.getUsername() + "!");

        // Notify other gang members
        notifyGangMembers(gang, targetName + " was kicked from the gang by " + kicker.getUsername());
    }

    public void promotePlayer(Player promoter, String targetName) {
        Gang gang = getPlayerGang(promoter);
        if (gang == null) {
            ChatUtil.sendError(promoter, "You are not in a gang!");
            return;
        }

        // Only gang leader can promote
        if (!gang.getOwnerUUID().equals(promoter.getUuid().toString())) {
            ChatUtil.sendError(promoter, "Only the gang leader can promote players!");
            return;
        }

        Player target = getPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(promoter, "Player '" + targetName + "' not found!");
            return;
        }

        String targetUUID = target.getUuid().toString();

        if (!gang.isMember(targetUUID)) {
            ChatUtil.sendError(promoter, targetName + " is not in your gang!");
            return;
        }

        Gang.GangRole currentRole = gang.getMemberRole(targetUUID);
        Gang.GangRole newRole = switch (currentRole) {
            case MEMBER -> Gang.GangRole.MODERATOR;
            case MODERATOR -> {
                ChatUtil.sendError(promoter, targetName + " is already at the highest promotable rank!");
                yield null;
            }
            case LEADER -> {
                ChatUtil.sendError(promoter, "You cannot promote another leader!");
                yield null;
            }
        };

        if (newRole != null) {
            gang.addMember(targetUUID, newRole); // This updates the role
            
            ChatUtil.sendSuccess(promoter, "Promoted " + targetName + " to " + formatRole(newRole) + "!");
            ChatUtil.sendSuccess(target, "You have been promoted to " + formatRole(newRole) + " in " + gang.getName() + "!");
            
            notifyGangMembers(gang, targetName + " was promoted to " + formatRole(newRole) + "!");
        }
    }

    public void demotePlayer(Player demoter, String targetName) {
        Gang gang = getPlayerGang(demoter);
        if (gang == null) {
            ChatUtil.sendError(demoter, "You are not in a gang!");
            return;
        }

        // Only gang leader can demote
        if (!gang.getOwnerUUID().equals(demoter.getUuid().toString())) {
            ChatUtil.sendError(demoter, "Only the gang leader can demote players!");
            return;
        }

        Player target = getPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(demoter, "Player '" + targetName + "' not found!");
            return;
        }

        String targetUUID = target.getUuid().toString();

        if (!gang.isMember(targetUUID)) {
            ChatUtil.sendError(demoter, targetName + " is not in your gang!");
            return;
        }

        if (targetUUID.equals(demoter.getUuid().toString())) {
            ChatUtil.sendError(demoter, "You cannot demote yourself!");
            return;
        }

        Gang.GangRole currentRole = gang.getMemberRole(targetUUID);
        Gang.GangRole newRole = switch (currentRole) {
            case MODERATOR -> Gang.GangRole.MEMBER;
            case MEMBER -> {
                ChatUtil.sendError(demoter, targetName + " is already at the lowest rank!");
                yield null;
            }
            case LEADER -> {
                ChatUtil.sendError(demoter, "You cannot demote another leader!");
                yield null;
            }
        };

        if (newRole != null) {
            gang.addMember(targetUUID, newRole); // This updates the role
            
            ChatUtil.sendSuccess(demoter, "Demoted " + targetName + " to " + formatRole(newRole) + "!");
            ChatUtil.sendMessage(target, "You have been demoted to " + formatRole(newRole) + " in " + gang.getName() + "!");
            
            notifyGangMembers(gang, targetName + " was demoted to " + formatRole(newRole) + "!");
        }
    }

    private Player getPlayerByName(String playerName) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equalsIgnoreCase(playerName)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    public void invitePlayer(Player inviter, Object targetObj) {
        if (!(targetObj instanceof Player target)) {
            ChatUtil.sendError(inviter, "Player not found!");
            return;
        }

        Gang gang = getPlayerGang(inviter);
        if (gang == null) {
            ChatUtil.sendError(inviter, "You are not in a gang!");
            return;
        }

        // Check if inviter has permission to invite
        Gang.GangRole inviterRole = gang.getMemberRole(inviter.getUuid().toString());
        if (inviterRole != Gang.GangRole.LEADER && inviterRole != Gang.GangRole.MODERATOR) {
            ChatUtil.sendError(inviter, "You don't have permission to invite players!");
            return;
        }

        // Check if target is already in a gang
        if (isInGang(target)) {
            ChatUtil.sendError(inviter, target.getUsername() + " is already in a gang!");
            return;
        }

        String targetUUID = target.getUuid().toString();
        String gangId = gang.getId();

        // Check if already has pending invite
        if (pendingInvites.containsKey(targetUUID)) {
            ChatUtil.sendError(inviter, target.getUsername() + " already has a pending gang invitation!");
            return;
        }

        // Check if gang is full
        if (gang.getMemberCount() >= gang.getMaxMembers()) {
            ChatUtil.sendError(inviter, "Your gang is full! (Max: " + gang.getMaxMembers() + " members)");
            return;
        }

        // Send invite
        pendingInvites.put(targetUUID, gangId);

        // Notify inviter
        ChatUtil.sendSuccess(inviter, "Gang invitation sent to " + target.getUsername() + "!");

        // Notify target
        ChatUtil.sendMessage(target, "§d§l§m                                        ");
        ChatUtil.sendMessage(target, "§d§lGANG INVITATION");
        ChatUtil.sendMessage(target, "");
        ChatUtil.sendMessage(target, "§f" + inviter.getUsername() + " §7has invited you to join");
        ChatUtil.sendMessage(target, "§d§l" + gang.getName());
        ChatUtil.sendMessage(target, "");
        ChatUtil.sendMessage(target, "§a/gang accept " + gang.getName() + " §7- Accept invitation");
        ChatUtil.sendMessage(target, "§c/gang deny " + gang.getName() + " §7- Deny invitation");
        ChatUtil.sendMessage(target, "");
        ChatUtil.sendMessage(target, "§7This invitation will expire in 5 minutes.");
        ChatUtil.sendMessage(target, "§d§l§m                                        ");

        // Schedule invite expiration (you might want to implement this with a scheduler)
        scheduleInviteExpiration(targetUUID, gangId, 300000); // 5 minutes
    }

    public boolean acceptInvite(Player player, String gangName) {
        String playerUUID = player.getUuid().toString();
        String gangId = pendingInvites.get(playerUUID);

        if (gangId == null) {
            ChatUtil.sendError(player, "You don't have any pending gang invitations!");
            return false;
        }

        Gang gang = gangs.get(gangId);
        if (gang == null || !gang.getName().equalsIgnoreCase(gangName)) {
            ChatUtil.sendError(player, "Invalid gang invitation!");
            pendingInvites.remove(playerUUID);
            return false;
        }

        // Check if gang is still not full
        if (gang.getMemberCount() >= gang.getMaxMembers()) {
            ChatUtil.sendError(player, "The gang is now full!");
            pendingInvites.remove(playerUUID);
            return false;
        }

        // Join gang
        gang.addMember(playerUUID, Gang.GangRole.MEMBER);
        playerGangs.put(playerUUID, gangId);
        pendingInvites.remove(playerUUID);

        // Notify player
        ChatUtil.sendSuccess(player, "You have joined " + gang.getName() + "!");

        // Notify gang members
        notifyGangMembers(gang, player.getUsername() + " has joined the gang!");

        return true;
    }

    public boolean denyInvite(Player player, String gangName) {
        String playerUUID = player.getUuid().toString();
        String gangId = pendingInvites.get(playerUUID);

        if (gangId == null) {
            ChatUtil.sendError(player, "You don't have any pending gang invitations!");
            return false;
        }

        Gang gang = gangs.get(gangId);
        if (gang == null || !gang.getName().equalsIgnoreCase(gangName)) {
            ChatUtil.sendError(player, "Invalid gang invitation!");
            pendingInvites.remove(playerUUID);
            return false;
        }

        pendingInvites.remove(playerUUID);
        ChatUtil.sendMessage(player, "§7You denied the invitation to join " + gang.getName());

        // Notify gang owner/moderators
        String ownerUUID = gang.getOwnerUUID();
        Player owner = getPlayerByUUID(ownerUUID);
        if (owner != null) {
            ChatUtil.sendMessage(owner, "§7" + player.getUsername() + " denied the gang invitation.");
        }

        return true;
    }

    private void scheduleInviteExpiration(String playerUUID, String gangId, long delayMs) {
        // In a real implementation, you'd use a proper scheduler
        // For now, this is a placeholder
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                if (pendingInvites.containsKey(playerUUID) && 
                    pendingInvites.get(playerUUID).equals(gangId)) {
                    pendingInvites.remove(playerUUID);
                    
                    Player player = getPlayerByUUID(playerUUID);
                    if (player != null) {
                        ChatUtil.sendError(player, "Gang invitation expired!");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void notifyGangMembers(Gang gang, String message) {
        for (String memberUUID : gang.getMembers()) {
            Player member = getPlayerByUUID(memberUUID);
            if (member != null) {
                ChatUtil.sendMessage(member, "§d[Gang] §7" + message);
            }
        }
    }

    private Player getPlayerByUUID(String playerUUID) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUuid().toString().equals(playerUUID)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    public void showGangInfo(Player player, String gangName) {
        Gang gang = getPlayerGang(player);
        if (gang == null) {
            ChatUtil.sendError(player, "You are not in a gang!");
            return;
        }

        ChatUtil.sendMessage(player, "§d§l§m              §r §d§lGANG INFO §r§d§l§m              ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lGang Name: §d" + gang.getName());
        ChatUtil.sendMessage(player, "§f§lOwner: §a" + getPlayerName(gang.getOwnerUUID()));
        ChatUtil.sendMessage(player, "§f§lMembers: §e" + gang.getMemberCount() + "§7/§e" + gang.getMaxMembers());
        ChatUtil.sendMessage(player, "§f§lLevel: §a" + gang.getLevel());
        ChatUtil.sendMessage(player, "§f§lBank: §a$" + ChatUtil.formatMoney(gang.getBank()));
        ChatUtil.sendMessage(player, "§f§lTotal Value: §a$" + ChatUtil.formatMoney(getTotalValue(gang)));
        ChatUtil.sendMessage(player, "");
        
        // Show gang role
        Gang.GangRole playerRole = gang.getMemberRole(player.getUuid().toString());
        ChatUtil.sendMessage(player, "§f§lYour Role: §b" + formatRole(playerRole));
        ChatUtil.sendMessage(player, "");

        // Show members
        ChatUtil.sendMessage(player, "§f§lMembers:");
        for (String memberUUID : gang.getMembers()) {
            String memberName = getPlayerName(memberUUID);
            Gang.GangRole role = gang.getMemberRole(memberUUID);
            String status = isPlayerOnline(memberUUID) ? "§aOnline" : "§7Offline";
            
            ChatUtil.sendMessage(player, "  §7• §f" + memberName + " §7(" + formatRole(role) + ") " + status);
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCommands:");
        
        if (gang.getOwnerUUID().equals(player.getUuid().toString())) {
            ChatUtil.sendMessage(player, "§d/gang invite <player> §7- Invite a player");
            ChatUtil.sendMessage(player, "§d/gang kick <player> §7- Kick a member");
            ChatUtil.sendMessage(player, "§d/gang promote <player> §7- Promote a member");
            ChatUtil.sendMessage(player, "§d/gang demote <player> §7- Demote a member");
            ChatUtil.sendMessage(player, "§d/gang disband §7- Disband the gang");
        }
        
        ChatUtil.sendMessage(player, "§d/gang deposit <amount> §7- Deposit money");
        ChatUtil.sendMessage(player, "§d/gang withdraw <amount> §7- Withdraw money");
        ChatUtil.sendMessage(player, "§d/gang leave §7- Leave the gang");
        ChatUtil.sendMessage(player, "§d/gang chat <message> §7- Gang chat");
        
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                ");
    }

    private String formatRole(Gang.GangRole role) {
        if (role == null) return "§7Member";
        
        return switch (role) {
            case LEADER -> "§cLeader";
            case MODERATOR -> "§eModerator";
            case MEMBER -> "§7Member";
        };
    }

    private double getTotalValue(Gang gang) {
        // Calculate total gang value (bank + member stats, etc.)
        return gang.getBank(); // For now, just return bank balance
    }

    private String getPlayerName(String playerUUID) {
        // Try to get online player first
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUuid().toString().equals(playerUUID)) {
                return onlinePlayer.getUsername();
            }
        }
        
        // If not online, return shortened UUID or stored name
        // In a real implementation, you'd want to store player names
        return "Player-" + playerUUID.substring(0, 8);
    }

    private boolean isPlayerOnline(String playerUUID) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUuid().toString().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public Gang createGang(Player player, String gangName) {
        String playerUUID = player.getUuid().toString();
        
        if (isInGang(player)) {
            return null; // Player already in a gang
        }

        String gangId = UUID.randomUUID().toString();
        Gang gang = new Gang(gangId, gangName, playerUUID);
        
        gangs.put(gangId, gang);
        playerGangs.put(playerUUID, gangId);
        
        return gang;
    }

    public Gang getPlayerGang(Player player) {
        String playerUUID = player.getUuid().toString();
        String gangId = playerGangs.get(playerUUID);
        return gangId != null ? gangs.get(gangId) : null;
    }

    public Gang getPlayerGang(Object player) {
        if (player instanceof Player p) {
            return getPlayerGang(p);
        }
        return null;
    }

    public boolean isInGang(Player player) {
        return getPlayerGang(player) != null;
    }

    public boolean isInGang(Object player) {
        return getPlayerGang(player) != null;
    }

    public boolean joinGang(Player player, String gangId) {
        if (isInGang(player)) {
            return false;
        }

        Gang gang = gangs.get(gangId);
        if (gang == null) {
            return false;
        }

        String playerUUID = player.getUuid().toString();
        gang.addMember(playerUUID, Gang.GangRole.MEMBER);
        playerGangs.put(playerUUID, gangId);
        
        return true;
    }

    public boolean leaveGang(Player player) {
        Gang gang = getPlayerGang(player);
        if (gang == null) {
            return false;
        }

        String playerUUID = player.getUuid().toString();
        gang.removeMember(playerUUID);
        playerGangs.remove(playerUUID);
        
        // If gang is empty, remove it
        if (gang.getMemberCount() == 0) {
            gangs.remove(gang.getId());
        }
        
        return true;
    }

    public Gang getGangById(String gangId) {
        return gangs.get(gangId);
    }

    public Collection<Gang> getAllGangs() {
        return gangs.values();
    }

    public boolean disbandGang(Player player) {
        Gang gang = getPlayerGang(player);
        if (gang == null || !gang.getOwnerUUID().equals(player.getUuid().toString())) {
            return false;
        }

        // Remove all members from player-gang mapping
        for (String memberUUID : gang.getMembers()) {
            playerGangs.remove(memberUUID);
        }

        // Remove gang
        gangs.remove(gang.getId());
        return true;
    }
}