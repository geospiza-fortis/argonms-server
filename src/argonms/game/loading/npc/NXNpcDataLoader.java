package argonms.game.loading.npc;

import argonms.common.character.inventory.InventoryTools;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.NXNode;

public class NXNpcDataLoader extends NpcDataLoader {

    private static final Logger LOG = Logger.getLogger(NXNpcDataLoader.class.getName());
    private final String dataPath;

    protected NXNpcDataLoader(String wzPath) {
        this.dataPath = wzPath;
    }

    @Override
    public boolean loadAll() {
        String dir = dataPath + "Npc.nx" + File.separatorChar;

        try {
            NXFile file = new EagerNXFile(dir);
            for (NXNode baseNode : file.getRoot()) {
                int npcId = Integer.parseInt(baseNode.getName().replace(".img", ""));
                doWork(npcId, baseNode);
                loaded.add(npcId);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not load all npc data from NX files.", e);
            return false;
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Could not parse all npc data from NX files.", e);
            return false;
        }
        return true;
    }

    @Override
    protected void load(int npcId) {
        NXNode node = getNode(npcId);
        if (node != null) {
            doWork(npcId, node);
            loaded.add(npcId);
        }
    }

    private NXNode getNode(int npcId) {
        String id = String.format("%08d", npcId);

        try {
            NXFile file = new EagerNXFile(dataPath + "Npc.nx" + File.separatorChar);
            return file.resolve(id + ".img" + File.separatorChar);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not load NX data for npc: " + npcId, e);
            e.printStackTrace();
        }

        return null;
    }

    private void doWork(int npcId, NXNode node) throws NumberFormatException {
        NXNode infoNode = node.getChild("info");
        if (infoNode != null) {
            int withdrawCost = 0, depositCost = 0;
            for (NXNode propertyNode: infoNode) {
                switch (propertyNode.getName()) {
                    case "script":
                        for (NXNode scriptNode : propertyNode) {
                            for (NXNode valueNode : scriptNode) {
                                String scriptName = valueNode.get().toString();
                                scriptNames.put(npcId, scriptName);
                            }
                        }
                        break;
                    case "trunkPut":
                        withdrawCost = Integer.parseInt(propertyNode.get().toString());
                        break;
                    case "trunkGet":
                        depositCost = Integer.parseInt(propertyNode.get().toString());
                        break;
                }
            }

            if (withdrawCost != 0 || depositCost != 0) {
                storageCosts.put(npcId, new NpcStorageKeeper(depositCost, withdrawCost));
            }
        }
    }
}
