/*
 * ArgonMS MapleStory server emulator written in Java
 * Copyright (C) 2011-2013  GoldenKevin
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

--
-- This script needs to be executed only if the shop server is enabled.
--

-- Keep track of cash for each account.
CREATE TABLE IF NOT EXISTS `cashshopbalance` (
  `entryid` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `accountid` INT(11) NOT NULL,
  `paypalnx` INT(11) NOT NULL,
  `maplepoints` INT(11) NOT NULL,
  `gamecardnx` INT(11) NOT NULL,
  PRIMARY KEY (`entryid`),
  CONSTRAINT FOREIGN KEY (`accountid`) REFERENCES `accounts` (`id`) ON DELETE CASCADE,
  UNIQUE (`accountid`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `cashshopcoupons` (
  `entryid` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(65508) CHARACTER SET latin1 NOT NULL, /* InnoDB limit; client supports up to 65529 */
  `maplepoints` INT(11),
  `mesos` INT(11),
  `remaininguses` INT(11) NOT NULL DEFAULT 1,
  `expiredate` BIGINT(20) NOT NULL DEFAULT 9223372036854775807,
  PRIMARY KEY (`entryid`),
  UNIQUE KEY (`code`(767))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `cashshopcouponitems` (
  `entryid` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `couponentryid` INT(11) UNSIGNED,
  `sn` INT(11) NOT NULL,
  PRIMARY KEY (`entryid`),
  KEY (`couponentryid`),
  CONSTRAINT FOREIGN KEY (`couponentryid`) REFERENCES `cashshopcoupons` (`entryid`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `cashshopcouponusers` (
  `entryid` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `couponentryid` INT(11) UNSIGNED,
  `accountid` INT(11) NOT NULL,
  PRIMARY KEY (`entryid`),
  KEY (`couponentryid`),
  CONSTRAINT FOREIGN KEY (`couponentryid`) REFERENCES `cashshopcoupons` (`entryid`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `cashshoplimitedcommodities` (
  `entryid` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `itemid` INT(11) NOT NULL,
  `used` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`entryid`),
  UNIQUE KEY (`itemid`)
) ENGINE=InnoDB;