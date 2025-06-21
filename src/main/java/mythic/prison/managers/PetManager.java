package mythic.prison.managers;

import mythic.prison.data.pets.Pet;
import mythic.prison.data.pets.PetType;
import net.minestom.server.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PetManager {
    
    private final Map<String, List<Pet>> playerPets = new ConcurrentHashMap<>();
    private final Map<String, Pet> activePets = new ConcurrentHashMap<>();
    private final List<PetType> availablePetTypes = new ArrayList<>();

    public PetManager() {
        initializePetTypes();
    }

    private void initializePetTypes() {
        // Common pets
        availablePetTypes.add(new PetType("Dog", "🐕", PetType.PetRarity.COMMON, 1.05, 1.15, 50, PetType.BoostType.MONEY));
        availablePetTypes.add(new PetType("Cat", "🐱", PetType.PetRarity.COMMON, 1.03, 1.12, 50, PetType.BoostType.TOKENS));
        
        // Uncommon pets
        availablePetTypes.add(new PetType("Wolf", "🐺", PetType.PetRarity.UNCOMMON, 1.08, 1.25, 75, PetType.BoostType.MONEY));
        availablePetTypes.add(new PetType("Fox", "🦊", PetType.PetRarity.UNCOMMON, 1.06, 1.20, 75, PetType.BoostType.EXP));
        
        // Rare pets
        availablePetTypes.add(new PetType("Lion", "🦁", PetType.PetRarity.RARE, 1.12, 1.35, 100, PetType.BoostType.MONEY));
        availablePetTypes.add(new PetType("Tiger", "🐅", PetType.PetRarity.RARE, 1.10, 1.30, 100, PetType.BoostType.TOKENS));
        
        // Epic pets
        availablePetTypes.add(new PetType("Dragon", "🐉", PetType.PetRarity.EPIC, 1.20, 1.50, 150, PetType.BoostType.MONEY));
        availablePetTypes.add(new PetType("Phoenix", "🔥", PetType.PetRarity.EPIC, 1.18, 1.45, 150, PetType.BoostType.LUCK));
        
        // Legendary pets
        availablePetTypes.add(new PetType("Unicorn", "🦄", PetType.PetRarity.LEGENDARY, 1.25, 1.75, 200, PetType.BoostType.MONEY));
        availablePetTypes.add(new PetType("Pegasus", "🐴", PetType.PetRarity.LEGENDARY, 1.22, 1.70, 200, PetType.BoostType.EXP));
        
        // Mythic pets
        availablePetTypes.add(new PetType("Kraken", "🐙", PetType.PetRarity.MYTHIC, 1.50, 2.00, 300, PetType.BoostType.MONEY));
        availablePetTypes.add(new PetType("Leviathan", "🐋", PetType.PetRarity.MYTHIC, 1.45, 1.95, 300, PetType.BoostType.LUCK));
    }

    public void initializePlayer(Player player) {
        String uuid = player.getUuid().toString();
        playerPets.putIfAbsent(uuid, new ArrayList<>());
    }

    public void initializePlayer(Object player) {
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public List<Pet> getPlayerPets(Player player) {
        String uuid = player.getUuid().toString();
        return playerPets.getOrDefault(uuid, new ArrayList<>());
    }

    public Pet getActivePet(Player player) {
        String uuid = player.getUuid().toString();
        return activePets.get(uuid);
    }

    public void setActivePet(Player player, Pet pet) {
        String uuid = player.getUuid().toString();
        if (pet == null) {
            activePets.remove(uuid);
        } else {
            activePets.put(uuid, pet);
        }
    }

    public boolean levelUpPet(Player player, Pet pet) {
        if (pet.canLevelUp()) {
            pet.levelUp();
            return true;
        }
        return false;
    }

    public boolean fusePets(Player player, Pet pet1, Pet pet2) {
        // Basic fusion logic - you can expand this
        if (pet1.getPetType().getRarity() == pet2.getPetType().getRarity()) {
            List<Pet> pets = getPlayerPets(player);
            pets.remove(pet1);
            pets.remove(pet2);
            
            // Create a new pet with higher level or different type
            PetType newType = getRandomPetType(pet1.getPetType().getRarity());
            Pet newPet = new Pet(newType, player.getUuid().toString());
            newPet.setLevel(Math.max(pet1.getLevel(), pet2.getLevel()) + 1);
            
            pets.add(newPet);
            return true;
        }
        return false;
    }

    private PetType getRandomPetType(PetType.PetRarity rarity) {
        List<PetType> typesOfRarity = availablePetTypes.stream()
                .filter(type -> type.getRarity() == rarity)
                .toList();
        
        if (typesOfRarity.isEmpty()) {
            return availablePetTypes.get(0);
        }
        
        Random random = new Random();
        return typesOfRarity.get(random.nextInt(typesOfRarity.size()));
    }

    public List<PetType> getAvailablePetTypes() {
        return new ArrayList<>(availablePetTypes);
    }

    public void addPet(Player player, Pet pet) {
        String uuid = player.getUuid().toString();
        List<Pet> pets = playerPets.computeIfAbsent(uuid, k -> new ArrayList<>());
        pets.add(pet);
    }

    public void addExperienceToActivePet(Player player, int amount) {
        Pet activePet = getActivePet(player);
        if (activePet != null) {
            activePet.addExperience(amount);
        
            // Check if pet can level up after gaining experience
            if (activePet.canLevelUp()) {
                // Optionally auto-level up the pet or notify the player
                // You can customize this behavior based on your game design
            }
        }
    }
}