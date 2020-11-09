/**
 * a pile of pink flowers (NPC 1063000)
 * Hidden Street: The Deep Forest of Patience <Step 2> (Map 105040311)
 *
 * Gives mineral ores as a reward if the quest is completed and the player
 * successfully reached the end.
 *
 * @author Geospiza (content from GoldenKevin, and KiniroMS r227)
 */

let rewards = [4010003, 4010000, 4010002, 4010005, 4010004, 4010001];
let itemId = rewards[Math.floor(Math.random() * rewards.length)];
let quantity = 2;

if (player.gainItem(itemId, quantity)) {
  player.changeMap(105040300);
} else {
  npc.say("Please check whether your ETC. inventory is full.");
}
