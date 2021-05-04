package argonms.common.loading.string;

import argonms.common.loading.item.KvjItemDataLoader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.NXNode;

public class NXStringDataLoader extends StringDataLoader {

    private static final Logger LOG = Logger.getLogger(KvjItemDataLoader.class.getName());
    private final String dataPath;

    protected NXStringDataLoader(String wzPath) {
        this.dataPath = wzPath;
    }

    @Override
    public boolean loadAll() {
        String dir = dataPath + "String.nx" + File.separatorChar;

        try {
            NXFile file = new EagerNXFile(dir);
            for (NXNode baseNode : file.resolve("Cash.img")) {
                int id = Integer.parseInt(baseNode.getName());
                NXNode itemName = baseNode.getChild("name");
                if (itemName != null)
                    itemNames.put(id, itemName.get().toString());
                NXNode itemDescription = baseNode.getChild("desc");
                if (itemDescription != null)
                    itemMsgs.put(id, itemDescription.get().toString());
            }

            for (NXNode baseNode : file.resolve("Eqp.img/Eqp")) {
                for (NXNode node : baseNode) {
                    int id = Integer.parseInt(node.getName());
                    NXNode itemName = node.getChild("name");
                    if (itemName != null)
                        itemNames.put(id, itemName.get().toString());
                }
            }

            for (NXNode baseNode : file.resolve("Map.img")) {
                for (NXNode type : baseNode) {
                    int id = Integer.parseInt(type.getName());
                    NXNode mapName = type.getChild("mapName");
                    if (mapName != null)
                        mapNames.put(id, mapName.get().toString());
                    NXNode streetName = type.getChild("streetName");
                    if (streetName != null)
                        streetNames.put(id, streetName.get().toString());
                }
            }

            for (NXNode baseNode : file.resolve("Mob.img")) {
                int id = Integer.parseInt(baseNode.getName());
                NXNode mobName = baseNode.getChild("name");
                if (mobName != null)
                    mobNames.put(id, mobName.get().toString());
            }

            for (NXNode baseNode : file.resolve("Npc.img")) {
                int id = Integer.parseInt(baseNode.getName());
                NXNode npcName = baseNode.getChild("name");
                if (npcName != null)
                    npcNames.put(id, npcName.get().toString());
            }

            for (NXNode baseNode : file.resolve("Skill.img")) {
                int id = Integer.parseInt(baseNode.getName());
                NXNode skillName = baseNode.getChild("name");
                if (skillName != null)
                    skillNames.put(id, skillName.get().toString());
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not load all string data from NX files.", e);
            return false;
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Could not parse all string data from NX files.", e);
            return false;
        }

        return true;
    }

}
