/**
 * a pile of blue flowers (NPC 1063001)
 * Hidden Street: The Deep Forest of Patience <Step 4> (Map 105040313)
 *
 * Gives jewel ores as a reward if the quest is completed and the player
 * successfully reached the end.
 *
 * @author Geospiza (content from GoldenKevin, and KiniroMS r227)
 */

let rewards = [4020005, 4020006, 4020004, 4020001, 4020003, 4020000, 4020002];
let itemId = rewards[Math.floor(Math.random() * rewards.length)];
let quantity = 2;

if (player.gainItem(itemId, quantity)) {
  player.changeMap(105040300);
} else {
  npc.say("Please check whether your ETC. inventory is full.");
}
