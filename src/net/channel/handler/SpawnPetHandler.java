package net.channel.handler;

import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleStat;
import database.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.AbstractMaplePacketHandler;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetHandler extends AbstractMaplePacketHandler {
    private boolean multipetEnabled = false;

    /*	TODO:
     *	1.  Move the equpping into a function.
     */
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        byte slot = slea.readByte();
        // Handle dragons
        if (c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId() == 5000028) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        // New instance of MaplePet - using the item ID and unique pet ID
        MaplePet pet = MaplePet.loadFromDb(c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId(), slot, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getPetId());
        // Assign the pet to the player, set stats
        if (c.getPlayer().getPetIndex(pet) != -1) {
            unequipPet(c, pet, true);
        } else {
            boolean replace = true;
            if (c.getPlayer().getNoPets() == 3 || (!multipetEnabled && c.getPlayer().getNoPets() > 0)) {
				//MaplePet pet_ = c.getPlayer().getPet(0);
                //unequipPet(c, pet_, false);
                replace = true;
            }
            c.getPlayer().addPet(pet, c.getPlayer().getNextEmptyPetIndex());
            if (multipetEnabled) {
                replace = false;
            }
            // Broadcast packet to the map...
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), pet, false, true), true);
            // Find the pet's unique ID
            int uniqueid = pet.getUniqueId();
            // Make a new List for the stat update
            List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
            stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(uniqueid)));
            // Write the stat update to the player...
            c.getSession().write(MaplePacketCreator.updatePlayerStats(stats, false, true, c.getPlayer().getNoPets()));
            c.getSession().write(MaplePacketCreator.enableActions());
            // Get the data
            MapleData petData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Data.wz" + "/Item")).getData("Pet/" + String.valueOf(pet.getItemId()) + ".img");
            MapleData hungerData = petData.getChildByPath("info/hungry");
            // Start the fullness schedule
            c.getPlayer().startFullnessSchedule(MapleDataTool.getInt(hungerData), pet, c.getPlayer().getPetIndex(pet));
        }
    }

    public static void unequipAllPets(MapleClient c) {
        List<MaplePet> pets = c.getPlayer().getPets();
        while (pets.size() > 0) {
            unequipPet(c, pets.get(0), true);
        }
    }

    public static void unequipPet(MapleClient c, MaplePet pet, boolean shift_left) {
        try {
            // Execute statement
            c.getPlayer().cancelFullnessSchedule(c.getPlayer().getPetIndex(pet));
            // Save pet data to the database
            Connection con = DatabaseConnection.getConnection(); // Get a connection to the database
            PreparedStatement ps = con.prepareStatement("UPDATE pets SET " + "name = ?, level = ?, " + "closeness = ?, fullness = ? " + "WHERE petid = ?"); // Prepare statement...
            ps.setString(1, pet.getName()); // Set name
            ps.setInt(2, pet.getLevel()); // Set Level
            ps.setInt(3, pet.getCloseness()); // Set Closeness
            ps.setInt(4, pet.getFullness()); // Set Fullness
            ps.setInt(5, pet.getUniqueId()); // Set ID
            ps.executeUpdate(); // Execute statement
            ps.close();
            // Broadcast the packet to the map - with null instead of MaplePet
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), pet, true, true), true);
            // Make a new list for the stat updates
            List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
            stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
            // Write the stat update to the player...
            c.getSession().write(MaplePacketCreator.updatePlayerStats(stats, false, true, c.getPlayer().getPetIndex(pet)));
            c.getSession().write(MaplePacketCreator.enableActions());
            // Un-assign the pet set to the player
            c.getPlayer().removePet(pet, shift_left);
        } catch (SQLException ex) {
            Logger.getLogger(SpawnPetHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
