package it.loneliness.mc.mobraid.Custom;

import org.bukkit.entity.EntityType;

public class RaidRoundEntityConfig {
        EntityType type;
        String name;
        int howMany;
        boolean onePerPlayer;

        public RaidRoundEntityConfig(EntityType type, String name, int howMany, boolean onePerPlayer){
            this.type = type;
            this.name = name;
            this.howMany = howMany;
            this.onePerPlayer = onePerPlayer;
        }

        public EntityType getType(){
            return type;
        }

        public String getName(){
            return name;
        }

        public int getHowMany(){
            return howMany;
        }

        public boolean isOnePerPlayer(){
            return onePerPlayer;
        }
}
