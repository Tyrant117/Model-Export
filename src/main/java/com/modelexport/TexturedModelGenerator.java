package com.modelexport;

import net.runelite.api.Model;
import net.runelite.api.TextureProvider;

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
import net.runelite.client.util.Text;

import net.runelite.api.Client;
import net.runelite.api.GameState;

import java.io.IOException;
import java.util.*;

public class TexturedModelGenerator {
    public  class Face{
        short textureID;
        int x,y,z;

        float u1, u2, u3;
        float v1, v2, v3;

        public Face(short textureID, int x, int y, int z){
            this.textureID = textureID;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public  void SetUV1(float u, float v){
            this.u1 = u;
            this.v1 = v;
        }

        public  void SetUV2(float u, float v){
            this.u2 = u;
            this.v2 = v;
        }

        public  void SetUV3(float u, float v){
            this.u3 = u;
            this.v3 = v;
        }

        public boolean Equals(Face face){
            return x == face.x && y == face.y && z == face.z;
        }
    }

    public void GenerateTexture(ModelExportPlugin plugin, Client client, Model model) throws IOException {
        if(model.getFaceTextures() == null){return;}

        Map<Short, List<Face>> similarFaces = new HashMap<>();
        short[] faceTextures = model.getFaceTextures();
        TextureProvider provider = client.getTextureProvider();
        Face[] faces = new Face[model.getTrianglesCount()];

        if(faceTextures.length != model.getTrianglesCount()){
//            plugin.Log("Texture / Triangle Mismatch " + faces.length + " : " + model.getTrianglesCount());
            return;
        }

        for (int face = 0; face < model.getTrianglesCount(); ++face) {
            faces[face] = new Face(faceTextures[face], model.getTrianglesX()[face] + 1,model.getTrianglesY()[face] + 1,model.getTrianglesZ()[face] + 1);
        }

        int textCordX = 1;
        int textCordY = 0;
        int textureOffset = 128;
        double textureUVStep = 0.0625;
        int uvMax = 15;
        for (Face face : faces) {
            if (similarFaces.containsKey(face.textureID)) {
                similarFaces.get(face.textureID).add(face);
            } else {
                similarFaces.put(face.textureID, new ArrayList<>());
                similarFaces.get(face.textureID).add(face);
            }
        }

        float[][] uCords = model.getFaceTextureUCoordinates();
        float[][] vCords = model.getFaceTextureVCoordinates();

        if(faces.length != uCords.length){
//            plugin.Log("Face / UV Mismatch " + faces.length + " : " + uCords.length);
            return;
        }

        for (int i = 0; i < uCords.length; ++i){
            if(uCords[i] == null || vCords[i] == null)
            {
// This is where it needs to target color textures. (0-128,0-128)
            }else{
                // Need to add an offset.
                faces[i].SetUV1(uCords[i][0],vCords[i][0]);
                faces[i].SetUV2(uCords[i][1],vCords[i][1]);
                faces[i].SetUV3(uCords[i][2],vCords[i][2]);
            }
        }
    }
}
