/**
 * Mysterious Statue (NPC 1061006)
 * Dungeon: Sleepywood (Map 105040300)
 *
 * @author geospiza (content from GoldenKevin, and KiniroMS r227)
 */

let selection = npc.askYesNo(
  "Once I lay my hand on the statue, a strange light covers me and it feels " +
    "like I am being sucked into somewhere else. Is it okay to be moved to " +
    "somewhere else randomly just like that?"
);
if (selection == 0) {
  npc.say("Alright, see you next time.");
} else {
  let selection = npc.askMenu(
    "#L0# Pink Viola" + "#L1# Blue Viola" + "#L2# White Viola"
  );
  switch (selection) {
    case 0:
      player.changeMap(105040310);
      break;
    case 1:
      player.changeMap(105040312);
      break;
    case 2:
      player.changeMap(105040314);
      break;
  }
}
