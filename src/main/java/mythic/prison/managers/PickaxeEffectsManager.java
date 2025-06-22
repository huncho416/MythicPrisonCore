package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.enchants.PickaxeEnchant;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class PickaxeEffectsManager {

    private final Random random = new Random();

    public void applyMiningEffects(Player player, Block minedBlock, Point blockPos) {
        PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
        
        if (pickaxeManager == null) return;

        Instance instance = player.getInstance();

        // Check token enchants (excluding passive ones since they're handled separately)
        Map<String, PickaxeEnchant> tokenEnchants = pickaxeManager.getTokenEnchants();
        for (String enchantId : tokenEnchants.keySet()) {
            int level = pickaxeManager.getTokenEnchantLevel(player, enchantId);
            if (level > 0 && !isPassiveEnchant(enchantId)) {
                applyEnchantEffect(player, enchantId, level, minedBlock, blockPos, instance);
            }
        }

        // Check soul enchants (excluding passive ones since they're handled separately)
        Map<String, PickaxeEnchant> soulEnchants = pickaxeManager.getSoulEnchants();
        for (String enchantId : soulEnchants.keySet()) {
            int level = pickaxeManager.getSoulEnchantLevel(player, enchantId);
            if (level > 0 && !isPassiveEnchant(enchantId)) {
                applyEnchantEffect(player, enchantId, level, minedBlock, blockPos, instance);
            }
        }
    }

    /**
     * Apply passive effects when pickaxe is equipped/held
     * This method should be called whenever the player equips their pickaxe
     */
    public void applyPassiveEffects(Player player) {
        PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
        if (pickaxeManager == null) return;

        // Apply Speed effect from Speed enchant - 100% active when pickaxe is held
        int speedLevel = pickaxeManager.getTokenEnchantLevel(player, "speed");
        if (speedLevel > 0) {
            // Permanent effect while holding pickaxe (5 minutes duration, will be reapplied)
            Potion speedPotion = new Potion(PotionEffect.SPEED, (byte) speedLevel, 6000); // 5 minutes
            player.addEffect(speedPotion);
        }

        // Apply Haste effect from Haste enchant - 100% active when pickaxe is held
        int hasteLevel = pickaxeManager.getTokenEnchantLevel(player, "haste");
        if (hasteLevel > 0) {
            // Permanent effect while holding pickaxe (5 minutes duration, will be reapplied)
            Potion hastePotion = new Potion(PotionEffect.HASTE, (byte) hasteLevel, 6000); // 5 minutes
            player.addEffect(hastePotion);
        }

        // Apply Efficiency mining speed bonus - 100% active when pickaxe is held
        int efficiencyLevel = pickaxeManager.getTokenEnchantLevel(player, "efficiency");
        if (efficiencyLevel > 0) {
            // Efficiency gives both haste and speed effects - always active
            Potion hastePotion = new Potion(PotionEffect.HASTE, (byte) Math.min(efficiencyLevel, 5), 6000);
            Potion speedPotion = new Potion(PotionEffect.SPEED, (byte) Math.min(efficiencyLevel, 5), 6000);
            player.addEffect(hastePotion);
            player.addEffect(speedPotion);
        }
    }

    /**
     * Remove passive effects when pickaxe is unequipped
     */
    public void removePassiveEffects(Player player) {
        // Remove speed and haste effects when pickaxe is unequipped
        player.removeEffect(PotionEffect.SPEED);
        player.removeEffect(PotionEffect.HASTE);
    }

    /**
     * Check if an enchant is passive (always active when pickaxe is held)
     */
    private boolean isPassiveEnchant(String enchantId) {
        return switch (enchantId.toLowerCase()) {
            case "efficiency", "speed", "haste" -> true;
            default -> false;
        };
    }

    private void applyEnchantEffect(Player player, String enchantId, int level, Block minedBlock, Point blockPos, Instance instance) {
        switch (enchantId.toLowerCase()) {
            case "efficiency":
            case "speed":
            case "haste":
                // These are now handled by passive effects system
                break;

            case "fortune":
                applyFortuneEffect(player, level, minedBlock);
                break;

            case "explosive":
            case "explosion":
            case "mega_explosive":
                applyExplosionEffect(player, level, blockPos, instance, minedBlock);
                break;

            case "telepathy":
            case "magnet":
                applyTelepathyEffect(player, level, minedBlock);
                break;

            case "auto_smelt":
            case "smelting":
                applySmeltingEffect(player, level, minedBlock);
                break;

            case "experience":
                applyExperienceEffect(player, level);
                break;

            case "tokenator":
            case "auto_sell":
                applyTokenatorEffect(player, level, minedBlock);
                break;

            case "soul_extraction":
            case "soulextraction":
            case "super_fortune":
                applySoulExtractionEffect(player, level, minedBlock);
                break;

            case "void_walker":
            case "time_warp":
                applyVoidWalkerEffect(player, level, minedBlock);
                break;
        }
    }

    private void applyFortuneEffect(Player player, int level, Block minedBlock) {
        // Improved fortune chance - higher base chance and better scaling
        double fortuneChance = Math.min(0.25 + (level * 0.15), 0.95); // 25% base + 15% per level, max 95%

        if (random.nextDouble() <= fortuneChance) {
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            double baseMoney = getBlockValue(minedBlock);
            double bonusMoney = baseMoney * (1.0 + level * 0.5); // +50% per level

            if (bonusMoney > 0) {
                currencyManager.addBalance(player, "money", bonusMoney);
                player.sendMessage("§e⭐ Fortune activated! +$" + String.format("%.2f", bonusMoney));
            }
        }
    }

    private void applyExplosionEffect(Player player, int level, Point centerPos, Instance instance, Block minedBlock) {
        // Improved explosion chance
        double explosionChance = Math.min(0.10 + (level * 0.05), 0.35); // 10% base + 5% per level, max 35%

        if (random.nextDouble() <= explosionChance) {
            int radius = Math.min(level, 3);
            List<Point> blocksToBreak = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Point blockPos = centerPos.add(x, y, z);
                        Block block = instance.getBlock(blockPos);

                        if (isMineable(block)) {
                            blocksToBreak.add(blockPos);
                        }
                    }
                }
            }

            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            
            for (Point pos : blocksToBreak) {
                Block block = instance.getBlock(pos);
                instance.setBlock(pos, Block.AIR);

                double money = getBlockValue(block);
                if (money > 0) {
                    currencyManager.addBalance(player, "money", money);
                }
            }

            if (!blocksToBreak.isEmpty()) {
                player.sendMessage("§c💥 Explosion activated! Broke " + blocksToBreak.size() + " blocks!");
            }
        }
    }

    private void applyTelepathyEffect(Player player, int level, Block minedBlock) {
        // Improved telepathy chance
        double telepathyChance = Math.min(0.30 + (level * 0.15), 0.85); // 30% base + 15% per level, max 85%

        if (random.nextDouble() <= telepathyChance) {
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            double baseMoney = getBlockValue(minedBlock);
            double bonusMoney = baseMoney * (level * 0.15);

            if (bonusMoney > 0) {
                currencyManager.addBalance(player, "money", bonusMoney);
                player.sendMessage("§b✨ Telepathy activated! +$" + String.format("%.2f", bonusMoney));
            }
        }
    }

    private void applySmeltingEffect(Player player, int level, Block minedBlock) {
        // Improved smelting chance
        double smeltingChance = Math.min(0.40 + (level * 0.20), 0.95); // 40% base + 20% per level, max 95%

        if (random.nextDouble() <= smeltingChance) {
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            double baseMoney = getBlockValue(minedBlock);
            double bonusMoney = baseMoney * (1.0 + level * 0.3);

            if (bonusMoney > 0) {
                currencyManager.addBalance(player, "money", bonusMoney);
                player.sendMessage("§6🔥 Auto Smelt activated! +$" + String.format("%.2f", bonusMoney));
            }
        }
    }

    private void applyExperienceEffect(Player player, int level) {
        // Improved experience chance
        double expChance = Math.min(0.35 + (level * 0.15), 0.85); // 35% base + 15% per level, max 85%

        if (random.nextDouble() <= expChance) {
            PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
            if (pickaxeManager != null) {
                long bonusXP = level * 5;
                pickaxeManager.addPickaxeExp(player, bonusXP);
                player.sendMessage("§e⭐ Experience activated! +" + bonusXP + " XP!");
            }
        }
    }

    private void applyTokenatorEffect(Player player, int level, Block minedBlock) {
        // Improved tokenator chance
        double tokenChance = Math.min(0.15 + (level * 0.08), 0.55); // 15% base + 8% per level, max 55%

        if (random.nextDouble() <= tokenChance) {
            double tokens = level * 1.0;
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            currencyManager.addBalance(player, "tokens", tokens);
            player.sendMessage("§6✨ Tokenator activated! +" + tokens + " tokens!");
        }
    }

    private void applySoulExtractionEffect(Player player, int level, Block minedBlock) {
        // Improved soul extraction chance
        double soulChance = Math.min(0.08 + (level * 0.05), 0.35); // 8% base + 5% per level, max 35%

        if (random.nextDouble() <= soulChance) {
            double souls = level * 0.5;
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            currencyManager.addBalance(player, "souls", souls);
            player.sendMessage("§5👻 Soul Extraction activated! +" + souls + " souls!");
        }
    }

    private void applyVoidWalkerEffect(Player player, int level, Block minedBlock) {
        // Improved void walker chance
        double voidChance = Math.min(0.05 + (level * 0.03), 0.20); // 5% base + 3% per level, max 20%

        if (random.nextDouble() <= voidChance) {
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            double tokens = level * 2.0;
            double souls = level * 1.0;
            
            currencyManager.addBalance(player, "tokens", tokens);
            currencyManager.addBalance(player, "souls", souls);
            player.sendMessage("§5✨ Void Walker activated! +" + tokens + " tokens, +" + souls + " souls!");
        }
    }

    // Utility methods for getting enchant chances (for GUI display)
    public double getEnchantChance(String enchantId, int level) {
        return switch (enchantId.toLowerCase()) {
            case "efficiency", "speed", "haste" -> 100.0; // Always active when pickaxe is held
            case "fortune" -> Math.min(0.25 + (level * 0.15), 0.95) * 100;
            case "explosive", "explosion", "mega_explosive" -> Math.min(0.10 + (level * 0.05), 0.35) * 100;
            case "telepathy", "magnet" -> Math.min(0.30 + (level * 0.15), 0.85) * 100;
            case "auto_smelt", "smelting" -> Math.min(0.40 + (level * 0.20), 0.95) * 100;
            case "experience" -> Math.min(0.35 + (level * 0.15), 0.85) * 100;
            case "tokenator", "auto_sell" -> Math.min(0.15 + (level * 0.08), 0.55) * 100;
            case "soul_extraction", "soulextraction", "super_fortune" -> Math.min(0.08 + (level * 0.05), 0.35) * 100;
            case "void_walker", "time_warp" -> Math.min(0.05 + (level * 0.03), 0.20) * 100;
            default -> 0.0;
        };
    }

    private boolean isMineable(Block block) {
        return block != Block.AIR &&
                block != Block.BEDROCK &&
                block != Block.BARRIER &&
                !block.name().contains("LIQUID") &&
                !block.name().contains("WATER") &&
                !block.name().contains("LAVA");
    }

    private double getBlockValue(Block block) {
        return switch (block.name()) {
            case "STONE", "COBBLESTONE" -> 1.0;
            case "COAL_ORE", "DEEPSLATE_COAL_ORE" -> 2.0;
            case "IRON_ORE", "DEEPSLATE_IRON_ORE" -> 5.0;
            case "GOLD_ORE", "DEEPSLATE_GOLD_ORE" -> 10.0;
            case "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE" -> 25.0;
            case "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE" -> 50.0;
            case "NETHERITE_ORE" -> 100.0;
            default -> 0.5;
        };
    }

    public double getEfficiencySpeedMultiplier(int level) {
        return 1.0 + (level * 0.3);
    }
}