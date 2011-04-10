/*
 * ArgonMS MapleStory server emulator written in Java
 * Copyright (C) 2011  GoldenKevin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package argonms.net.client.handler;

import argonms.character.Player;
import argonms.game.GameClient;
import argonms.loading.mob.Skill;
import argonms.loading.skill.MobSkillEffectsData;
import argonms.loading.skill.SkillDataLoader;
import argonms.map.MapEntity;
import argonms.map.MapEntity.MapEntityType;
import argonms.map.movement.AbsoluteLifeMovement;
import argonms.map.movement.ChairMovement;
import argonms.map.movement.ChangeEquipSpecialAwesome;
import argonms.map.movement.JumpDownMovement;
import argonms.map.movement.LifeMovement;
import argonms.map.movement.LifeMovementFragment;
import argonms.map.movement.RelativeLifeMovement;
import argonms.map.movement.TeleportMovement;
import argonms.map.entity.Mob;
import argonms.net.client.ClientSendOps;
import argonms.net.client.CommonPackets;
import argonms.net.client.RemoteClient;
import argonms.tools.Rng;
import argonms.tools.input.LittleEndianReader;
import argonms.tools.output.LittleEndianByteArrayWriter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GoldenKevin
 */
public class GameMovementHandler {
	private static final Logger LOG = Logger.getLogger(GameMovementHandler.class.getName());

	public static void handleMovePlayer(LittleEndianReader packet, RemoteClient rc) {
		packet.readByte();
		Point startPos = packet.readPos();
		List<LifeMovementFragment> res = parseMovement(packet);
		if (packet.available() != 18) {
			//TODO: this is received when a player moves after being revived.
			//decode and handle?
			LOG.log(Level.WARNING, "Received unusual player movement packet w/ {0} bytes remaining: {1}",
					new Object[] { packet.available(), packet });
			return;
		}
		Player player = ((GameClient) rc).getPlayer();
		player.getMap().playerMoved(player, res, startPos);
		updatePosition (res, player, 0);
	}

	public static void handleMoveMob(LittleEndianReader packet, RemoteClient rc) {
		int entId = packet.readInt();
		short moveid = packet.readShort();

		Player player = ((GameClient) rc).getPlayer();
		//TODO: Synchronize on the mob (for the canUseSkill, which gets Hp, and
		//the aggro things)
		MapEntity ent = player.getMap().getEntityById(entId);
		if (ent == null || ent.getEntityType() != MapEntityType.MONSTER)
			return;
		Mob monster = (Mob) ent;

		List<LifeMovementFragment> res = null;
		boolean useSkill = packet.readBool();
		byte skill = packet.readByte();
		short skillId = (short) (packet.readByte() & 0xFF);
		byte skillLevel = packet.readByte();
		byte skill3 = packet.readByte();
		byte skill4 = packet.readByte();

		Skill skillToUse = null;
		MobSkillEffectsData skillToUseEffect = null;

		List<Skill> skills = monster.getSkills();
		int skillsCount = skills.size();
		if (useSkill && skillsCount > 0) {
			skillToUse = skills.get(Rng.getGenerator().nextInt(skillsCount));
			skillToUseEffect = SkillDataLoader.getInstance().getMobSkill(skillToUse.getSkill()).getLevel(skillToUse.getLevel());
			if (!monster.canUseSkill(skillToUseEffect)) {
				skillToUse = null;
				skillToUseEffect = null;
			}
			//TODO: apply mob skill
		}

		if ((skillId >= 100 && skillId <= 200) && monster.hasSkill(skillId, skillLevel)) {
			MobSkillEffectsData playerSkillEffect = SkillDataLoader.getInstance().getMobSkill(skillId).getLevel(skillLevel);
			if (playerSkillEffect != null && monster.canUseSkill(playerSkillEffect))
				monster.applyEffect(playerSkillEffect, player, true);
		}

		packet.readByte();
		packet.readInt();
		Point startPos = packet.readPos();

		res = parseMovement(packet);

		Player controller = monster.getController();
		if (controller != player) {
			if (monster.wasAttackedBy(player)) { // aggro and controller change
				if (controller != null) {
					controller.uncontrolMonster(monster);
					controller.getClient().getSession().send(CommonPackets.writeStopControllingMonster(monster));
				}
				monster.setController(player);
				player.controlMonster(monster);
				player.getClient().getSession().send(CommonPackets.writeControlMonster(monster, true));
				monster.setControllerHasAggro(true);
				monster.setControllerKnowsAboutAggro(false);
			} else {
				return;
			}
		} else if (skill == -1 && monster.controllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
			monster.setControllerHasAggro(false);
			monster.setControllerKnowsAboutAggro(false);
		}
		boolean aggro = monster.controllerHasAggro();

		if (skillToUse != null)
			rc.getSession().send(moveMonsterResponse(entId, moveid, monster.getMp(), aggro, skillToUse.getSkill(), skillToUse.getLevel()));
		else
			rc.getSession().send(moveMonsterResponse(entId, moveid, monster.getMp(), aggro, (short) 0, (byte) 0));

		if (aggro)
			monster.setControllerKnowsAboutAggro(true);

		if (packet.available() != 9) {
			LOG.log(Level.WARNING, "Received unusual life movement packet w/ {0} bytes remaining: {1}",
					new Object[] { packet.available(), packet });
			return;
		}
		player.getMap().monsterMoved(player, monster, res, useSkill, skill, skillId, skillLevel, skill3, skill4, startPos);
		updatePosition (res, monster, -1);
	}

	public static void handleMoveNpc(LittleEndianReader packet, RemoteClient rc) {
		//too complicated to add one NPC animator per map (mobs were bad enough)
		//so we'll just let all clients animate their own NPCs and echo back
		//what they send to us.
		LittleEndianByteArrayWriter lew;
		int remaining = packet.available();
		if (remaining == 6) { //speech bubble
			lew = new LittleEndianByteArrayWriter(8);
			lew.writeShort(ClientSendOps.MOVE_NPC);
			//int - entityid, short - messageid
			lew.writeBytes(packet.readBytes(remaining));
		} else { //actual movement
			lew = new LittleEndianByteArrayWriter(remaining - 7);
			lew.writeShort(ClientSendOps.MOVE_NPC);
			lew.writeBytes(packet.readBytes(remaining - 9));
		}
		rc.getSession().send(lew.getBytes());
	}

	private static List<LifeMovementFragment> parseMovement(LittleEndianReader packet) {
		List<LifeMovementFragment> res = new ArrayList<LifeMovementFragment>();
		int numCommands = packet.readByte();
		for (int i = 0; i < numCommands; i++) {
			byte command = packet.readByte();
			switch (command) {
				case 0: // normal move
				case 5:
				case 17: { //float
					Point pos = packet.readPos();
					Point wobble = packet.readPos();
					short unk = packet.readShort();
					byte newstate = packet.readByte();
					short duration = packet.readShort();
					AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, pos, duration, newstate);
					alm.setUnk(unk);
					alm.setPixelsPerSecond(wobble);
					res.add(alm);
					break;
				} case 1:
				case 2:
				case 6: // fj
				case 12:
				case 13: // Shot-jump-back thing
				case 16: { //float
					Point mod = packet.readPos();
					byte newstate = packet.readByte();
					short duration = packet.readShort();
					RelativeLifeMovement rlm = new RelativeLifeMovement(command, mod, duration, newstate);
					res.add(rlm);
					break;
				} case 3:
				case 4: // tele... -.-
				case 7: // assaulter
				case 8: // assassinate
				case 9: // rush
				case 14: {
					Point pos = packet.readPos();
					Point wobble = packet.readPos();
					byte newstate = packet.readByte();
					TeleportMovement tm = new TeleportMovement(command, pos, newstate);
					tm.setPixelsPerSecond(wobble);
					res.add(tm);
					break;
				} case 10: { //change equip???
					res.add(new ChangeEquipSpecialAwesome(packet.readByte()));
					break;
				} case 11: { //chair
					Point pos = packet.readPos();
					short unk = packet.readShort();
					byte newstate = packet.readByte();
					short duration = packet.readShort();
					ChairMovement cm = new ChairMovement(command, pos, duration, newstate);
					cm.setUnk(unk);
					res.add(cm);
					break;
				} case 15: {
					Point pos = packet.readPos();
					Point wobble = packet.readPos();
					short unk = packet.readShort();
					short fh = packet.readShort();
					byte newstate = packet.readByte();
					short duration = packet.readShort();
					JumpDownMovement jdm = new JumpDownMovement(command, pos, duration, newstate);
					jdm.setUnk(unk);
					jdm.setPixelsPerSecond(wobble);
					jdm.setFH(fh);
					res.add(jdm);
					break;
				} default: {
					return null;
				}
			}
		}
		return res;
	}

	//TODO: I don't like instanceof...
	private static void updatePosition(List<LifeMovementFragment> movement, MapEntity target, int yoffset) {
		for (LifeMovementFragment move : movement) {
			if (move instanceof LifeMovement) {
				LifeMovement lm = (LifeMovement) move;
				if (lm instanceof AbsoluteLifeMovement) {
					Point position = lm.getPosition();
					position.y += yoffset;
					target.setPosition(position);
				}
				target.setStance(lm.getNewstate());
			}
		}
	}

	private static byte[] moveMonsterResponse(int entityid, short moveid, int currentMp, boolean useSkills, short skillId, byte skillLevel) {
		LittleEndianByteArrayWriter mplew = new LittleEndianByteArrayWriter(13);

		mplew.writeShort(ClientSendOps.MOVE_MONSTER_RESPONSE);
		mplew.writeInt(entityid);
		mplew.writeShort(moveid);
		mplew.writeBool(useSkills);
		mplew.writeShort((short) currentMp);
		mplew.writeByte((byte) skillId);
		mplew.writeByte(skillLevel);

		return mplew.getBytes();
	}
}
