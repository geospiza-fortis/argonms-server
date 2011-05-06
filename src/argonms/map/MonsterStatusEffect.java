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

package argonms.map;

/**
 *
 * @author GoldenKevin
 */
public enum MonsterStatusEffect {
	WATK				(0x00000001),
	WDEF				(0x00000002),
	MATK				(0x00000004),
	MDEF				(0x00000008),
	ACC					(0x00000010),
	AVOID				(0x00000020),
	SPEED				(0x00000040),
	STUN				(0x00000080),
	FREEZE				(0x00000100),
	POISON				(0x00000200),
	SEAL				(0x00000400),
	TAUNT				(0x00000800),
	WEAPON_ATTACK_UP	(0x00001000),
	WEAPON_DEFENSE_UP	(0x00002000),
	MAGIC_ATTACK_UP		(0x00004000),
	MAGIC_DEFENSE_UP	(0x00008000),
	DOOM				(0x00010000),
	SHADOW_WEB			(0x00020000),
	WEAPON_IMMUNITY		(0x00040000),
	MAGIC_IMMUNITY		(0x00080000),
	NINJA_AMBUSH		(0x00400000),
	INERTMOB			(0x10000000);

	private final int mask;

	private MonsterStatusEffect(int mask) {
		this.mask = mask;
	}

	public int intValue() {
		return mask;
	}
}
