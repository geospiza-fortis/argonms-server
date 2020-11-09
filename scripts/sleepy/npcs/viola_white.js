/**
 * a pile of white flowers (NPC 1063002)
 * Hidden Street: The Deep Forest of Patience <Step 7> (Map 105040316)
 *
 * Gives rare jewel or mineral ores as a reward if the quest is completed and
 * the player successfully reached the end.
 *
 * @author geospiza (content from GoldenKevin, and KiniroMS r227)
 */

let rewards = [4020007, 4020008, 4010006];
let itemId = rewards[Math.floor(Math.random() * rewards.length)];
let quantity = 2;

if (player.gainItem(itemId, quantity)) {
  player.changeMap(105040300);
} else {
  npc.say("Please check whether your ETC. inventory is full.");
}
