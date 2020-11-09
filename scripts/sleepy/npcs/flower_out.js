/**
 * Crumbling Statue (NPC 1061007)
 * Hidden Street: The Deep Forest of Patience <Step 1> (Map 105040310),
 * Hidden Street: The Deep Forest of Patience <Step 2> (Map 105040311),
 * Hidden Street: The Deep Forest of Patience <Step 3> (Map 105040312),
 * Hidden Street: The Deep Forest of Patience <Step 4> (Map 105040313),
 * Hidden Street: The Deep Forest of Patience <Step 5> (Map 105040314),
 * Hidden Street: The Deep Forest of Patience <Step 6> (Map 105040315),
 * Hidden Street: The Deep Forest of Patience <Step 7> (Map 105040316)
 *
 * Forfeits jump quests.
 *
 * @author GoldenKevin (content from KiniroMS r227)
 */

npc.sayNext("It's a Crumbling Statue.");
if (npc.askYesNo("Would you like to leave this place?") == 1)
  player.changeMap(105040300);
