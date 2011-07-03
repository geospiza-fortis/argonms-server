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

package argonms.shop;

import argonms.common.LocalServer;
import argonms.common.ServerType;
import argonms.common.loading.DataFileType;
import argonms.common.loading.item.ItemDataLoader;
import argonms.common.loading.string.StringDataLoader;
import argonms.login.LoginWorld;
import argonms.common.net.external.ClientListener;
import argonms.common.net.external.ClientListener.ClientFactory;
import argonms.common.net.external.PlayerLog;
import argonms.common.tools.DatabaseManager;
import argonms.common.tools.DatabaseManager.DatabaseType;
import argonms.common.tools.Scheduler;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GoldenKevin
 */
public class ShopServer implements LocalServer {
	private static final Logger LOG = Logger.getLogger(ShopServer.class.getName());

	private static ShopServer instance;

	private ClientListener<ShopClient> handler;
	private ShopCenterInterface sci;
	private String address;
	private int port;
	private Map<Byte, LoginWorld> onlineWorlds;
	private boolean preloadAll;
	private DataFileType wzType;
	private String wzPath;
	private boolean useNio;
	private boolean centerConnected;
	private PlayerLog<ShopCharacter> storage;

	private ShopServer() {
		onlineWorlds = new HashMap<Byte, LoginWorld>();
	}

	public void init() {
		Properties prop = new Properties();
		String centerIp;
		int centerPort;
		String authKey;
		try {
			FileReader fr = new FileReader(System.getProperty("argonms.shop.config.file", "shop.properties"));
			prop.load(fr);
			fr.close();
			address = prop.getProperty("argonms.shop.host");
			port = Integer.parseInt(prop.getProperty("argonms.shop.port"));
			wzType = DataFileType.valueOf(prop.getProperty("argonms.shop.data.type"));
			//wzPath = prop.getProperty("argonms.shop.data.dir");
			preloadAll = Boolean.parseBoolean(prop.getProperty("argonms.shop.data.preload"));
			centerIp = prop.getProperty("argonms.shop.center.ip");
			centerPort = Integer.parseInt(prop.getProperty("argonms.shop.center.port"));
			authKey = prop.getProperty("argonms.shop.auth.key");
			useNio = Boolean.parseBoolean(prop.getProperty("argonms.shop.usenio"));
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "Could not load shop server properties!", ex);
			System.exit(2);
			return;
		}
		boolean mcdb = (wzType == DataFileType.MCDB);
		prop = new Properties();
		try {
			FileReader fr = new FileReader(System.getProperty("argonms.db.config.file", "db.properties"));
			prop.load(fr);
			fr.close();
			DatabaseManager.setProps(prop, mcdb, useNio);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "Could not load database properties!", ex);
			System.exit(3);
			return;
		} catch (SQLException ex) {
			LOG.log(Level.SEVERE, "Could not initialize database!", ex);
			System.exit(3);
			return;
		}
		try {
			DatabaseManager.cleanup(DatabaseType.STATE, null, null, DatabaseManager.getConnection(DatabaseType.STATE));
			if (mcdb)
				DatabaseManager.cleanup(DatabaseType.WZ, null, null, DatabaseManager.getConnection(DatabaseType.WZ));
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Could not connect to database!", e);
			System.exit(3);
			return;
		}
		wzPath = System.getProperty("argonms.data.dir");
		sci = new ShopCenterInterface(authKey, this);
		sci.connect(centerIp, centerPort);
		System.exit(4); //connection with center server lost before we were able to shutdown
	}

	private void initializeData(boolean preloadAll, DataFileType wzType, String wzPath) {
		StringDataLoader.setInstance(wzType, wzPath);
		ItemDataLoader.setInstance(wzType, wzPath);
		long start, end;
		start = System.nanoTime();
		System.out.print("Loading String data...");
		StringDataLoader.getInstance().loadAll();
		System.out.println("\tDone!");
		if (preloadAll) {
			System.out.print("Loading Item data...");
			ItemDataLoader.getInstance().loadAll();
			System.out.println("\tDone!");
		}
		end = System.nanoTime();
		System.out.println("Preloaded data in " + ((end - start) / 1000000.0) + "ms.");
	}

	public void centerConnected() {
		LOG.log(Level.FINE, "Link with Center server established.");
		centerConnected = true;
		initializeData(preloadAll, wzType, wzPath);
		Scheduler.enable();
		try {
			handler = new ClientListener<ShopClient>(ServerType.SHOP, (byte) -1, useNio, new ClientShopPacketProcessor(), new ClientFactory<ShopClient>() {
				public ShopClient newInstance(byte world, byte client) {
					return new ShopClient();
				}
			});
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "\"new ShopClient(byte world, byte channel)\" constructor missing!");
			System.exit(5);
		}
		if (handler.bind(port)) {
			LOG.log(Level.INFO, "Shop Server is online.");
			sci.serverReady();
		} else {
			System.exit(5);
		}
	}

	public void centerDisconnected() {
		if (centerConnected) {
			LOG.log(Level.SEVERE, "Center server disconnected.");
			centerConnected = false;
		}
	}

	public void gameConnected(byte serverId, byte world, String host, Map<Byte, Integer> ports) {
		try {
			byte[] ip = InetAddress.getByName(host).getAddress();
			LoginWorld w = onlineWorlds.get(Byte.valueOf(world));
			if (w == null) {
				w = new LoginWorld(world);
				onlineWorlds.put(Byte.valueOf(world), w);
			}
			w.addGameServer(ip, ports, serverId);
			LOG.log(Level.INFO, "{0} server accepted from {1}.", new Object[] { ServerType.getName(serverId), host });
		} catch (UnknownHostException e) {
			LOG.log(Level.INFO, "Could not accept " + ServerType.getName(world)
					+ " server because its address could not be resolved!", e);
		}
	}

	public void gameDisconnected(byte serverId, byte world) {
		LOG.log(Level.INFO, "{0} server disconnected.", ServerType.getName(serverId));
		Byte oW = Byte.valueOf(world);
		LoginWorld w = onlineWorlds.get(oW);
		w.removeGameServer(serverId);
		if (w.getChannelCount() == 0)
			onlineWorlds.remove(oW);
	}

	public static ShopServer getInstance() {
		return instance;
	}

	public String getExternalIp() {
		return address;
	}

	public int getClientPort() {
		return port;
	}

	public void addPlayer(ShopCharacter p) {
		storage.addPlayer(p);
	}

	public void removePlayer(ShopCharacter p) {
		storage.deletePlayer(p);
	}

	public ShopCharacter getPlayerById(int characterid) {
		return storage.getPlayer(characterid);
	}

	public ShopCharacter getPlayerByName(String name) {
		return storage.getPlayer(name);
	}

	public static void main(String[] args) {
		instance = new ShopServer();
		instance.init();
	}
}
