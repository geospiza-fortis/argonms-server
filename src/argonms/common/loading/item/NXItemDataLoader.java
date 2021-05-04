package argonms.common.loading.item;

import argonms.common.StatEffect;
import argonms.common.character.inventory.InventoryTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.NXNode;

public class NXItemDataLoader extends ItemDataLoader {

    private static final Logger LOG = Logger.getLogger(KvjItemDataLoader.class.getName());
    private final String dataPath;

    protected NXItemDataLoader(String wzPath) {
        this.dataPath = wzPath;
    }

    @Override
    public boolean loadAll() {
        try {
            NXFile file = new EagerNXFile(dataPath + "Item.nx" + File.separatorChar);
            for (NXNode baseNode : file.getRoot()) {
                if (baseNode.getName().equals("Pet")) {
                    for (NXNode imgNode : baseNode) {
                        int itemId = Integer.parseInt(imgNode.getName().replace(".img", ""));
                        doWork(itemId, imgNode);
                        loaded.add(itemId);
                    }
                } else {
                    for (NXNode imgNode : baseNode) {
                        for (NXNode node : imgNode) {
                            int itemId = Integer.parseInt(node.getName()); // Does not end with .img
                            doWork(itemId, node);
                            loaded.add(itemId);
                        }
                    }
                }
            }
            file = new EagerNXFile(dataPath + "Character.nx" + File.separatorChar);
            for (NXNode baseNode : file.getRoot()) {
                String name = baseNode.getName();

                // .img and Afterimage nodes in the root are excluded as we don't use them.
                // Face and Hair nodes in the root are excluded as we already load them in NXBeautyDataLoader.
                if (!name.endsWith(".img") && !name.equals("Afterimage") && !name.equals("Face") && !name.equals("Hair")) {
                    for (NXNode node : baseNode) {
                        int itemId = Integer.parseInt(node.getName().replace(".img", ""));
                        doWork(itemId, node);
                        loaded.add(itemId);
                    }
                }
             }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not load all item data from NX files.", e);
            return false;
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Could not parse all item data from NX files.", e);
            return false;
        }

        return true;
    }

    @Override
    protected void load(int itemid) {
        NXNode node = getNode(itemid);
        if (node != null) {
            doWork(itemid, node);
            loaded.add(itemid);
        }
    }

    @Override
    public boolean canLoad(int itemid) {
        return loaded.contains(itemid) || getNode(itemid) != null;
    }

    private NXNode getNode(int itemId) {
        String id = String.format("%08d", itemId);
        String categoryName = InventoryTools.getCategoryName(itemId);

        if (categoryName == null) {
            return null;
        }

        switch (categoryName) {
            case "Pet":
                try {
                    NXFile file = new EagerNXFile(dataPath + "Item.nx" + File.separatorChar);
                    return file.resolve("Pet" + File.separatorChar + id + ".img");
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Could not load NX data for item: " + itemId, e);
                }
                break;
            case "Equip":
                try {
                    NXFile file = new EagerNXFile(dataPath + "Character.nx" + File.separatorChar);
                    return file.resolve(InventoryTools.getCharCat(itemId) + File.separatorChar + id + ".img");
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.log(Level.WARNING, "Could not load NX data for item: " + itemId, e);
                }
                break;
            default:
                try {
                    NXFile file = new EagerNXFile(dataPath + "Item.nx" + File.separatorChar);
                    return file.resolve(categoryName + File.separatorChar + id.substring(0, 4) + ".img" + File.separatorChar + id);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Could not load NX data for item: " + itemId, e);
                    e.printStackTrace();
                }
                break;
        }

        return null;
    }

    private void doWork(int itemId, NXNode node) {
        NXNode infoNode = node.getChild("info");
        if (infoNode != null) {
            loadInfo(itemId, infoNode);
        }

        NXNode reqNode = node.getChild("req");
        if (reqNode != null) {
            loadReq(itemId, reqNode);
        }

        NXNode mobNode = node.getChild("mob");
        if (mobNode != null) {
            loadMob(itemId, mobNode);
        }

        NXNode specNode = node.getChild("spec");
        if (specNode != null) {
            loadSpec(itemId, specNode);
        }

        NXNode interactNode = node.getChild("interact");
        if (interactNode != null) {
            loadInteract(itemId, interactNode);
        }
    }

    private void loadInfo(int itemId, NXNode node) {
        short[] incStats = new short[16];
        for (NXNode propertyNode : node) {
            switch (propertyNode.getName()) {
                case "price":
                    int wholePrice = Integer.parseInt(propertyNode.get().toString());
                    this.wholePrice.put(itemId, wholePrice);
                    break;
                case "slotMax":
                    short slotMax = Short.parseShort(propertyNode.get().toString());
                    this.slotMax.put(itemId, slotMax);
                    break;
                case "tradeBlock":
                    int tradeBlock = Integer.parseInt(propertyNode.get().toString());
                    if (tradeBlock == 1)
                        this.tradeBlocked.add(itemId);
                    break;
                case "only":
                    int onlyOne = Integer.parseInt(propertyNode.get().toString());
                    if (onlyOne == 1)
                        this.onlyOne.add(itemId);
                    break;
                case "quest":
                    int questItem = Integer.parseInt(propertyNode.get().toString());
                    if (questItem == 1)
                        this.questItem.add(itemId);
                    break;
                case "incSTR":
                case "incDEX":
                case "incINT":
                case "incLUK":
                case "incPAD":
                case "incPDD":
                case "incMAD":
                case "incMDD":
                case "incACC":
                case "incEVA":
                case "incMHP":
                case "incMMP":
                case "incSpeed":
                case "incJump":
                    String incName = propertyNode.getName();
                    short incStat = Short.parseShort(propertyNode.get().toString());
                    incStats[StatEffect.getByName(incName)] = incStat;
                    break;
                case "success":
                    int success = Integer.parseInt(propertyNode.get().toString());
                    this.success.put(itemId, success);
                    break;
                case "cursed":
                    int cursed = Integer.parseInt(propertyNode.get().toString());
                    this.cursed.put(itemId, cursed);
                    break;
                case "recover":
                    int recover = Integer.parseInt(propertyNode.get().toString());
                    if (recover == 1)
                        this.recover.add(itemId);
                    break;
                case "randstat":
                    int randStat = Integer.parseInt(propertyNode.get().toString());
                    if (randStat == 1)
                        this.randStat.add(itemId);
                    break;
                case "preventslip":
                    int preventSlip = Integer.parseInt(propertyNode.get().toString());
                    if (preventSlip == 1)
                        this.preventSlip.add(itemId);
                    break;
                case "warmsupport":
                    int warmSupport = Integer.parseInt(propertyNode.get().toString());
                    if (warmSupport == 1)
                        this.warmSupport.add(itemId);
                    break;
                case "cash":
                    int cash = Integer.parseInt(propertyNode.get().toString());
                    if (cash == 1)
                        this.cash.add(itemId);
                    break;
                case "time":
                    if (!operatingHours.containsKey(itemId))
                        operatingHours.put(itemId, new ArrayList<>());

                    for (NXNode timeNode : propertyNode) {
                        String time = timeNode.get().toString();
                        operatingHours.get(itemId).add(time.getBytes()); // TODO: Test. Not sure if this works.
                    }
                    break;
                case "skill":
                    if (!skills.containsKey(itemId))
                        skills.put(itemId, new ArrayList<>());

                    for (NXNode skillNode : propertyNode) {
                        int skillId = Integer.parseInt(skillNode.get().toString());
                        skills.get(itemId).add(skillId);
                    }
                    break;
                case "unitPrice":
                    double unitPrice = Double.parseDouble(propertyNode.get().toString());
                    this.unitPrice.put(itemId, unitPrice);
                    break;
                case "reqSTR":
                case "reqDEX":
                case "reqINT":
                case "reqLUK":
                case "reqLevel":
                case "reqJob":
                    if (!reqStats.containsKey(itemId))
                        reqStats.put(itemId, new short[16]);

                    String reqName = propertyNode.getName();
                    short reqStat = Short.parseShort(propertyNode.get().toString());

                    this.reqStats.get(itemId)[StatEffect.getByName(reqName)] = reqStat;
                    break;
                case "tuc":
                    byte tuc = Byte.parseByte(propertyNode.get().toString()); // TODO: Test. Not sure if this works.
                    this.tuc.put(itemId, tuc);
                    break;
                case "stateChangeItem":
                    int changeItemId = Integer.parseInt(propertyNode.get().toString());
                    this.triggerItem.put(itemId, changeItemId);
                    break;
                case "meso":
                    int meso = Integer.parseInt(propertyNode.get().toString());
                    this.mesoValue.put(itemId, meso);
                    break;
                case "hungry":
                    int hunger = Integer.parseInt(propertyNode.get().toString());
                    this.petHunger.put(itemId, hunger);
                    break;
                case "life":
                    byte life = Byte.parseByte(propertyNode.get().toString()); // TODO: Test. Not sure if this works.
                    this.petPeriod.put(itemId, life);
                    break;
//                  ## Could not find this one ##
//                  case "evolve":
//                      this.petEvolveChoices.put();
//                      break;
                case "tamingMob":
                    byte tamingMob = Byte.parseByte(propertyNode.get().toString()); // TODO: Test. Not sure if this works.
                    this.tamingMobIds.put(itemId, tamingMob);
                    break;
                default:
                    break;
            }
        }

        // Check if there are any bonus stats.
        for (int i = 0; i < 16; i++) {
            if (incStats[i] != 0) {
                bonusStats.put(itemId, incStats);
                break;
            }
        }
    }

    private void loadReq(int itemId, NXNode node) {
        List<Integer> requirements = new ArrayList<>();
        for (NXNode baseNode : node) {
            int equipId = Integer.parseInt(baseNode.get().toString());
            requirements.add(equipId);
        }
        scrollReqs.put(itemId, requirements);
    }

    private void loadMob(int itemId, NXNode node) {
        ArrayList<int[]> mobsToSpawn = new ArrayList<>();
        for (NXNode baseNode : node) {
            int id = Integer.parseInt(baseNode.getChild("id").get().toString());
            int prob = Integer.parseInt(baseNode.getChild("prob").get().toString());
            mobsToSpawn.add(new int[] { id, prob });
        }

        // Check if there are any mobs to spawn.
        if (!mobsToSpawn.isEmpty())
            summons.put(itemId, mobsToSpawn);
    }

    private void loadSpec(int itemId, NXNode node) {
        ItemEffectsData effect = new ItemEffectsData(itemId);
        for (NXNode baseNode : node) {
            String name = baseNode.getName();
            switch (name) {
                case "time":
                    int time = Integer.parseInt(baseNode.get().toString());
                    effect.setDuration(time);
                    break;
                case "pad":
                    short pad = Short.parseShort(baseNode.get().toString());
                    effect.setWatk(pad);
                    break;
                case "pdd":
                    short pdd = Short.parseShort(baseNode.get().toString());
                    effect.setWdef(pdd);
                    break;
                case "mad":
                    short mad = Short.parseShort(baseNode.get().toString());
                    effect.setMatk(mad);
                    break;
                case "mdd":
                    short mdd = Short.parseShort(baseNode.get().toString());
                    effect.setMdef(mdd);
                    break;
                case "acc":
                    short acc = Short.parseShort(baseNode.get().toString());
                    effect.setAcc(acc);
                    break;
                case "eva":
                    short eva = Short.parseShort(baseNode.get().toString());
                    effect.setAvoid(eva);
                    break;
                case "hp":
                    short hp = Short.parseShort(baseNode.get().toString());
                    effect.setHpRecover(hp);
                    break;
                case "mp":
                    short mp = Short.parseShort(baseNode.get().toString());
                    effect.setMpRecover(mp);
                    break;
                case "speed":
                    short speed = Short.parseShort(baseNode.get().toString());
                    effect.setSpeed(speed);
                    break;
                case "jump":
                    short jump = Short.parseShort(baseNode.get().toString());
                    effect.setJump(jump);
                    break;
                case "morph":
                    short morph = Short.parseShort(baseNode.get().toString());
                    effect.setMorph(morph);
                    break;
                case "hpR":
                    short hpR = Short.parseShort(baseNode.get().toString());
                    effect.setHpRecoverPercent(hpR);
                    break;
                case "mpR":
                    short mpR = Short.parseShort(baseNode.get().toString());
                    effect.setMpRecoverPercent(mpR);
                    break;
                case "moveTo":
                    int moveTo = Integer.parseInt(baseNode.get().toString());
                    effect.setMoveTo(moveTo);
                    break;
                case "poison":
                    int poison = Integer.parseInt(baseNode.get().toString());
                    if (poison == 1)
                        effect.setPoison();
                    break;
                case "seal":
                    int seal = Integer.parseInt(baseNode.get().toString());
                    if (seal == 1)
                        effect.setSeal();
                    break;
                case "darkness":
                    int darkness = Integer.parseInt(baseNode.get().toString());
                    if (darkness == 1)
                        effect.setDarkness();
                    break;
                case "weakness":
                    int weakness = Integer.parseInt(baseNode.get().toString());
                    if (weakness == 1)
                        effect.setWeakness();
                    break;
                case "curse":
                    int curse = Integer.parseInt(baseNode.get().toString());
                    if (curse == 1)
                        effect.setCurse();
                    break;
                case "consumeOnPickup":
                    int consumeOnPickup = Integer.parseInt(baseNode.get().toString());
                    if (consumeOnPickup == 1)
                        effect.setConsumeOnPickup();
                    break;
//                  ## Could not find this one ##
//                  case "petConsumableBy":
//                      break;
//                  ## Could not find this one ##
//                  case "endEffect":
//                      break;
                case "inc":
                    int inc = Integer.parseInt(baseNode.get().toString());
                    petFullnessRecover.put(itemId, (byte) inc);
                    break;
                default:
                    break;
            }
        }
    }

    private void loadInteract(int itemId, NXNode node) {
        for (NXNode baseNode : node) {
            if (!petCommands.containsKey(itemId))
                petCommands.put(itemId, new HashMap<>());

            byte commandId = Byte.parseByte(baseNode.getChild("command").get().toString().replace("c", ""));
            int prob = Integer.parseInt(baseNode.getChild("prob").get().toString());
            int inc = Integer.parseInt(baseNode.getChild("inc").get().toString());
            petCommands.get(itemId).put(commandId, new int[] { prob, inc });
        }
    }
}
