/*
 * ArgonMS MapleStory server emulator written in Java
 * Copyright (C) 2011-2012  GoldenKevin
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

package argonms.game.field;

import java.awt.Point;

/**
 *
 * @author GoldenKevin
 */
public interface MapEntity {
	public enum EntityType {
		MONSTER, DROP, NPC, PLAYER, REACTOR, MINI_ROOM, DOOR, SUMMON, MIST
	}

	public EntityType getEntityType();
	public int getId();
	public void setId(int newEid);
	public Point getPosition();
	public void setPosition(Point newPos);
	public byte getStance();
	public void setStance(byte newStance);
	public short getFoothold();
	public void setFoothold(short newFh);

	public boolean isAlive();
	public boolean isVisible();

	public byte[] getShowNewSpawnMessage();
	public byte[] getShowExistingSpawnMessage();
	public byte[] getDestructionMessage();
}
