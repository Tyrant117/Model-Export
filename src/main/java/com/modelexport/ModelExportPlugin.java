/*
 * Copyright (c) 2020, Bram91
 * Copyright (c) 2020, Unmoon <https://github.com/Unmoon>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.modelexport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.inject.Provides;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.tileindicators.TileIndicatorsConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;

@Slf4j
@PluginDescriptor(
	name = "Model Export",
		description = "Allows exporting models by right clicking them.",
		tags = {"Model", "Dumper","Exporter","3d","obj"}
)
public class ModelExportPlugin extends Plugin
{
	private static final String EXPORT_MODEL = "Export Model";
	private static final String EXPORT_ALL = "Export ALL Models";
	private static final String MENU_TARGET = "Player";
	private static final String MENU_ALL = "ALL";
	private static final WidgetMenuOption FIXED_EQUIPMENT_TAB_EXPORT = new WidgetMenuOption(EXPORT_MODEL,
			MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_EQUIPMENT_TAB);
	private static final WidgetMenuOption FIXED_EQUIPMENT_TAB_ALLEXPORT = new WidgetMenuOption(EXPORT_ALL,
			MENU_ALL, WidgetInfo.FIXED_VIEWPORT_EQUIPMENT_TAB);
	private static final WidgetMenuOption RESIZABLE_EQUIPMENT_TAB_EXPORT = new WidgetMenuOption(EXPORT_MODEL,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_EQUIPMENT_TAB);
	private static final WidgetMenuOption RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB_EXPORT = new WidgetMenuOption(EXPORT_MODEL,
			MENU_TARGET,WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);
	private final ImmutableList<String> set = ImmutableList.of(
			"Trade with", "Attack", "Talk-to", "Examine"
	);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SceneOverlay sceneOverlay;

	@Inject
	private MenuManager menuManager;

	@Inject
	private ModelExportConfig config;

	@Provides
	ModelExportConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ModelExportConfig.class);
	}

	private final Table<WorldPoint, Integer, GroundItem> groundItems = HashBasedTable.create();
	private Tile lastTileOnMenuOpen = null;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(sceneOverlay);
		menuManager.addManagedCustomMenu(FIXED_EQUIPMENT_TAB_EXPORT);
		menuManager.addManagedCustomMenu(FIXED_EQUIPMENT_TAB_ALLEXPORT);
		menuManager.addManagedCustomMenu(RESIZABLE_EQUIPMENT_TAB_EXPORT);
		menuManager.addManagedCustomMenu(RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB_EXPORT);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(sceneOverlay);
		menuManager.removeManagedCustomMenu(FIXED_EQUIPMENT_TAB_EXPORT);
		menuManager.removeManagedCustomMenu(FIXED_EQUIPMENT_TAB_ALLEXPORT);
		menuManager.removeManagedCustomMenu(RESIZABLE_EQUIPMENT_TAB_EXPORT);
		menuManager.removeManagedCustomMenu(RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB_EXPORT);
		groundItems.clear();
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)	{
		lastTileOnMenuOpen = client.getSelectedSceneTile();
		// Right now this is bugged and only shows one entry.
		MenuEntry[] menuEntries = event.getMenuEntries();
//		final MenuEntry exportedMenuEntry = new MenuEntry();
//		exportedMenuEntry.setOption("Export Tile");
//		exportedMenuEntry.setTarget("");
//		exportedMenuEntry.setIdentifier(0);
//		exportedMenuEntry.setParam1(0);
//		addEntry(exportedMenuEntry);

		final MenuEntry exportedTextMenuEntry = new MenuEntry();
		exportedTextMenuEntry.setOption("Export Text Tile");
		exportedTextMenuEntry.setTarget("");
		exportedTextMenuEntry.setIdentifier(0);
		exportedTextMenuEntry.setParam1(0);
		addEntry(exportedTextMenuEntry);

//		final MenuEntry exportedRegionMapMenuEntry = new MenuEntry();
//		exportedRegionMapMenuEntry.setOption("Export Region Map");
//		exportedRegionMapMenuEntry.setTarget("");
//		exportedRegionMapMenuEntry.setIdentifier(0);
//		exportedRegionMapMenuEntry.setParam1(0);
//		addEntry(exportedRegionMapMenuEntry);

		final MenuEntry exportedRegionMenuEntry = new MenuEntry();
		exportedRegionMenuEntry.setOption("Export Text Region");
		exportedRegionMenuEntry.setTarget("");
		exportedRegionMenuEntry.setIdentifier(0);
		exportedRegionMenuEntry.setParam1(0);
		addEntry(exportedRegionMenuEntry);

//		final MenuEntry exportedSceneMenuEntry = new MenuEntry();
//		exportedSceneMenuEntry.setOption("Export Scene");
//		exportedSceneMenuEntry.setTarget("");
//		exportedSceneMenuEntry.setIdentifier(0);
//		exportedSceneMenuEntry.setParam1(0);
//		addEntry(exportedSceneMenuEntry);

//		final MenuEntry exportTextures = new MenuEntry();
//		exportTextures.setOption("Export Textures");
//		exportTextures.setTarget("");
//		exportTextures.setIdentifier(0);
//		exportTextures.setParam1(0);
//		addEntry(exportTextures);


		MenuEntry firstEntry = event.getFirstEntry();
		if(!firstEntry.getTarget().isEmpty())
		{
			final MenuEntry firstMenuEntry = new MenuEntry();
			firstMenuEntry.setOption("Export Target");
			firstMenuEntry.setTarget(firstEntry.getTarget());
			firstMenuEntry.setIdentifier(firstEntry.getIdentifier());
			firstMenuEntry.setParam1(0);
			addEntry(firstMenuEntry);
		}
	}

	private void addEntry(MenuEntry exportMenuEntry)	{
		MenuEntry[] oldMenu = client.getMenuEntries();
		MenuEntry[] newMenu = new MenuEntry[oldMenu.length + 1];
		for (int i = 0; i < oldMenu.length + 1; i++) {
			if (i < 1)
				newMenu[i] = oldMenu[i];
			else if (i == 1)
				newMenu[i] = exportMenuEntry;
			else
				newMenu[i] = oldMenu[i - 1];
		}
		client.setMenuEntries(newMenu);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		log.info(event.getMenuOption());
		try {
			if (event.getMenuOption().equals("Export Text Region")) {
				exportTextRegion();
			}else if (event.getMenuOption().equals("Export Text Tile")) {
				exportTextTile();
			}else if (event.getMenuOption().equals("Export Target")) {
				exportTargetModel(event.getMenuTarget(), event.getId());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final int FNV_32_INIT = 0x811c9dc5;
	private static final int FNV_32_PRIME = 0x01000193;
	private static int VertexHash(Renderable renderable, int oID) {
		int length;
		int[] v1;
		int[] v2;
		int[] v3;
		int[] tri1;
		int[] tri2;
		int[] tri3;
		if (renderable instanceof Model) {
			Model m = (Model) renderable;
			length = m.getVerticesCount();
			v1 = m.getVerticesX();
			v2 = m.getVerticesY();
			v3 = m.getVerticesZ();
			tri1 = m.getTrianglesX();
			tri2 = m.getTrianglesY();
			tri3 = m.getTrianglesZ();
		} else {
			if (renderable != null && renderable.getModel() != null) {
				Model m = renderable.getModel();
				length = m.getVerticesCount();
				v1 = m.getVerticesX();
				v2 = m.getVerticesY();
				v3 = m.getVerticesZ();
				tri1 = m.getTrianglesX();
				tri2 = m.getTrianglesY();
				tri3 = m.getTrianglesZ();
			} else {
				return 0;
			}
		}
		int rv = FNV_32_INIT;
		for (int i = 0; i < length; i++) {
			rv ^= v1[i];
			rv *= FNV_32_PRIME;
			rv ^= v2[i];
			rv *= FNV_32_PRIME;
			rv ^= v3[i];
			rv *= FNV_32_PRIME;
		}
		for (int i = 0; i < tri1.length; i++) {
			rv ^= tri1[i];
			rv *= FNV_32_PRIME;
			rv ^= tri2[i];
			rv *= FNV_32_PRIME;
			rv ^= tri3[i];
			rv *= FNV_32_PRIME;
		}
		rv ^= oID;
		rv *= FNV_32_PRIME;
		return rv;
	}

	public void exportTextTile() throws  IOException {
		int camPitch = client.getCameraPitch();
		int camYaw = client.getCameraYaw();
		int zoom = client.getScale();
		int vpHeight = client.getViewportHeight();
		int vpWidth = client.getViewportWidth();
		log.info("Yaw " + camYaw + " | Pitch " + camPitch + " | Zoom " + zoom + " | VP Height " + vpHeight + " | VP Width " + vpWidth);
		Scene scene = client.getScene();
		Tile tile = lastTileOnMenuOpen;
		int regionID = tile.getWorldLocation().getRegionID();
		WorldPoint wp = tile.getWorldLocation();
		final int regionX = wp.getX() >> 6;
		final int regionY = wp.getY() >> 6;
		int sx = tile.getSceneLocation().getX();
		int sy = tile.getSceneLocation().getY();
		Tile[][][] tiles = scene.getTiles();
		int[][][] heights = client.getTileHeights();
		HashSet<GameObject> goHash = new HashSet<>();
		for (int z = 0; z < tiles.length; ++z) {
			tile = tiles[z][sx][sy];
			if(tile == null){continue;}
			int renderZ = tile.getRenderLevel();
			int swHeight = heights[renderZ][sx][sy];
			int seHeight = heights[renderZ][sx + 1][sy];
			int neHeight = heights[renderZ][sx + 1][sy + 1];
			int nwHeight = heights[renderZ][sx][sy + 1];
			double avgHeight = (swHeight + seHeight + neHeight + nwHeight) / 4.0;
			log.info(String.format("Height: %s | Layer %s", avgHeight, renderZ));
			ExportTextTile(tile, regionID, regionX, regionY, z, avgHeight, false, goHash, null, null);
			if (tile.getBridge() != null) {
				renderZ = tile.getBridge().getRenderLevel();
				swHeight = heights[renderZ][sx][sy];
				seHeight = heights[renderZ][sx + 1][sy];
				neHeight = heights[renderZ][sx + 1][sy + 1];
				nwHeight = heights[renderZ][sx][sy + 1];
				avgHeight = (swHeight + seHeight + neHeight + nwHeight) / 4.0;
				log.info(String.format("Bridge Height: %s | Layer %s", avgHeight, renderZ));
				ExportTextTile(tile.getBridge(), regionID, regionX, regionY, z, avgHeight,false, goHash, null, null);
			}
		}
	}

	public void exportTextRegion() throws  IOException {
		Scene scene = client.getScene();
		int regionID = lastTileOnMenuOpen.getWorldLocation().getRegionID();
		WorldPoint wp = lastTileOnMenuOpen.getWorldLocation();
		final int regionX = wp.getX() >> 6;
		final int regionY = wp.getY() >> 6;
		Tile[][][] tiles = scene.getTiles();
		int[][][] heights = client.getTileHeights();
		String path = RuneLite.RUNELITE_DIR + "//models//";
		StringBuilder mapSb = new StringBuilder();
		HashSet<GameObject> goHash = new HashSet<>();
		for (int z = 0; z < tiles.length; ++z) {
			StringBuilder sb = new StringBuilder();
			for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
				for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
					Tile tile = tiles[z][x][y];
					if(tile == null){continue;}
					int renderZ = tile.getRenderLevel();
					int swHeight = heights[renderZ][x][y];
					int seHeight = heights[renderZ][x + 1][y];
					int neHeight = heights[renderZ][x + 1][y + 1];
					int nwHeight = heights[renderZ][x][y + 1];
					double avgHeight = (swHeight + seHeight + neHeight + nwHeight) / 4.0;
					ExportTextTile(tile, regionID, regionX, regionY, z, avgHeight, true, goHash, mapSb, sb);
					if (tile.getBridge() != null) {
						renderZ = tile.getBridge().getRenderLevel();
						swHeight = heights[renderZ][x][y];
						seHeight = heights[renderZ][x + 1][y];
						neHeight = heights[renderZ][x + 1][y + 1];
						nwHeight = heights[renderZ][x][y + 1];
						avgHeight = (swHeight + seHeight + neHeight + nwHeight) / 4.0;
						ExportTextTile(tile.getBridge(), regionID, regionX, regionY, z, avgHeight,true, goHash, mapSb, sb);
					}
				}
			}

			String name = "ObjMap_" + regionX +  "_" + regionY + "_" + z + ".txt";
			File modelOutput = new File(path + name);
			FileWriter modelWriter = new FileWriter(modelOutput);
			modelWriter.write(sb.toString());
			modelWriter.flush();
			modelWriter.close();
		}

		ExportVertexScene(mapSb, regionX, regionY, tiles, heights);
		ExportNavMeshData(regionID, regionX,regionY, tiles);

		String mapName = "Map_" + regionX +  "_" + regionY + ".txt";
		File mapOutput = new File(path + mapName);
		FileWriter stringWriter = new FileWriter(mapOutput);
		stringWriter.write(mapSb.toString());
		stringWriter.flush();
		stringWriter.close();
	}

	public  void exportTargetModel(String menuTarget, int id) throws  IOException {
		if (id < 0 || id >= client.getCachedNPCs().length) {
			return;
		}
		NPC npc = client.getCachedNPCs()[id];
		if (npc == null) {
			return;
		}

		String composition = "";
		for (String co : npc.getComposition().getActions()) {
			composition += co + "_";
		}
		composition += npc.getComposition().getId();
		log.info(npc.getName() + " | " + Text.removeFormattingTags(menuTarget));
		String nameFormat = String.format("NPC_%s_%d", npc.getName().replace(" ", "-"), npc.getId());
		TryExportTextFile(nameFormat, composition.replace(" ", "-"), npc.getModel());
	}

	public void exportAllTextures() throws IOException{
		Texture[] textures = client.getTextureProvider().getTextures();
		for (int i = 0; i < textures.length; ++i) {
			if(textures[i] == null){
				continue;
			}
			int[] pixels = textures[i].getPixels();
			if (pixels == null) {
				continue;
			}
			int textureSize = (int) Math.sqrt(pixels.length);
			log.info("Texture Size: " + textureSize);
			BufferedImage texture = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_RGB);
			for (int x = 0; x < textureSize; ++x) {
				for (int y = 0; y < textureSize; ++y) {
					Color c = new Color(JagexColor.HSLtoRGB((short)pixels[y * textureSize + x], JagexColor.BRIGTHNESS_MIN));
					texture.setRGB(x, textureSize - y - 1, JagexColor.adjustForBrightness(pixels[y * textureSize + x], JagexColor.BRIGTHNESS_MIN));
				}
			}
			File imageFile = new File(RuneLite.RUNELITE_DIR + "//models//Textures//", "Texture_" + i + ".png");
			ImageIO.write(texture, "png", imageFile);
		}
	}

	private void ExportTextTile(Tile tile, int regionID, int regionX, int regionY, int z, double tileHeight, boolean isRegion,
								HashSet<GameObject> goHash,
								StringBuilder mapSb, StringBuilder sb) throws IOException
	{
		if (tile.getWorldLocation().getRegionID() == regionID) {
			log.info(tile.getRenderLevel() + " " + tile.getPlane());
			String tileFormat = String.format("%d,%d_%d,%d,%d,%s_", regionX, regionY, tile.getWorldLocation().getRegionX(), tile.getWorldLocation().getRegionY(), z, tileHeight);
			ExportWallObject(tile, tileFormat, isRegion, mapSb, sb);
			ExportDecorativeObject(tile, tileFormat, isRegion, mapSb, sb);
			ExportGroundObject(tile,tileFormat,isRegion,mapSb,sb);
			ExportGameObjects(tile, tileFormat, z, goHash, isRegion, mapSb, sb);
		}
	}

	private void ExportWallObject(Tile tile, String tileFormat, boolean isRegion, StringBuilder mapSb, StringBuilder sb) throws IOException {
		if (tile.getWallObject() != null) {
			ObjectComposition wComp = client.getObjectDefinition(tile.getWallObject().getId());
			String composition = "";
			for (String co : wComp.getActions()) {
				composition += co + "_";
			}
			composition += wComp.getId();
			String name = wComp.getName();
			if (name == null || name.contains("null")) {
				name = "" + tile.getWallObject().getId();
			} else {
				name = name + "_" + tile.getWallObject().getId();
			}
			String objectTypeFormat1 = String.format("Wall1,%d_", tile.getWallObject().getOrientationA());
			String objectTypeFormat2 = String.format("Wall2,%d_", tile.getWallObject().getOrientationB());
			if (isRegion) {
				boolean onlyMap1 = false;
				boolean onlyMap2 = false;
				TryExportTextRegion(mapSb, sb, tileFormat + objectTypeFormat1 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getWallObject().getRenderable1(), onlyMap1);
				TryExportTextRegion(mapSb, sb, tileFormat + objectTypeFormat2 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getWallObject().getRenderable2(), onlyMap2);
			} else {
				TryExportTextFile(tileFormat + objectTypeFormat1 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getWallObject().getRenderable1());
				TryExportTextFile(tileFormat + objectTypeFormat2 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getWallObject().getRenderable2());
			}
		}
	}

	private  void ExportDecorativeObject(Tile tile, String tileFormat, boolean isRegion, StringBuilder mapSb, StringBuilder sb) throws IOException{
		if (tile.getDecorativeObject() != null) {
			ObjectComposition dComp = client.getObjectDefinition(tile.getDecorativeObject().getId());
			String composition = "";
			for (String co : dComp.getActions()) {
				composition += co + "_";
			}
			composition += dComp.getId();
			String name = dComp.getName();
			if (name == null || name.contains("null")) {
				name = "" + tile.getDecorativeObject().getId();
			}
			else{
				name = name + "_" + tile.getDecorativeObject().getId();
			}
			String objectTypeFormat1 = String.format("Decorative1,%d,%d_", tile.getDecorativeObject().getXOffset(), tile.getDecorativeObject().getYOffset());
			String objectTypeFormat2 = String.format("Decorative2,%d,%d_", tile.getDecorativeObject().getXOffset(), tile.getDecorativeObject().getYOffset());
			if(isRegion) {
				boolean onlyMap1 = false;
				boolean onlyMap2 = false;
				TryExportTextRegion(mapSb, sb, tileFormat + objectTypeFormat1 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getDecorativeObject().getRenderable(), onlyMap1);
				TryExportTextRegion(mapSb, sb, tileFormat + objectTypeFormat2 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getDecorativeObject().getRenderable2(), onlyMap2);
			}else{
				TryExportTextFile(tileFormat + objectTypeFormat1 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getDecorativeObject().getRenderable());
				TryExportTextFile(tileFormat + objectTypeFormat2 + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getDecorativeObject().getRenderable2());
			}
		}
	}

	private void ExportGroundObject(Tile tile, String tileFormat, boolean isRegion, StringBuilder mapSb, StringBuilder sb) throws IOException{
		if (tile.getGroundObject() != null) {
			log.info("Object is on plane (Z): " + tile.getGroundObject().getWorldLocation().getPlane());
			log.info("Object is on offset plane (Z): " + tile.getGroundObject().getWorldLocation().dz(0));
			ObjectComposition gComp = client.getObjectDefinition(tile.getGroundObject().getId());
			String composition = "";
			for (String co : gComp.getActions()) {
				composition += co + "_";
			}
			composition += gComp.getId();
			String name = gComp.getName();
			if (name == null || name.contains("null")) {
				name = "" + tile.getGroundObject().getId();
			}
			else{
				name = name + "_" + tile.getGroundObject().getId();
			}
			String objectTypeFormat = String.format("Ground_");
			if(isRegion) {
				boolean onlyMap = false;
				TryExportTextRegion(mapSb, sb, tileFormat + objectTypeFormat + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getGroundObject().getRenderable(), onlyMap);
			}else{
				TryExportTextFile(tileFormat + objectTypeFormat + name.replace(" ", "-"), composition.replace(" ", "-"), tile.getGroundObject().getRenderable());
			}
		}
	}

	private void ExportGameObjects(Tile tile, String tileFormat, int z, HashSet<GameObject> goHash, boolean isRegion, StringBuilder mapSb, StringBuilder sb) throws IOException{
		if (tile.getGameObjects() != null) {
			for (GameObject go : tile.getGameObjects()) {
				if (go == null || goHash.contains(go)) {
					continue;
				}
				goHash.add(go);
				ObjectComposition goComp = client.getObjectDefinition(go.getId());
				String composition = "";
				for (String co : goComp.getActions()) {
					composition += co + "_";
				}
				composition += goComp.getId();
				String name = Text.removeFormattingTags(goComp.getName());
				if (name.contains("null")) {
					name = "" + go.getId();
				}
				else{
					name = name + "_" + go.getId();
				}
				int tileHeight = Perspective.getTileHeight(client, go.getLocalLocation(), go.getPlane());
				WorldPoint wpMin = WorldPoint.fromScene(client, go.getSceneMinLocation().getX(),go.getSceneMinLocation().getY(),z);
				WorldPoint wpMax = WorldPoint.fromScene(client, go.getSceneMaxLocation().getX(),go.getSceneMaxLocation().getY(),z);

				String objectTypeFormat = String.format("GameObject,%d,%d,%d,%d,%d_",
						wpMin.getRegionX(),
						wpMin.getRegionY(),
						wpMax.getRegionX(),
						wpMax.getRegionY(),
						tileHeight);
				if(isRegion) {
					boolean onlyMap = false;
					TryExportTextRegion(mapSb, sb, tileFormat + objectTypeFormat + name.replace(" ", "-"), composition.replace(" ", "-"), go.getRenderable(), onlyMap);
				}else {
					TryExportTextFile(tileFormat + objectTypeFormat + name.replace(" ", "-"), composition.replace(" ", "-"), go.getRenderable());
				}
			}
		}
	}

	private void ExportVertexScene(StringBuilder mapSb, int regionX, int regionY, Tile[][][] tiles, int[][][] tileHeights) throws IOException {
		int regionID = lastTileOnMenuOpen.getWorldLocation().getRegionID();
		String path = RuneLite.RUNELITE_DIR + "//models//";
		CollisionData[] collisionData = client.getCollisionMaps();
		for (int z = 0; z < tiles.length; ++z) {
			StringBuilder groupSB = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			StringBuilder uvb = new StringBuilder();
			StringBuilder fb = new StringBuilder();
			StringBuilder cb = new StringBuilder();
			StringBuilder Bridgesb = new StringBuilder();
			StringBuilder Bridgeuvb = new StringBuilder();
			StringBuilder Bridgefb = new StringBuilder();
			StringBuilder Bridgecb = new StringBuilder();
			StringBuilder Scenesb = new StringBuilder();
			StringBuilder Sceneuvb = new StringBuilder();
			StringBuilder Scenefb = new StringBuilder();
			StringBuilder Scenecb = new StringBuilder();
			StringBuilder BridgeScenesb = new StringBuilder();
			StringBuilder BridgeSceneuvb = new StringBuilder();
			StringBuilder BridgeScenefb = new StringBuilder();
			StringBuilder BridgeScenecb = new StringBuilder();
			StringBuilder NavMeshsb = new StringBuilder();
			StringBuilder NavMeshuvb = new StringBuilder();
			StringBuilder NavMeshfb = new StringBuilder();
			int xFirst = -1;
			int yFirst = -1;
			int xnFirst = -1;
			int ynFirst = -1;
			int xbFirst = -1;
			int ybFirst = -1;
			int xSFirst = -1;
			int ySFirst = -1;
			int xbSFirst = -1;
			int ybSFirst = -1;
			int lastTri = 1;
			int lastNTri = 1;
			int lastBTri = 1;
			int lastSTri = 1;
			int lastBSTri = 1;
			double offSet = 0.00390625 * 2/8;
			double step = 0.0078125 * 2/8;
			int uvMax = 2047;
			int u = 0;
			int v = 0;
			int su = 0;
			int sv = 0;
			int wf = 0;
			int[][] collisionDataFlags = collisionData[z].getFlags();
			for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
				for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
					Tile tile = tiles[z][x][y];
					// Plane
					if (tile != null && tile.getWorldLocation().getRegionID() == regionID && tile.getSceneTilePaint() != null) {
						SceneTilePaint paint = tile.getSceneTilePaint();
						if (xFirst == -1 && yFirst == -1) {
							String name = String.format("%d,%d_%d,%d,%d,0_Plane,%d_ID_%d", regionX, regionY, x, y, z, regionID, z);
							sb.append("nm " + name).append("\n");
							mapSb.append(name).append("\n");
							xFirst = x - tile.getWorldLocation().getRegionX();//x;
							yFirst = y - tile.getWorldLocation().getRegionY();//y;
						}

						int renderZ = tile.getRenderLevel();
						int swHeight = tileHeights[renderZ][x][y];
						int seHeight = tileHeights[renderZ][x + 1][y];
						int neHeight = tileHeights[renderZ][x + 1][y + 1];
						int nwHeight = tileHeights[renderZ][x][y + 1];

						final int neColor = paint.getNeColor();
						final int nwColor = paint.getNwColor();
						final int seColor = paint.getSeColor();
						final int swColor = paint.getSwColor();
						final int texture = paint.getTexture();


						if (neColor != 12345678) {
							// I can use this if I want to visualize the navmesh.

							// 0,0 - 0
							float vertexDx = (x * 1.28f - xFirst * 1.28f) * 100;
							float vertexDy = (y * 1.28f - yFirst * 1.28f) * 100;
							float vertexDz = swHeight;
							Color c0 = new Color(JagexColor.HSLtoRGB((short) swColor, JagexColor.BRIGTHNESS_MIN));
							float r0 = c0.getRed() / 255.0f;
							float g0 = c0.getGreen() / 255.0f;
							float b0 = c0.getBlue() / 255.0f;

							// 1,0 - 1
							float vertexCx = (x * 1.28f + 1.28f - xFirst * 1.28f) * 100;
							float vertexCy = (y * 1.28f - yFirst * 1.28f) * 100;
							float vertexCz = seHeight;
							Color c1 = new Color(JagexColor.HSLtoRGB((short) seColor, JagexColor.BRIGTHNESS_MIN));
							float r1 = c1.getRed() / 255.0f;
							float g1 = c1.getGreen() / 255.0f;
							float b1 = c1.getBlue() / 255.0f;

							// 1,1 - 3
							float vertexAx = (x * 1.28f + 1.28f - xFirst * 1.28f) * 100;
							float vertexAy = (y * 1.28f + 1.28f - yFirst * 1.28f) * 100;
							float vertexAz = neHeight;
							Color c2 = new Color(JagexColor.HSLtoRGB((short) neColor, JagexColor.BRIGTHNESS_MIN));
							float r2 = c2.getRed() / 255.0f;
							float g2 = c2.getGreen() / 255.0f;
							float b2 = c2.getBlue() / 255.0f;

							// 0,1 - 2
							float vertexBx = (x * 1.28f - xFirst * 1.28f) * 100;
							float vertexBy = (y * 1.28f + 1.28f - yFirst * 1.28f) * 100;
							float vertexBz = nwHeight;
							Color c3 = new Color(JagexColor.HSLtoRGB((short) nwColor, JagexColor.BRIGTHNESS_MIN));
							float r3 = c3.getRed() / 255.0f;
							float g3 = c3.getGreen() / 255.0f;
							float b3 = c3.getBlue() / 255.0f;


							sb.append(String.format("vt %s %s %s\n", vertexDx, -vertexDz, -vertexDy)); // 0
							sb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
							sb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2

							sb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
							sb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
							sb.append(String.format("vt %s %s %s\n", vertexAx, -vertexAz, -vertexAy)); // 3

							if (texture == -1) {
								uvb.append(String.format("uv 0.0 0.0\n"));
								uvb.append(String.format("uv 0.0 0.0\n"));
								uvb.append(String.format("uv 0.0 0.0\n"));

								uvb.append(String.format("uv 0.0 0.0\n"));
								uvb.append(String.format("uv 0.0 0.0\n"));
								uvb.append(String.format("uv 0.0 0.0\n"));
							} else {
								double textureX = texture - 16.0 * Math.floor(texture / 16.0);
								double textureY = Math.floor(texture / 16.0);

								double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));

								double uvx1 = (1.0 + (textureX * -0.0625));
								double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));

								double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy2 = (1.0 + (textureY * -0.0625));

								double uvx3 = (1.0 + (textureX * -0.0625));
								double uvy3 = (1.0 + (textureY * -0.0625));

								String uvMap0 = String.format("uv %s %s\n", uvx0, uvy0);
								String uvMap1 = String.format("uv %s %s\n", uvx1, uvy1);
								String uvMap2 = String.format("uv %s %s\n", uvx2, uvy2);
								String uvMap3 = String.format("uv %s %s\n", uvx3, uvy3);

								uvb.append(String.format(uvMap0));
								uvb.append(String.format(uvMap1));
								uvb.append(String.format(uvMap2));

								uvb.append(String.format(uvMap2));
								uvb.append(String.format(uvMap1));
								uvb.append(String.format(uvMap3));
							}

							fb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									texture, 0,
									lastTri, lastTri, FormatVertexColor(swColor),
									lastTri + 1, lastTri + 1, FormatVertexColor(seColor),
									lastTri + 2, lastTri + 2, FormatVertexColor(nwColor)));
							lastTri += 3;
							fb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									texture, 0,
									lastTri, lastTri, FormatVertexColor(nwColor),
									lastTri + 1, lastTri + 1, FormatVertexColor(seColor),
									lastTri + 2, lastTri + 2, FormatVertexColor(neColor)));
							lastTri += 3;


							if (texture == -1) {
								// c0, c1, c2
								double r012 = Math.sqrt((r0 * r0 + r1 * r1 + r2 * r2) / 3);
								double g012 = Math.sqrt((g0 * g0 + g1 * g1 + g2 * g2) / 3);
								double b012 = Math.sqrt((b0 * b0 + b1 * b1 + b2 * b2) / 3);
								// c2, c1, c3
								double r213 = Math.sqrt((r2 * r2 + r1 * r1 + r3 * r3) / 3);
								double g213 = Math.sqrt((g2 * g2 + g1 * g1 + g3 * g3) / 3);
								double b213 = Math.sqrt((b2 * b2 + b1 * b1 + b3 * b3) / 3);

								double r = Math.sqrt((r213 * r213 + r012 * g012) / 2);
								double g = Math.sqrt((g012 * g012 + g213 * g213) / 2);
								double b = Math.sqrt((b012 * b012 + b213 * b213) / 2);

								cb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
								cb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
							}
						}

					}
					// Bridge Plane
					if (tile != null && tile.getWorldLocation().getRegionID() == regionID && tile.getBridge() != null && tile.getBridge().getSceneTilePaint() != null) {
						Tile bridgeTile = tile.getBridge();
						SceneTilePaint paint = tile.getBridge().getSceneTilePaint();
						if (xbFirst == -1 && ybFirst == -1) {
							String name = String.format("%d,%d_%d,%d,%d,0_BridgePlane,%d_ID_%d", regionX, regionY, x, y, z, regionID, z);
							Bridgesb.append("nm " + name).append("\n");
							mapSb.append(name).append("\n");
							xbFirst = x - bridgeTile.getWorldLocation().getRegionX();//x;
							ybFirst = y - bridgeTile.getWorldLocation().getRegionY();//y;
						}

						int renderZ = bridgeTile.getRenderLevel();
						int swHeight = tileHeights[renderZ][x][y];
						int seHeight = tileHeights[renderZ][x + 1][y];
						int neHeight = tileHeights[renderZ][x + 1][y + 1];
						int nwHeight = tileHeights[renderZ][x][y + 1];

						final int neColor = paint.getNeColor();
						final int nwColor = paint.getNwColor();
						final int seColor = paint.getSeColor();
						final int swColor = paint.getSwColor();
						final int texture = paint.getTexture();


						if (neColor != 12345678) {
							// I can use this if I want to visualize the navmesh.


							// 0,0 - 0
							float vertexDx = (x * 1.28f - xbFirst * 1.28f) * 100;
							float vertexDy = (y * 1.28f - ybFirst * 1.28f) * 100;
							float vertexDz = swHeight;
							Color c0 = new Color(JagexColor.HSLtoRGB((short) swColor, JagexColor.BRIGTHNESS_MIN));
							float r0 = c0.getRed() / 255.0f;
							float g0 = c0.getGreen() / 255.0f;
							float b0 = c0.getBlue() / 255.0f;

							// 1,0 - 1
							float vertexCx = (x * 1.28f + 1.28f - xbFirst * 1.28f) * 100;
							float vertexCy = (y * 1.28f - ybFirst * 1.28f) * 100;
							float vertexCz = seHeight;
							Color c1 = new Color(JagexColor.HSLtoRGB((short) seColor, JagexColor.BRIGTHNESS_MIN));
							float r1 = c1.getRed() / 255.0f;
							float g1 = c1.getGreen() / 255.0f;
							float b1 = c1.getBlue() / 255.0f;

							// 1,1 - 3
							float vertexAx = (x * 1.28f + 1.28f - xbFirst * 1.28f) * 100;
							float vertexAy = (y * 1.28f + 1.28f - ybFirst * 1.28f) * 100;
							float vertexAz = neHeight;
							Color c2 = new Color(JagexColor.HSLtoRGB((short) neColor, JagexColor.BRIGTHNESS_MIN));
							float r2 = c2.getRed() / 255.0f;
							float g2 = c2.getGreen() / 255.0f;
							float b2 = c2.getBlue() / 255.0f;

							// 0,1 - 2
							float vertexBx = (x * 1.28f - xbFirst * 1.28f) * 100;
							float vertexBy = (y * 1.28f + 1.28f - ybFirst * 1.28f) * 100;
							float vertexBz = nwHeight;
							Color c3 = new Color(JagexColor.HSLtoRGB((short) nwColor, JagexColor.BRIGTHNESS_MIN));
							float r3 = c3.getRed() / 255.0f;
							float g3 = c3.getGreen() / 255.0f;
							float b3 = c3.getBlue() / 255.0f;


							Bridgesb.append(String.format("vt %s %s %s\n", vertexDx, -vertexDz, -vertexDy)); // 0
							Bridgesb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
							Bridgesb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2

							Bridgesb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
							Bridgesb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
							Bridgesb.append(String.format("vt %s %s %s\n", vertexAx, -vertexAz, -vertexAy)); // 3

							if (texture == -1) {
								Bridgeuvb.append(String.format("uv 0.0 0.0\n"));
								Bridgeuvb.append(String.format("uv 0.0 0.0\n"));
								Bridgeuvb.append(String.format("uv 0.0 0.0\n"));

								Bridgeuvb.append(String.format("uv 0.0 0.0\n"));
								Bridgeuvb.append(String.format("uv 0.0 0.0\n"));
								Bridgeuvb.append(String.format("uv 0.0 0.0\n"));
							} else {
								double textureX = texture - 16.0 * Math.floor(texture / 16.0);
								double textureY = Math.floor(texture / 16.0);

								double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));

								double uvx1 = (1.0 + (textureX * -0.0625));
								double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));

								double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy2 = (1.0 + (textureY * -0.0625));

								double uvx3 = (1.0 + (textureX * -0.0625));
								double uvy3 = (1.0 + (textureY * -0.0625));

								String uvMap0 = String.format("uv %s %s\n", uvx0, uvy0);
								String uvMap1 = String.format("uv %s %s\n", uvx1, uvy1);
								String uvMap2 = String.format("uv %s %s\n", uvx2, uvy2);
								String uvMap3 = String.format("uv %s %s\n", uvx3, uvy3);

								Bridgeuvb.append(String.format(uvMap0));
								Bridgeuvb.append(String.format(uvMap1));
								Bridgeuvb.append(String.format(uvMap2));

								Bridgeuvb.append(String.format(uvMap2));
								Bridgeuvb.append(String.format(uvMap1));
								Bridgeuvb.append(String.format(uvMap3));
							}

							Bridgefb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									texture, 0,
									lastBTri, lastBTri, FormatVertexColor(swColor),
									lastBTri + 1, lastBTri + 1, FormatVertexColor(seColor),
									lastBTri + 2, lastBTri + 2, FormatVertexColor(nwColor)));
							lastBTri += 3;
							Bridgefb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									texture, 0,
									lastBTri, lastBTri, FormatVertexColor(nwColor),
									lastBTri + 1, lastBTri + 1, FormatVertexColor(seColor),
									lastBTri + 2, lastBTri + 2, FormatVertexColor(neColor)));
							lastBTri += 3;


							if (texture == -1) {
								// c0, c1, c2
								double r012 = Math.sqrt((r0 * r0 + r1 * r1 + r2 * r2) / 3);
								double g012 = Math.sqrt((g0 * g0 + g1 * g1 + g2 * g2) / 3);
								double b012 = Math.sqrt((b0 * b0 + b1 * b1 + b2 * b2) / 3);
								// c2, c1, c3
								double r213 = Math.sqrt((r2 * r2 + r1 * r1 + r3 * r3) / 3);
								double g213 = Math.sqrt((g2 * g2 + g1 * g1 + g3 * g3) / 3);
								double b213 = Math.sqrt((b2 * b2 + b1 * b1 + b3 * b3) / 3);

								double r = Math.sqrt((r213 * r213 + r012 * g012) / 2);
								double g = Math.sqrt((g012 * g012 + g213 * g213) / 2);
								double b = Math.sqrt((b012 * b012 + b213 * b213) / 2);

								Bridgecb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
								Bridgecb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
							}
						}
					}
					// Scene
					if (tile != null && tile.getWorldLocation().getRegionID() == regionID && tile.getSceneTileModel() != null) {
						SceneTileModel sceneTileModel = tile.getSceneTileModel();
						if (xSFirst == -1 && ySFirst == -1) {
							String name = String.format("%d,%d_%d,%d,%d,0_Scene,%d_ID_%d", regionX, regionY, x, y, z, regionID, z);
							Scenesb.append("nm " + name).append("\n");
							mapSb.append(name).append("\n");
							xSFirst = x - tile.getWorldLocation().getRegionX();//x;
							ySFirst = y - tile.getWorldLocation().getRegionY();//y;
						}

						final int[] faceX = sceneTileModel.getFaceX();
						final int[] faceY = sceneTileModel.getFaceY();
						final int[] faceZ = sceneTileModel.getFaceZ();

						final int[] vertexX = sceneTileModel.getVertexX();
						final int[] vertexY = sceneTileModel.getVertexY();
						final int[] vertexZ = sceneTileModel.getVertexZ();

						final int[] triangleColorA = sceneTileModel.getTriangleColorA();
						final int[] triangleColorB = sceneTileModel.getTriangleColorB();
						final int[] triangleColorC = sceneTileModel.getTriangleColorC();

						final int[] triangleTextures = sceneTileModel.getTriangleTextureId();
						final int faceCount = faceX.length;

						int cnt = 0;
						for (int i = 0; i < faceCount; ++i) {
							int texture = -1;
							if (triangleTextures != null) {
								texture = triangleTextures[i];
							}
							final int triangleA = faceX[i];
							final int triangleB = faceY[i];
							final int triangleC = faceZ[i];

							int colorA = triangleColorA[i];
							int colorB = triangleColorB[i];
							int colorC = triangleColorC[i];

							if (colorA == 12345678) {
								continue;
							}

							cnt += 3;

							// vertexes are stored in scene local, convert to tile local
							double vertexXA = vertexX[triangleA] - xSFirst * 1.28 * 100;
							double vertexZA = vertexZ[triangleA] - ySFirst * 1.28 * 100;

							double vertexXB = vertexX[triangleB] - xSFirst * 1.28 * 100;
							double vertexZB = vertexZ[triangleB] - ySFirst * 1.28 * 100;

							double vertexXC = vertexX[triangleC] - xSFirst * 1.28 * 100;
							double vertexZC = vertexZ[triangleC] - ySFirst * 1.28 * 100;

							Scenesb.append(String.format("vt %s %s %s\n", vertexXA, -vertexY[triangleA], -vertexZA)); // 0
							Scenesb.append(String.format("vt %s %s %s\n", vertexXB, -vertexY[triangleB], -vertexZB)); // 1
							Scenesb.append(String.format("vt %s %s %s\n", vertexXC, -vertexY[triangleC], -vertexZC)); // 2


							String uvMap = String.format("uv %s %s\n", offSet + su * step, offSet + sv * step);
							if (texture == -1) {
//								Sceneuvb.append(String.format(uvMap));
//								Sceneuvb.append(String.format(uvMap));
//								Sceneuvb.append(String.format(uvMap));
//									su++;
//									if (su > uvMax / 2 / 2) {
//										su = 0;
//										sv++;
//									}
////								}

								// Non-Textured Vertex Colors do not need UVs.
								Sceneuvb.append(String.format("uv 0.0 0.0\n"));
								Sceneuvb.append(String.format("uv 0.0 0.0\n"));
								Sceneuvb.append(String.format("uv 0.0 0.0\n"));
							} else {
								double textureX = texture - 16.0 * Math.floor(texture / 16.0);
								double textureY = Math.floor(texture / 16.0);

//								double uvxOffset0 = Math.min(0.0625, Math.abs((vertexX[triangleA]-x*128)/128.0));
//								double uvyOffset0 = Math.min(0.0625, Math.abs((vertexZ[triangleA]-y*128)/128.0));
								double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));

//								double uvxOffset1 = Math.max(-0.0625, -Math.abs((vertexX[triangleB]-x*128)/128.0));
//								double uvyOffset1 = Math.min(0.0625, Math.abs((vertexZ[triangleB]-y*128)/128.0));
								double uvx1 = (1.0 + (textureX * -0.0625));
								double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));

//								double uvxOffset2 = Math.min(0.0625, Math.abs((vertexX[triangleC]-x*128)/128.0));
//								double uvyOffset2 = Math.max(-0.0625, -Math.abs((vertexZ[triangleC]-y*128)/128.0));
								double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy2 = (1.0 + (textureY * -0.0625));

								double uvx3 = (1.0 + (textureX * -0.0625));
								double uvy3 = (1.0 + (textureY * -0.0625));

								String uvMap0 = String.format("uv %s %s\n", uvx0, uvy0);
								String uvMap1 = String.format("uv %s %s\n", uvx1, uvy1);
								String uvMap2 = String.format("uv %s %s\n", uvx2, uvy2);
								String uvMap3 = String.format("uv %s %s\n", uvx3, uvy3);

								Sceneuvb.append(String.format(uvMap0));
								Sceneuvb.append(String.format(uvMap1));
								Sceneuvb.append(String.format(uvMap2));
							}

							Scenefb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									texture, 0,
									lastSTri, lastSTri, FormatVertexColor(colorA),
									lastSTri + 1, lastSTri + 1, FormatVertexColor(colorB),
									lastSTri + 2, lastSTri + 2, FormatVertexColor(colorC)));
							lastSTri += 3;

							if (texture == -1) {
								// Not needed for vertex colors or textures.
								Scenecb.append(FormatColor(colorA, colorB, colorC));
							}
						}
					}
					// Bridge Scene
					if (tile != null && tile.getWorldLocation().getRegionID() == regionID && tile.getBridge() != null && tile.getBridge().getSceneTileModel() != null) {
						SceneTileModel sceneTileModel = tile.getBridge().getSceneTileModel();
						if (xbSFirst == -1 && ybSFirst == -1) {
							String name = String.format("%d,%d_%d,%d,%d,0_BridgeScene,%d_ID_%d", regionX, regionY, x, y, z, regionID, z);
							BridgeScenesb.append("nm " + name).append("\n");
							mapSb.append(name).append("\n");
							xbSFirst = x - tile.getBridge().getWorldLocation().getRegionX();//x;
							ybSFirst = y - tile.getBridge().getWorldLocation().getRegionY();//y;
						}

						final int[] faceX = sceneTileModel.getFaceX();
						final int[] faceY = sceneTileModel.getFaceY();
						final int[] faceZ = sceneTileModel.getFaceZ();

						final int[] vertexX = sceneTileModel.getVertexX();
						final int[] vertexY = sceneTileModel.getVertexY();
						final int[] vertexZ = sceneTileModel.getVertexZ();

						final int[] triangleColorA = sceneTileModel.getTriangleColorA();
						final int[] triangleColorB = sceneTileModel.getTriangleColorB();
						final int[] triangleColorC = sceneTileModel.getTriangleColorC();

						final int[] triangleTextures = sceneTileModel.getTriangleTextureId();
						final int faceCount = faceX.length;

						for (int i = 0; i < faceCount; ++i) {
							int texture = -1;
							if (triangleTextures != null) {
								texture = triangleTextures[i];
							}
							final int triangleA = faceX[i];
							final int triangleB = faceY[i];
							final int triangleC = faceZ[i];

							int colorA = triangleColorA[i];
							int colorB = triangleColorB[i];
							int colorC = triangleColorC[i];

							if (colorA == 12345678) {
								continue;
							}

							// vertexes are stored in scene local, convert to tile local
							double vertexXA = vertexX[triangleA] - xbSFirst * 1.28 * 100;
							double vertexZA = vertexZ[triangleA] - ybSFirst * 1.28 * 100;

							double vertexXB = vertexX[triangleB] - xbSFirst * 1.28 * 100;
							double vertexZB = vertexZ[triangleB] - ybSFirst * 1.28 * 100;

							double vertexXC = vertexX[triangleC] - xbSFirst * 1.28 * 100;
							double vertexZC = vertexZ[triangleC] - ybSFirst * 1.28 * 100;

							BridgeScenesb.append(String.format("vt %s %s %s\n", vertexXA, -vertexY[triangleA], -vertexZA)); // 0
							BridgeScenesb.append(String.format("vt %s %s %s\n", vertexXB, -vertexY[triangleB], -vertexZB)); // 1
							BridgeScenesb.append(String.format("vt %s %s %s\n", vertexXC, -vertexY[triangleC], -vertexZC)); // 2


							String uvMap = String.format("uv %s %s\n", offSet + su * step, offSet + sv * step);
							if (texture == -1) {
								// Non-Textured Vertex Colors do not need UVs.
								BridgeSceneuvb.append(String.format("uv 0.0 0.0\n"));
								BridgeSceneuvb.append(String.format("uv 0.0 0.0\n"));
								BridgeSceneuvb.append(String.format("uv 0.0 0.0\n"));
							} else {
								double textureX = texture - 16.0 * Math.floor(texture / 16.0);
								double textureY = Math.floor(texture / 16.0);

//								double uvxOffset0 = Math.min(0.0625, Math.abs((vertexX[triangleA]-x*128)/128.0));
//								double uvyOffset0 = Math.min(0.0625, Math.abs((vertexZ[triangleA]-y*128)/128.0));
								double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));

//								double uvxOffset1 = Math.max(-0.0625, -Math.abs((vertexX[triangleB]-x*128)/128.0));
//								double uvyOffset1 = Math.min(0.0625, Math.abs((vertexZ[triangleB]-y*128)/128.0));
								double uvx1 = (1.0 + (textureX * -0.0625));
								double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));

//								double uvxOffset2 = Math.min(0.0625, Math.abs((vertexX[triangleC]-x*128)/128.0));
//								double uvyOffset2 = Math.max(-0.0625, -Math.abs((vertexZ[triangleC]-y*128)/128.0));
								double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
								double uvy2 = (1.0 + (textureY * -0.0625));

								double uvx3 = (1.0 + (textureX * -0.0625));
								double uvy3 = (1.0 + (textureY * -0.0625));

								String uvMap0 = String.format("uv %s %s\n", uvx0, uvy0);
								String uvMap1 = String.format("uv %s %s\n", uvx1, uvy1);
								String uvMap2 = String.format("uv %s %s\n", uvx2, uvy2);
								String uvMap3 = String.format("uv %s %s\n", uvx3, uvy3);

								BridgeSceneuvb.append(String.format(uvMap0));
								BridgeSceneuvb.append(String.format(uvMap1));
								BridgeSceneuvb.append(String.format(uvMap2));
							}

							BridgeScenefb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									texture, 0,
									lastBSTri, lastBSTri, FormatVertexColor(colorA),
									lastBSTri + 1, lastBSTri + 1, FormatVertexColor(colorB),
									lastBSTri + 2, lastBSTri + 2, FormatVertexColor(colorC)));
							lastBSTri += 3;
						}
					}

					// NavMesh
					WorldPoint wp = WorldPoint.fromScene(client, x, y, z);
					String whiteFormat1 = String.format("0.75,0.75,0.75");
					String whiteFormat2 = String.format("0.65,0.65,0.65");
					String whiteFormat3 = String.format("0.7,0.7,0.7");
					int cdFlag = collisionDataFlags[x][y];
					if((cdFlag & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0){
						whiteFormat1 = String.format("0.75,0.0,0.0");
						whiteFormat2 = String.format("0.65,0.0,0.0");
						whiteFormat3 = String.format("0.7,0.0,0.0");
					}
					if(tile != null) {

						int renderZ = tile.getRenderLevel();
						int swHeight = tileHeights[renderZ][x][y];
						int seHeight = tileHeights[renderZ][x + 1][y];
						int neHeight = tileHeights[renderZ][x + 1][y + 1];
						int nwHeight = tileHeights[renderZ][x][y + 1];
						if (tile.getWorldLocation().getRegionID() == regionID) {
							if (xnFirst == -1 && ynFirst == -1) {
								String name = String.format("%d,%d_%d,%d,%d,0_NavMesh,%d_ID_%d", regionX, regionY, x, y, z, regionID, z);
								NavMeshsb.append("nm " + name).append("\n");
								mapSb.append(name).append("\n");
								xnFirst = x - wp.getRegionX();//x;
								ynFirst = y - wp.getRegionY();//y;
							}

							// 0,0 - 0
							float vertexDx = (x * 1.28f - xnFirst * 1.28f) * 100;
							float vertexDy = (y * 1.28f - ynFirst * 1.28f) * 100;
							float vertexDz = swHeight;

							// 1,0 - 1
							float vertexCx = (x * 1.28f + 1.28f - xnFirst * 1.28f) * 100;
							float vertexCy = (y * 1.28f - ynFirst * 1.28f) * 100;
							float vertexCz = seHeight;

							// 1,1 - 3
							float vertexAx = (x * 1.28f + 1.28f - xnFirst * 1.28f) * 100;
							float vertexAy = (y * 1.28f + 1.28f - ynFirst * 1.28f) * 100;
							float vertexAz = neHeight;

							// 0,1 - 2
							float vertexBx = (x * 1.28f - xnFirst * 1.28f) * 100;
							float vertexBy = (y * 1.28f + 1.28f - ynFirst * 1.28f) * 100;
							float vertexBz = nwHeight;

							NavMeshsb.append(String.format("vt %s %s %s\n", vertexDx, -vertexDz, -vertexDy)); // 0
							NavMeshsb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
							NavMeshsb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2

							NavMeshsb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
							NavMeshsb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
							NavMeshsb.append(String.format("vt %s %s %s\n", vertexAx, -vertexAz, -vertexAy)); // 3

							NavMeshuvb.append(String.format("uv 0.0 0.0\n"));
							NavMeshuvb.append(String.format("uv 0.0 0.0\n"));
							NavMeshuvb.append(String.format("uv 0.0 0.0\n"));

							NavMeshuvb.append(String.format("uv 0.0 0.0\n"));
							NavMeshuvb.append(String.format("uv 0.0 0.0\n"));
							NavMeshuvb.append(String.format("uv 0.0 0.0\n"));

							String whiteFormat = "";
							if (wf == 0) {
								whiteFormat = whiteFormat1;
								wf++;
							} else if (wf == 1) {
								whiteFormat = whiteFormat2;
								wf++;
							} else {
								whiteFormat = whiteFormat3;
								wf = 0;
							}
							NavMeshfb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									-1, 0,
									lastNTri, lastNTri, whiteFormat,
									lastNTri + 1, lastNTri + 1, whiteFormat,
									lastNTri + 2, lastNTri + 2, whiteFormat));
							lastNTri += 3;
							NavMeshfb.append(String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
									-1, 0,
									lastNTri, lastNTri, whiteFormat,
									lastNTri + 1, lastNTri + 1, whiteFormat,
									lastNTri + 2, lastNTri + 2, whiteFormat));
							lastNTri += 3;
						}
					}
				}
			}

			sb.append(uvb.toString());
			sb.append(fb.toString());
			sb.append("ts 2048\n");
			sb.append(cb.toString());
			sb.append("**br**\n");

			Bridgesb.append(Bridgeuvb.toString());
			Bridgesb.append(Bridgefb.toString());
			Bridgesb.append("ts 2048\n");
			Bridgesb.append(Bridgecb.toString());
			Bridgesb.append("**br**\n");

			Scenesb.append(Sceneuvb.toString());
			Scenesb.append(Scenefb.toString());
			Scenesb.append("ts 2048\n");
			Scenesb.append(Scenecb.toString());
			Scenesb.append("**br**\n");

			BridgeScenesb.append(BridgeSceneuvb.toString());
			BridgeScenesb.append(BridgeScenefb.toString());
			BridgeScenesb.append("ts 2048\n");
			BridgeScenesb.append(BridgeScenecb.toString());
			BridgeScenesb.append("**br**\n");

			groupSB.append(sb.toString());
			groupSB.append(Bridgesb.toString());
			groupSB.append(Scenesb.toString());
			groupSB.append(BridgeScenesb.toString());

			NavMeshsb.append(NavMeshuvb.toString());
			NavMeshsb.append(NavMeshfb.toString());
			NavMeshsb.append("ts 2048\n");
			NavMeshsb.append("**br**\n");

			String name = "RegionPlanes_" + regionX + "_" + regionY + "_" + z + ".txt";
			File modelOutput = new File(path + name);
			FileWriter modelWriter = new FileWriter(modelOutput);
			modelWriter.write(groupSB.toString());
			modelWriter.flush();
			modelWriter.close();

			String nmname = "NavMesh_" + regionX + "_" + regionY + "_" + z + ".txt";
			File nmOutput = new File(path + nmname);
			FileWriter nmWriter = new FileWriter(nmOutput);
			nmWriter.write(NavMeshsb.toString());
			nmWriter.flush();
			nmWriter.close();
		}
	}

	private void ExportNavMeshData(int regionID, int regionX, int regionY, Tile[][][] tiles) throws IOException {
		CollisionData[] collisionData = client.getCollisionMaps();
		String path = RuneLite.RUNELITE_DIR + "//models//";
		for (int z = 0; z < tiles.length; ++z) {
			int[][] collisionDataFlags = collisionData[z].getFlags();
			StringBuilder nmSb = new StringBuilder();
			String name = String.format("%d_%d_%d_NavMap", regionX, regionY, z);
			nmSb.append("nm " + name).append("\n");
			for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
				for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
					Tile tile = tiles[z][x][y];
					if(tile == null) { continue; }

					WorldPoint wp = tile.getWorldLocation();
					if(wp.getRegionID() == regionID) {
						int rx = wp.getRegionX();
						int ry = wp.getRegionY();
						int cdFlag = collisionDataFlags[x][y];
						String tileFormat = String.format("%d_%d_%d_%d", rx, ry, z, cdFlag);
						nmSb.append("ti " + tileFormat).append("\n");
					}
				}
			}
			nmSb.append("**br**\n");

			String nmname = "NavMap_" + regionX + "_" + regionY + "_" + z + ".txt";
			File nmOutput = new File(path + nmname);
			FileWriter nmWriter = new FileWriter(nmOutput);
			nmWriter.write(nmSb.toString());
			nmWriter.flush();
			nmWriter.close();
		}
	}

	private void TryExportTextFile(String name, String composition, Renderable renderable) throws  IOException{
		if(renderable instanceof Model){
			ExportAsVertexFile((Model) renderable, name + ".txt", composition, null);
		}
		else {
			if(renderable != null && renderable.getModel() != null){
				ExportAsVertexFile(renderable.getModel(), name + ".txt", composition, null);
			}
		}
	}

	private void TryExportTextRegion(StringBuilder mapSb, StringBuilder sb, String name, String composition, Renderable renderable, boolean onlyMap) throws IOException{
		if(renderable instanceof Model){
			mapSb.append(name).append("\n");
			if(!onlyMap) {
				ExportAsVertexFile((Model) renderable, name, composition, sb);
			}
		}
		else {
			if(renderable != null && renderable.getModel() != null){
				mapSb.append(name).append("\n");
				if(!onlyMap) {
					ExportAsVertexFile(renderable.getModel(), name, composition, sb);
				}
			}
		}
	}

	public void ExportAsVertexFile(Model model, String name, String composition, StringBuilder regionSB) throws IOException
	{
		StringBuilder modelSB = new StringBuilder();
		StringBuilder vertexSB = new StringBuilder();
		StringBuilder uvSB = new StringBuilder();
		StringBuilder triSB = new StringBuilder();
		StringBuilder colorSB = new StringBuilder();

		modelSB.append("nm " + name.replace(".txt","")+"\n");
		modelSB.append("co " + composition + "\n");

		final int triangleCount = model.getTrianglesCount();

		final int vertexCount = model.getVerticesCount();
		final int[] vertexX = model.getVerticesX();
		final int[] vertexY = model.getVerticesY();
		final int[] vertexZ = model.getVerticesZ();

		final int[] trianglesX = model.getTrianglesX();
		final int[] trianglesY = model.getTrianglesY();
		final int[] trianglesZ = model.getTrianglesZ();

		final int[] color1s = model.getFaceColors1();
		final int[] color2s = model.getFaceColors2();
		final int[] color3s = model.getFaceColors3();

		final byte[] transparencies = model.getTriangleTransparencies();
		final short[] faceTextures = model.getFaceTextures();
		final byte[] facePriorities = model.getFaceRenderPriorities();

		float[][] us = model.getFaceTextureUCoordinates();
		float[][] vs = model.getFaceTextureVCoordinates();

		double textureOffSet = 0.00390625;
		double textureStep = 0.0078125;
		int textureUvMax = 255;
		int textureU = 0;
		int textureV = 0;

		double flatOffSet = 0.00390625 * 2;
		double flatStep = 0.0078125 * 2;
		int flatUvMax = 255;
		int flatU = 0;
		int flatV = 0;

		for (int vertex = 0; vertex < vertexCount; ++vertex)
		{
			vertexSB.append(String.format("vt %d %d %d\n", vertexX[vertex], -vertexY[vertex], -vertexZ[vertex]));
		}

		int lastFace = 0;
		for (int face = 0; face < triangleCount; ++face) {
			int color1 = color1s[face];
			int color2 = color2s[face];
			int color3 = color3s[face];

			int triangleA = trianglesX[face];
			int triangleB = trianglesY[face];
			int triangleC = trianglesZ[face];

			short textureID = -1;
			if (faceTextures != null) {
				textureID = faceTextures[face];
			}

			boolean colorBreak = false;
			if (color3 == -1) {
				color2 = color3 = color1;
			}
			else if (color3 == -2) {
				if (faceTextures != null) {

//					if (us != null && us[face] != null && vs != null && vs[face] != null && textureID != -1) {
//						for (int j = 0; j < us[face].length; ++j) {
//							uvSB.append(String.format("fu %d %s %s\n",
//									textureID,
//									us[face][j],
//									vs[face][j]));
//						}
//					} else {
//
//					}
					uvSB.append(String.format("uv 0.0 0.0\n"));
					uvSB.append(String.format("uv 0.0 0.0\n"));
					uvSB.append(String.format("uv 0.0 0.0\n"));
					lastFace++;

//					int packAlphaPriority = packAlphaPriority(faceTextures, transparencies, facePriorities, face);
//					String faceFormat = String.format("tr %d %d/%d/%s %d/%d/%s %d/%d/%s\n",
//							textureID,
//							triangleA + 1, (3 * lastFace + 0) + 1, FormatVertexColor(color1),
//							triangleB + 1, (3 * lastFace + 1) + 1, FormatVertexColor(color2),
//							triangleC + 1, (3 * lastFace + 2) + 1, FormatVertexColor(color3));
//					triSB.append(faceFormat);
//					lastFace++;
				}
				continue;
			}

			if (faceTextures != null) {
				if (us != null && us[face] != null && vs != null && vs[face] != null && textureID != -1) {
					for (int j = 0; j < us[face].length; ++j) {
						uvSB.append(String.format("fu %d %s %s\n",
								textureID,
								us[face][j],
								vs[face][j]));
					}
				} else {
					uvSB.append(String.format("uv 0.0 0.0\n"));
					uvSB.append(String.format("uv 0.0 0.0\n"));
					uvSB.append(String.format("uv 0.0 0.0\n"));
				}
			}

			byte facePriority  = 0;
			if(facePriorities != null) {
				facePriority = facePriorities[face];
			}

			int packAlphaPriority = packAlphaPriority(faceTextures, transparencies, facePriorities, face);
			String faceFormat = String.format("tr %d/%d %d/%d/%s %d/%d/%s %d/%d/%s\n",
					textureID, facePriority,
					triangleA + 1, (3 * lastFace + 0) + 1, FormatVertexColor(packAlphaPriority | color1),
					triangleB + 1, (3 * lastFace + 1) + 1, FormatVertexColor(packAlphaPriority | color2),
					triangleC + 1, (3 * lastFace + 2) + 1, FormatVertexColor(packAlphaPriority | color3));
			triSB.append(faceFormat);

			lastFace++;
		}

		modelSB.append(vertexSB.toString());
		modelSB.append(uvSB.toString());
		if(faceTextures != null) {
			modelSB.append("ts " + 512 + "\n");
		}else{
			modelSB.append("ts " + 256 + "\n");
		}
		modelSB.append(triSB.toString());
		modelSB.append(colorSB.toString());
		modelSB.append("**br**\n");

		if(regionSB != null)
		{
			regionSB.append(modelSB.toString());
		}
		else
		{
			String path = RuneLite.RUNELITE_DIR + "//models//";
			File modelOutput = new File(path + name);
			FileWriter modelWriter = new FileWriter(modelOutput);
			modelWriter.write(modelSB.toString());
			modelWriter.flush();
			modelWriter.close();
		}
	}

	private static int packAlphaPriority(short[] faceTextures, byte[] faceTransparencies, byte[] facePriorities, int face)
	{
		int alpha = 0;
		if (faceTransparencies != null && (faceTextures == null || faceTextures[face] == -1))
		{
			alpha = (faceTransparencies[face] & 0xFF) << 24;
		}
		int priority = 0;
		if (facePriorities != null)
		{
			priority = (facePriorities[face] & 0xff) << 16;
		}
		return alpha | priority;
	}

	private String FormatVertexColor(int color)
	{
		int rgb = JagexColor.HSLtoRGB((short) color, JagexColor.BRIGTHNESS_MIN);
		Color c = new Color(rgb);
		float r = c.getRed() / 255.0f;
		float g = c.getGreen() / 255.0f;
		float b = c.getBlue() / 255.0f;

		String colorFormat = String.format("%.4f,%.4f,%.4f", r, g, b);
		return  colorFormat;
	}

	private String FormatColor(int color1, int color2, int color3)
	{
		int rgb1 = JagexColor.HSLtoRGB((short) color1, JagexColor.BRIGTHNESS_MIN);
		int rgb2 = JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN);
		int rgb3 = JagexColor.HSLtoRGB((short) color3, JagexColor.BRIGTHNESS_MIN);
		Color c1 = new Color(rgb1);
		Color c2 = new Color(rgb2);
		Color c3 = new Color(rgb3);
		float r1 = c1.getRed() / 255.0f;
		float g1 = c1.getGreen() / 255.0f;
		float b1 = c1.getBlue() / 255.0f;
		float r2 = c2.getRed() / 255.0f;
		float g2 = c2.getGreen() / 255.0f;
		float b2 = c2.getBlue() / 255.0f;
		float r3 = c3.getRed() / 255.0f;
		float g3 = c3.getGreen() / 255.0f;
		float b3 = c3.getBlue() / 255.0f;
		double r = Math.sqrt((r1 * r1 + r2 * r2 + r3 * r3) / 3);
		double g = Math.sqrt((g1 * g1 + g2 * g2 + g3 * g3) / 3);
		double b = Math.sqrt((b1 * b1 + b2 * b2 + b3 * b3) / 3);
		String colorFormat = String.format("fc %.4f %.4f %.4f\n", r, g, b);
		return  colorFormat;
	}

	private double GetCenterHeight(Point min, Point max, int renderPlane, int[][][] heights) {
		int minX = min.getX();
		int minY = min.getY();
		int maxX = max.getX();
		int maxY = max.getY();

		int sizeX = maxX - minX;
		int sizeY = maxY - minY;

		boolean isEvenX = sizeX % 2 == 0;
		boolean isEvenY = sizeY % 2 == 0;

		int offSetX = sizeX/2;
		int offSetY = sizeY/2;

		// Even Numbered Sizes
		int swHeight = heights[renderPlane][minX + offSetX][minY + offSetY];
		int seHeight = heights[renderPlane][minX + 1 + offSetX][minY + offSetY];
		int neHeight = heights[renderPlane][minX + 1 + offSetX][minY + 1 + offSetY];
		int nwHeight = heights[renderPlane][minX + offSetX][minY + 1 + offSetY];

		// Odd Numbered Sizes
		int oddHeight = heights[renderPlane][minX + 1 + offSetX][minY + 1 + offSetY];

		// One This is the center of a tile or the neHieght of the min.
		swHeight = heights[renderPlane][minX+1][minY+1];
		seHeight = heights[renderPlane][minX + 1][minY];
		neHeight = heights[renderPlane][minX + 1][minY + 1];
		nwHeight = heights[renderPlane][minX][minY + 1];

		// Two This is the center tile of them.
		swHeight = heights[renderPlane][minX+1][minY+1];
		seHeight = heights[renderPlane][minX + 2][minY+1];
		neHeight = heights[renderPlane][minX + 2][minY + 2];
		nwHeight = heights[renderPlane][minX+1][minY + +2];

		return 0;
	}

}
