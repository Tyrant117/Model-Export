package com.modelexport;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObsoleteStorage {

//    private void ExportSceneObjects(Tile tile, String tileFormat, boolean isRegion) throws IOException {
//        if (tile.getSceneTileModel() != null) {
//            SceneTileModel model = tile.getSceneTileModel();
//            String name = String.format(tileFormat + "Scene_0.txt");
//            Point tilePoint = tile.getSceneLocation();
//            StringBuilder modelData = new StringBuilder();
//            if (isRegion) {
//
//            } else {
//                modelData.append("nm " + name.replace(".txt","")+"\n");
//                final int[] faceX = model.getFaceX();
//                final int[] faceY = model.getFaceY();
//                final int[] faceZ = model.getFaceZ();
//
//                final int[] vertexX = model.getVertexX();
//                final int[] vertexY = model.getVertexY();
//                final int[] vertexZ = model.getVertexZ();
//
//                final int[] triangleColorA = model.getTriangleColorA();
//                final int[] triangleColorB = model.getTriangleColorB();
//                final int[] triangleColorC = model.getTriangleColorC();
//
//                final int faceCount = faceX.length;
//                int baseX = Perspective.LOCAL_TILE_SIZE * tilePoint.getX();
//                int baseY = Perspective.LOCAL_TILE_SIZE * tilePoint.getY();
//
//                int cnt = 0;
//                for (int i = 0; i < faceCount; ++i){
//                    final int triangleA = faceX[i];
//                    final int triangleB = faceY[i];
//                    final int triangleC = faceZ[i];
//
//                    final int colorA = triangleColorA[i];
//                    final int colorB = triangleColorB[i];
//                    final int colorC = triangleColorC[i];
//
//                    if (colorA == 12345678)
//                    {
//                        continue;
//                    }
//                    cnt += 3;
//
//                    // vertexes are stored in scene local, convert to tile local
//                    int vertexXA = vertexX[triangleA] - baseX;
//                    int vertexZA = vertexZ[triangleA] - baseY;
//
//                    int vertexXB = vertexX[triangleB] - baseX;
//                    int vertexZB = vertexZ[triangleB] - baseY;
//
//                    int vertexXC = vertexX[triangleC] - baseX;
//                    int vertexZC = vertexZ[triangleC] - baseY;
//
//                    modelData.append(String.format("vt %d %d %d\n", vertexXA, vertexY[triangleA], vertexZA));
//                    modelData.append(String.format("vt %d %d %d\n", vertexXB, vertexY[triangleB], vertexZB));
//                    modelData.append(String.format("vt %d %d %d\n", vertexXC, vertexY[triangleC], vertexZC));
//                }
//
//                double offSet = 0.00390625 * 2;
//                double step = 0.0078125 * 2;
//                int uvMax = 255;
//                int u = 0;
//                int v = 0;
//                for (int face = 0; face < faceCount; ++face) {
//                    String uvMap = String.format("uv %s %s\n", offSet + u * step, offSet + v * step);
//                    int color3 = triangleColorA[face];
//                    if(color3 == 12345678){
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                    }else {
//                        modelData.append(uvMap);
//                        modelData.append(uvMap);
//                        modelData.append(uvMap);
//
//                        u++;
//                        if (u > uvMax / 2 / 2) {
//                            u = 0;
//                            v++;
//                        }
//                    }
//                }
//                for (int face = 0; face < faceCount; ++face) {
//                    int x = faceX[face] + 1;
//                    int y = faceY[face] + 1;
//                    int z = faceZ[face] + 1;
//                    int color3 = triangleColorA[face];
//                    if(color3 == 12345678) {
//                        continue;
//                    }
//                    String faceFormat = String.format("tr %d/%d %d/%d %d/%d\n", x, (3 * face + 0) + 1, y, (3 * face + 1) + 1, z, (3 * face + 2) + 1);
//                    modelData.append(faceFormat);
//                }
//
//                // Colors
//                modelData.append("ts " + (uvMax + 1) + "\n");
//                for (int face = 0; face < faceCount; ++face) {
//
//                    int color1 = triangleColorC[face];
//                    int color2 = triangleColorB[face];
//                    int color3 = triangleColorA[face];
//
//                    if (color3 == -1) {
//                        color2 = color3 = color1;
//                    }
//                    else if (color3 == 12345678)
//                    {
//                        continue;
//                    }
//
//                    // c2 is the primary face color, c1 is used as a booster when c3 is flagged as far as I can tell.
//                    // I dont know if we actually want to continue with c3 = -2, but it should draw it black so it will at least be easy to debug.
////				Color c1 = new Color(JagexColor.HSLtoRGB((short)color1, JagexColor.BRIGTHNESS_MIN));
//                    Color c2 = new Color(JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN));
////				Color c3 = new Color(JagexColor.HSLtoRGB((short)color3, JagexColor.BRIGTHNESS_MIN));
//
//                    double r = c2.getRed()  / 255.0;
//                    double g = c2.getGreen() / 255.0;
//                    double b = c2.getBlue() / 255.0;
//
//                    String colorFormat = String.format("fc %.4f %.4f %.4f\n", r, g, b);
//                    modelData.append(colorFormat);
//                }
//
//                modelData.append("**br**\n");
//                String path = RuneLite.RUNELITE_DIR + "//models//";
//                File modelOutput = new File(path + name);
//                FileWriter modelWriter = new FileWriter(modelOutput);
//                modelWriter.write(modelData.toString());
//                modelWriter.flush();
//                modelWriter.close();
//
//            }
//        }
//    }
//
//    private void ExportSceneTile(SceneTileModel sceneTileModel, int xSFirst, int ySFirst, double offSet, int su, int sv, double step, int lastSTri, StringBuilder Scenesb, StringBuilder Sceneuvb, StringBuilder Scenefb, StringBuilder Scenecb){
//        final int[] faceX = sceneTileModel.getFaceX();
//        final int[] faceY = sceneTileModel.getFaceY();
//        final int[] faceZ = sceneTileModel.getFaceZ();
//
//        final int[] vertexX = sceneTileModel.getVertexX();
//        final int[] vertexY = sceneTileModel.getVertexY();
//        final int[] vertexZ = sceneTileModel.getVertexZ();
//
//        final int[] triangleColorA = sceneTileModel.getTriangleColorA();
//        final int[] triangleColorB = sceneTileModel.getTriangleColorB();
//        final int[] triangleColorC = sceneTileModel.getTriangleColorC();
//
//        final int[] triangleTextures = sceneTileModel.getTriangleTextureId();
//        final int faceCount = faceX.length;
//
//        int cnt = 0;
//        for (int i = 0; i < faceCount; ++i)
//        {
//            int texture = -1;
//            if(triangleTextures != null){
//                texture = triangleTextures[i];
//            }
//            final int triangleA = faceX[i];
//            final int triangleB = faceY[i];
//            final int triangleC = faceZ[i];
//
//            int colorA = triangleColorA[i];
//            int colorB = triangleColorB[i];
//            int colorC = triangleColorC[i];
//
//            if (colorA == 12345678)
//            {
//                continue;
//            }
//
//            cnt += 3;
//
//            // vertexes are stored in scene local, convert to tile local
//            double vertexXA = vertexX[triangleA] - xSFirst*1.28*100;
//            double vertexZA = vertexZ[triangleA] - ySFirst*1.28*100;
//
//            double vertexXB = vertexX[triangleB] - xSFirst*1.28*100;
//            double vertexZB = vertexZ[triangleB] - ySFirst*1.28*100;
//
//            double vertexXC = vertexX[triangleC] - xSFirst*1.28*100;
//            double vertexZC = vertexZ[triangleC] - ySFirst*1.28*100;
//
//            Scenesb.append(String.format("vt %s %s %s\n", vertexXA, -vertexY[triangleA], -vertexZA)); // 0
//            Scenesb.append(String.format("vt %s %s %s\n", vertexXB, -vertexY[triangleB], -vertexZB)); // 1
//            Scenesb.append(String.format("vt %s %s %s\n", vertexXC, -vertexY[triangleC], -vertexZC)); // 2
//
//
//            String uvMap = String.format("uv %s %s\n", offSet + su * step, offSet + sv * step);
//            if(texture == -1)
//            {
////								Sceneuvb.append(String.format(uvMap));
////								Sceneuvb.append(String.format(uvMap));
////								Sceneuvb.append(String.format(uvMap));
////									su++;
////									if (su > uvMax / 2 / 2) {
////										su = 0;
////										sv++;
////									}
//////								}
//
//                // Non-Textured Vertex Colors do not need UVs.
//                Sceneuvb.append(String.format("uv 0.0 0.0\n"));
//                Sceneuvb.append(String.format("uv 0.0 0.0\n"));
//                Sceneuvb.append(String.format("uv 0.0 0.0\n"));
//            }
//            else
//            {
//                double textureX = texture - 16.0 * Math.floor(texture / 16.0);
//                double textureY = Math.floor(texture / 16.0);
//
////								double uvxOffset0 = Math.min(0.0625, Math.abs((vertexX[triangleA]-x*128)/128.0));
////								double uvyOffset0 = Math.min(0.0625, Math.abs((vertexZ[triangleA]-y*128)/128.0));
//                double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
//                double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));
//
////								double uvxOffset1 = Math.max(-0.0625, -Math.abs((vertexX[triangleB]-x*128)/128.0));
////								double uvyOffset1 = Math.min(0.0625, Math.abs((vertexZ[triangleB]-y*128)/128.0));
//                double uvx1 = (1.0 + (textureX * -0.0625));
//                double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));
//
////								double uvxOffset2 = Math.min(0.0625, Math.abs((vertexX[triangleC]-x*128)/128.0));
////								double uvyOffset2 = Math.max(-0.0625, -Math.abs((vertexZ[triangleC]-y*128)/128.0));
//                double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
//                double uvy2 = (1.0 + (textureY * -0.0625));
//
//                double uvx3 = (1.0 + (textureX * -0.0625));
//                double uvy3 = (1.0 + (textureY * -0.0625));
//
//                String uvMap0 = String.format("uv %s %s\n",uvx0 ,uvy0);
//                String uvMap1 = String.format("uv %s %s\n",uvx1 ,uvy1);
//                String uvMap2 = String.format("uv %s %s\n",uvx2 ,uvy2);
//                String uvMap3 = String.format("uv %s %s\n",uvx3 ,uvy3);
//
//                Sceneuvb.append(String.format(uvMap0));
//                Sceneuvb.append(String.format(uvMap1));
//                Sceneuvb.append(String.format(uvMap2));
//            }
//
//            Scenefb.append(String.format("tr %d %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                    texture,
//                    lastSTri, lastSTri, FormatVertexColor(colorA),
//                    lastSTri + 1, lastSTri + 1, FormatVertexColor(colorB),
//                    lastSTri + 2, lastSTri + 2, FormatVertexColor(colorC)));
//            lastSTri += 3;
//
//            if(texture == -1)
//            {
//                // Not needed for vertex colors or textures.
//                Scenecb.append(FormatColor(colorA, colorB, colorC));
//            }
//        }
//    }
//
//    private  void ExportSceneTilePaint(Tile tile, int[][][] tileHeights, int x, int y, SceneTilePaint paint, int xFirst, int yFirst, int lastTri, StringBuilder sb, StringBuilder uvb, StringBuilder fb, StringBuilder cb){
//        int renderZ = tile.getRenderLevel();
//        int swHeight = tileHeights[renderZ][x][y];
//        int seHeight = tileHeights[renderZ][x + 1][y];
//        int neHeight = tileHeights[renderZ][x + 1][y + 1];
//        int nwHeight = tileHeights[renderZ][x][y + 1];
//
//        final int neColor = paint.getNeColor();
//        final int nwColor = paint.getNwColor();
//        final int seColor = paint.getSeColor();
//        final int swColor = paint.getSwColor();
//        final int texture = paint.getTexture();
//
//
//        if (neColor == 12345678) {
//            // I can use this if I want to visualize the navmesh.
//            return;
//        }
//
//        // 0,0 - 0
//        float vertexDx = (x * 1.28f - xFirst * 1.28f) * 100;
//        float vertexDy = (y * 1.28f - yFirst * 1.28f) * 100;
//        float vertexDz = swHeight;
//        Color c0 = new Color(JagexColor.HSLtoRGB((short) swColor, JagexColor.BRIGTHNESS_MIN));
//        float r0 = c0.getRed() / 255.0f;
//        float g0 = c0.getGreen() / 255.0f;
//        float b0 = c0.getBlue() / 255.0f;
//
//        // 1,0 - 1
//        float vertexCx = (x * 1.28f + 1.28f - xFirst * 1.28f) * 100;
//        float vertexCy = (y * 1.28f - yFirst * 1.28f) * 100;
//        float vertexCz = seHeight;
//        Color c1 = new Color(JagexColor.HSLtoRGB((short) seColor, JagexColor.BRIGTHNESS_MIN));
//        float r1 = c1.getRed() / 255.0f;
//        float g1 = c1.getGreen() / 255.0f;
//        float b1 = c1.getBlue() / 255.0f;
//
//        // 1,1 - 3
//        float vertexAx = (x * 1.28f + 1.28f - xFirst * 1.28f) * 100;
//        float vertexAy = (y * 1.28f + 1.28f - yFirst * 1.28f) * 100;
//        float vertexAz = neHeight;
//        Color c2 = new Color(JagexColor.HSLtoRGB((short) neColor, JagexColor.BRIGTHNESS_MIN));
//        float r2 = c2.getRed() / 255.0f;
//        float g2 = c2.getGreen() / 255.0f;
//        float b2 = c2.getBlue() / 255.0f;
//
//        // 0,1 - 2
//        float vertexBx = (x * 1.28f - xFirst * 1.28f) * 100;
//        float vertexBy = (y * 1.28f + 1.28f - yFirst * 1.28f) * 100;
//        float vertexBz = nwHeight;
//        Color c3 = new Color(JagexColor.HSLtoRGB((short) nwColor, JagexColor.BRIGTHNESS_MIN));
//        float r3 = c3.getRed() / 255.0f;
//        float g3 = c3.getGreen() / 255.0f;
//        float b3 = c3.getBlue() / 255.0f;
//
//
//        sb.append(String.format("vt %s %s %s\n", vertexDx, -vertexDz, -vertexDy)); // 0
//        sb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
//        sb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
//
//        sb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
//        sb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
//        sb.append(String.format("vt %s %s %s\n", vertexAx, -vertexAz, -vertexAy)); // 3
//
//        if(texture == -1)
//        {
//            uvb.append(String.format("uv 0.0 0.0\n"));
//            uvb.append(String.format("uv 0.0 0.0\n"));
//            uvb.append(String.format("uv 0.0 0.0\n"));
//
//            uvb.append(String.format("uv 0.0 0.0\n"));
//            uvb.append(String.format("uv 0.0 0.0\n"));
//            uvb.append(String.format("uv 0.0 0.0\n"));
//        }
//        else
//        {
//            double textureX = texture - 16.0 * Math.floor(texture / 16.0);
//            double textureY = Math.floor(texture / 16.0);
//
//            double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
//            double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));
//
//            double uvx1 = (1.0 + (textureX * -0.0625));
//            double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));
//
//            double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
//            double uvy2 = (1.0 + (textureY * -0.0625));
//
//            double uvx3 = (1.0 + (textureX * -0.0625));
//            double uvy3 = (1.0 + (textureY * -0.0625));
//
//            String uvMap0 = String.format("uv %s %s\n",uvx0 ,uvy0);
//            String uvMap1 = String.format("uv %s %s\n",uvx1 ,uvy1);
//            String uvMap2 = String.format("uv %s %s\n",uvx2 ,uvy2);
//            String uvMap3 = String.format("uv %s %s\n",uvx3 ,uvy3);
//
//            uvb.append(String.format(uvMap0));
//            uvb.append(String.format(uvMap1));
//            uvb.append(String.format(uvMap2));
//
//            uvb.append(String.format(uvMap2));
//            uvb.append(String.format(uvMap1));
//            uvb.append(String.format(uvMap3));
//        }
//
//        fb.append(String.format("tr %d %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                texture,
//                lastTri, lastTri, FormatVertexColor(swColor),
//                lastTri + 1, lastTri + 1, FormatVertexColor(seColor),
//                lastTri + 2, lastTri + 2, FormatVertexColor(nwColor)));
//        lastTri += 3;
//        fb.append(String.format("tr %d %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                texture,
//                lastTri, lastTri, FormatVertexColor(nwColor),
//                lastTri + 1, lastTri + 1, FormatVertexColor(seColor),
//                lastTri + 2, lastTri + 2, FormatVertexColor(neColor)));
//        lastTri += 3;
//
//
//        if(texture == -1)
//        {
//            // c0, c1, c2
//            double r012 = Math.sqrt((r0 * r0 + r1 * r1 + r2 * r2) / 3);
//            double g012 = Math.sqrt((g0 * g0 + g1 * g1 + g2 * g2) / 3);
//            double b012 = Math.sqrt((b0 * b0 + b1 * b1 + b2 * b2) / 3);
//            // c2, c1, c3
//            double r213 = Math.sqrt((r2 * r2 + r1 * r1 + r3 * r3) / 3);
//            double g213 = Math.sqrt((g2 * g2 + g1 * g1 + g3 * g3) / 3);
//            double b213 = Math.sqrt((b2 * b2 + b1 * b1 + b3 * b3) / 3);
//
//            double r = Math.sqrt((r213 * r213 + r012 * g012) / 2);
//            double g = Math.sqrt((g012 * g012 + g213 * g213) / 2);
//            double b = Math.sqrt((b012 * b012 + b213 * b213) / 2);
//
//            cb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
//            cb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
//        }
//    }
//
//    public void exportTextFile(Model model, String name) throws IOException {
//        StringBuilder modelData = new StringBuilder();
//        modelData.append("nm " + name.replace(".txt","")+"\n");
//        final int[] color1s = model.getFaceColors1();
//        final int[] color2s = model.getFaceColors2();
//        final int[] color3s = model.getFaceColors3();
//
////		final byte[] colorAlpha = model.getTriangleTransparencies();
////		if(colorAlpha != null){ log.info("HAS ALPHA FACES " + colorAlpha.length);}
////		int faceRenderPriority = 0;
////		if(model.getFaceRenderPriorities() != null){
////			faceRenderPriority = model.getFaceRenderPriorities().length;
////		}
//
//        for (int i = 0; i < model.getVerticesCount(); ++i) {
//            int vx = model.getVerticesX()[i];
//            int vy = model.getVerticesY()[i] * -1;
//            int vz = model.getVerticesZ()[i] * -1;
//
//            modelData.append(String.format("vt %d %d %d\n", vx, vy, vz));
//        }
//
//        if(model.getFaceTextures() != null) {
//
//            short[] faceTextures = model.getFaceTextures();
//            double offSet = 0.00390625;
//            double step = 0.0078125;
//            int uvMax = 255;
//            int u = 0;
//            int v = 0;
//            log.info(String.format("U: %d | V: %d | Tris: %d",
//                    model.getFaceTextureUCoordinates().length,
//                    model.getFaceTextureVCoordinates().length,
//                    model.getTrianglesCount()));
//
//            boolean[] createColor = new boolean[model.getTrianglesCount()];
//            for (int i = 0; i < model.getFaceTextureUCoordinates().length; ++i) {
//                short textureID = faceTextures[i];
//                if (model.getFaceTextureUCoordinates()[i] == null || model.getFaceTextureVCoordinates()[i] == null || textureID == -1) {
//                    String uvMap = String.format("uv %s %s\n", (offSet + u * step), (offSet + v * step));
//                    int color3 = color3s[i];
//                    if(color3 == -2){
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        createColor[i] = false;
//                    }else {
//                        modelData.append(uvMap);
//                        modelData.append(uvMap);
//                        modelData.append(uvMap);
//                        createColor[i] = true;
//                        u++;
//                        if (u > uvMax / 2 / 2) {
//                            u = 0;
//                            v++;
//                        }
//                    }
//                } else {
//                    for (int j = 0; j < model.getFaceTextureUCoordinates()[i].length; ++j) {
//                        modelData.append(String.format("fu %d %s %s\n", textureID, model.getFaceTextureUCoordinates()[i][j],
//                                model.getFaceTextureVCoordinates()[i][j]));
//                    }
//                    createColor[i] = false;
//                }
//            }
//
//            modelData.append("ts " + 512 + "\n");
//            for (int face = 0; face < model.getTrianglesCount(); ++face) {
//                int x = model.getTrianglesX()[face] + 1;
//                int y = model.getTrianglesY()[face] + 1;
//                int z = model.getTrianglesZ()[face] + 1;
//
//                if(createColor[face]) {
//                    int color3 = color3s[face];
//                    if (color3 == -2) {
//                        continue;
//                    }
//                }
//                String faceFormat = String.format("tr %d/%d %d/%d %d/%d\n", x, (3 * face + 0) + 1, y, (3 * face + 1) + 1, z, (3 * face + 2) + 1);
//                modelData.append(faceFormat);
//            }
//
//            for (int face = 0; face < model.getTrianglesCount(); ++face){
//                int color1 = color1s[face];
//                int color2 = color2s[face];
//                int color3 = color3s[face];
//
//                if (color3 == -1)
//                {
//                    color2 = color3 = color1;
//                }
//                else if (createColor[face] && color3 == -2)
//                {
//                    continue;
//                }
//
//                // c2 is the primary face color, c1 is used as a booster when c3 is flagged as far as I can tell.
//                // I dont know if we actually want to continue with c3 = -2, but it should draw it black so it will at least be easy to debug.
////				Color c1 = new Color(JagexColor.HSLtoRGB((short)color1, JagexColor.BRIGTHNESS_MIN));
////				Color c2 = new Color(JagexColor.HSLtoRGB((short)color2, JagexColor.BRIGTHNESS_MIN));
////				Color c3 = new Color(JagexColor.HSLtoRGB((short)color3, JagexColor.BRIGTHNESS_MIN));
//
////				double r = c2.getRed() / 255.0;
////				double g = c2.getGreen()/ 255.0;
////				double b = c2.getBlue() / 255.0;
//
////				Color color1 = new Color(JagexColor.HSLtoRGB((short)color1s[face], JagexColor.BRIGTHNESS_MIN));
////				Color color2 = new Color(JagexColor.HSLtoRGB((short)color2s[face], JagexColor.BRIGTHNESS_MIN));
////				Color color3 = new Color(JagexColor.HSLtoRGB((short)color3s[face], JagexColor.BRIGTHNESS_MIN));
////				double r = (color1.getRed() / 255.0 + color2.getRed() / 255.0 + color3.getRed() / 255.0) / 3;
////				double g = (color1.getGreen() / 255.0 + color2.getGreen() / 255.0 + color3.getGreen() / 255.0) / 3;
////				double b = (color1.getBlue() / 255.0 + color2.getBlue() / 255.0 + color3.getBlue() / 255.0) / 3;
//
//                int rgb1 = JagexColor.HSLtoRGB((short) color1, JagexColor.BRIGTHNESS_MIN);
//                int rgb2 = JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN);
//                int rgb3 = JagexColor.HSLtoRGB((short) color3, JagexColor.BRIGTHNESS_MIN);
//                Color c1 = new Color(rgb1);
//                Color c2 = new Color(rgb2);
//                Color c3 = new Color(rgb3);
//                float r1 = c1.getRed() / 255.0f;
//                float g1 = c1.getGreen() / 255.0f;
//                float b1 = c1.getBlue() / 255.0f;
//                float r2 = c2.getRed() / 255.0f;
//                float g2 = c2.getGreen() / 255.0f;
//                float b2 = c2.getBlue() / 255.0f;
//                float r3 = c3.getRed() / 255.0f;
//                float g3 = c3.getGreen() / 255.0f;
//                float b3 = c3.getBlue() / 255.0f;
//                double r = Math.sqrt((r1 * r1 + r2 * r2 + r3 * r3) / 3);
//                double g = Math.sqrt((g1 * g1 + g2 * g2 + g3 * g3) / 3);
//                double b = Math.sqrt((b1 * b1 + b2 * b2 + b3 * b3) / 3);
//                String colorFormat;
//                if(createColor[face]) {
//                    colorFormat = String.format("fc %.4f %.4f %.4f\n", r, g, b);
//                    modelData.append(colorFormat);
//                }
//            }
//
//            modelData.append("**br**\n");
//            String path = RuneLite.RUNELITE_DIR + "//models//";
//            File modelOutput = new File(path + name);
//            FileWriter modelWriter = new FileWriter(modelOutput);
//            modelWriter.write(modelData.toString());
//            modelWriter.flush();
//            modelWriter.close();
//        }
//        else {
//            double offSet = 0.00390625 * 2;
//            double step = 0.0078125 * 2;
//            int uvMax = 255;
//            int u = 0;
//            int v = 0;
//            for (int face = 0; face < model.getTrianglesCount(); ++face) {
//                String uvMap = String.format("uv %s %s\n", offSet + u * step, offSet + v * step);
//                int color3 = color3s[face];
//                if(color3 == -2){
////					modelData.append(String.format("uv 0.0 0.0\n"));
////					modelData.append(String.format("uv 0.0 0.0\n"));
////					modelData.append(String.format("uv 0.0 0.0\n"));
//                }else {
//                    modelData.append(uvMap);
//                    modelData.append(uvMap);
//                    modelData.append(uvMap);
//
//                    u++;
//                    if (u > uvMax / 2 / 2) {
//                        u = 0;
//                        v++;
//                    }
//                }
//            }
//            for (int face = 0; face < model.getTrianglesCount(); ++face) {
//                int x = model.getTrianglesX()[face] + 1;
//                int y = model.getTrianglesY()[face] + 1;
//                int z = model.getTrianglesZ()[face] + 1;
//                int color3 = color3s[face];
//                if(color3 == -2) {
//                    continue;
//                }
//                String faceFormat = String.format("tr %d/%d %d/%d %d/%d\n", x, (3 * face + 0) + 1, y, (3 * face + 1) + 1, z, (3 * face + 2) + 1);
//                modelData.append(faceFormat);
//
////				modelData.append("tr ").append(x).append("/").append((3*face+0)+1).append(" ").append(y).append("/").append((3*face+1)+1).append(" ").append(z).append("/").append((3*face+2)+1).append("\n");
//            }
//
//            // Colors
//            modelData.append("ts " + (uvMax + 1) + "\n");
//            for (int face = 0; face < model.getTrianglesCount(); ++face) {
//
//                int color1 = color1s[face];
//                int color2 = color2s[face];
//                int color3 = color3s[face];
//
//                if (color3 == -1) {
//                    color2 = color3 = color1;
//                }
//                else if (color3 == -2)
//                {
//                    continue;
//                }
//
//                // c2 is the primary face color, c1 is used as a booster when c3 is flagged as far as I can tell.
//                // I dont know if we actually want to continue with c3 = -2, but it should draw it black so it will at least be easy to debug.
////				Color c1 = new Color(JagexColor.HSLtoRGB((short)color1, JagexColor.BRIGTHNESS_MIN));
////				Color c2 = new Color(JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN)); Using this one
////				Color c3 = new Color(JagexColor.HSLtoRGB((short)color3, JagexColor.BRIGTHNESS_MIN));
//
////				double r = c2.getRed()  / 255.0;
////				double g = c2.getGreen() / 255.0;
////				double b = c2.getBlue() / 255.0;
//
//                int rgb1 = JagexColor.HSLtoRGB((short) color1, JagexColor.BRIGTHNESS_MIN);
//                int rgb2 = JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN);
//                int rgb3 = JagexColor.HSLtoRGB((short) color3, JagexColor.BRIGTHNESS_MIN);
//                Color c1 = new Color(rgb1);
//                Color c2 = new Color(rgb2);
//                Color c3 = new Color(rgb3);
//                float r1 = c1.getRed() / 255.0f;
//                float g1 = c1.getGreen() / 255.0f;
//                float b1 = c1.getBlue() / 255.0f;
//                float r2 = c2.getRed() / 255.0f;
//                float g2 = c2.getGreen() / 255.0f;
//                float b2 = c2.getBlue() / 255.0f;
//                float r3 = c3.getRed() / 255.0f;
//                float g3 = c3.getGreen() / 255.0f;
//                float b3 = c3.getBlue() / 255.0f;
//                double r = Math.sqrt((r1 * r1 + r2 * r2 + r3 * r3) / 3);
//                double g = Math.sqrt((g1 * g1 + g2 * g2 + g3 * g3) / 3);
//                double b = Math.sqrt((b1 * b1 + b2 * b2 + b3 * b3) / 3);
//
//                String colorFormat = String.format("fc %.4f %.4f %.4f\n", r, g, b);
//                modelData.append(colorFormat);
//            }
//
//            modelData.append("**br**\n");
//            String path = RuneLite.RUNELITE_DIR + "//models//";
//            File modelOutput = new File(path + name);
//            FileWriter modelWriter = new FileWriter(modelOutput);
//            modelWriter.write(modelData.toString());
//            modelWriter.flush();
//            modelWriter.close();
//        }
//    }
//
//    public String exportObjGroup(Model model, String name)throws IOException{
//        StringBuilder modelData = new StringBuilder();
//        modelData.append("nm " + name +"\n");
//        final int[] color1s = model.getFaceColors1();
//        final int[] color2s = model.getFaceColors2();
//        final int[] color3s = model.getFaceColors3();
//
//
//        for (int i = 0; i < model.getVerticesCount(); ++i) {
//            int vx = model.getVerticesX()[i];
//            int	vy = model.getVerticesY()[i] * -1;
//            int vz = model.getVerticesZ()[i] * -1;
//            modelData.append(String.format("vt %d %d %d\n", vx, vy, vz));
//        }
//
//        if(model.getFaceTextures() != null) {
//            short[] faceTextures = model.getFaceTextures();
//            double offSet = 0.00390625;
//            double step = 0.0078125;
//            int uvMax = 255;
//            int u = 0;
//            int v = 0;
//            log.info(String.format("U: %d | V: %d | Tris: %d",
//                    model.getFaceTextureUCoordinates().length,
//                    model.getFaceTextureVCoordinates().length,
//                    model.getTrianglesCount()));
//
//            boolean[] createColor = new boolean[model.getTrianglesCount()];
//            for (int i = 0; i < model.getFaceTextureUCoordinates().length; ++i) {
//                short textureID = faceTextures[i];
//                if (model.getFaceTextureUCoordinates()[i] == null || model.getFaceTextureVCoordinates()[i] == null || textureID == -1) {
//                    String uvMap = String.format("uv %s %s\n", (offSet + u * step), (offSet + v * step));
//                    int color3 = color3s[i];
//                    if(color3 == -2){
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        modelData.append(String.format("uv 0.0 0.0\n"));
//                        createColor[i] = false;
//                    }else {
//                        modelData.append(uvMap);
//                        modelData.append(uvMap);
//                        modelData.append(uvMap);
//                        createColor[i] = true;
//                        u++;
//                        if (u > uvMax / 2 / 2) {
//                            u = 0;
//                            v++;
//                        }
//                    }
//                } else {
//                    for (int j = 0; j < model.getFaceTextureUCoordinates()[i].length; ++j) {
//                        modelData.append(String.format("fu %d %s %s\n", textureID, model.getFaceTextureUCoordinates()[i][j],
//                                model.getFaceTextureVCoordinates()[i][j]));
//                    }
//                    createColor[i] = false;
//                }
//            }
//
//            modelData.append("ts " + 512 + "\n");
//            for (int face = 0; face < model.getTrianglesCount(); ++face) {
//                int x = model.getTrianglesX()[face] + 1;
//                int y = model.getTrianglesY()[face] + 1;
//                int z = model.getTrianglesZ()[face] + 1;
//
//                if(createColor[face]) {
//                    int color3 = color3s[face];
//                    if (color3 == -2) {
//                        continue;
//                    }
//                }
//                String faceFormat = String.format("tr %d/%d %d/%d %d/%d\n", x, (3 * face + 0) + 1, y, (3 * face + 1) + 1, z, (3 * face + 2) + 1);
//                modelData.append(faceFormat);
//            }
//
//            for (int face = 0; face < model.getTrianglesCount(); ++face){
//
//                int color1 = color1s[face];
//                int color2 = color2s[face];
//                int color3 = color3s[face];
//
//                if (color3 == -1)
//                {
//                    color2 = color3 = color1;
//                }
//                else if (createColor[face] && color3 == -2)
//                {
//                    continue;
//                }
//
//                // c2 is the primary face color, c1 is used as a booster when c3 is flagged as far as I can tell.
//                // I dont know if we actually want to continue with c3 = -2, but it should draw it black so it will at least be easy to debug.
////				Color c1 = new Color(JagexColor.HSLtoRGB((short)color1, JagexColor.BRIGTHNESS_MIN));
////				Color c2 = new Color(JagexColor.HSLtoRGB((short)color2, JagexColor.BRIGTHNESS_MIN));
////				Color c3 = new Color(JagexColor.HSLtoRGB((short)color3, JagexColor.BRIGTHNESS_MIN));
//
////				double r = c2.getRed() / 255.0;
////				double g = c2.getGreen()/ 255.0;
////				double b = c2.getBlue() / 255.0;
//
//                int rgb1 = JagexColor.HSLtoRGB((short) color1, JagexColor.BRIGTHNESS_MIN);
//                int rgb2 = JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN);
//                int rgb3 = JagexColor.HSLtoRGB((short) color3, JagexColor.BRIGTHNESS_MIN);
//                Color c1 = new Color(rgb1);
//                Color c2 = new Color(rgb2);
//                Color c3 = new Color(rgb3);
//                float r1 = c1.getRed() / 255.0f;
//                float g1 = c1.getGreen() / 255.0f;
//                float b1 = c1.getBlue() / 255.0f;
//                float r2 = c2.getRed() / 255.0f;
//                float g2 = c2.getGreen() / 255.0f;
//                float b2 = c2.getBlue() / 255.0f;
//                float r3 = c3.getRed() / 255.0f;
//                float g3 = c3.getGreen() / 255.0f;
//                float b3 = c3.getBlue() / 255.0f;
//                double r = Math.sqrt((r1 * r1 + r2 * r2 + r3 * r3) / 3);
//                double g = Math.sqrt((g1 * g1 + g2 * g2 + g3 * g3) / 3);
//                double b = Math.sqrt((b1 * b1 + b2 * b2 + b3 * b3) / 3);
//                String colorFormat;
//                if(createColor[face]) {
//                    colorFormat = String.format("fc %.4f %.4f %.4f\n", r, g, b);
//                    modelData.append(colorFormat);
//                }
//            }
//
//            modelData.append("**br**\n");
//            return modelData.toString();
//
//        }
//        else {
//            double offSet = 0.00390625 * 2;
//            double step = 0.0078125 * 2;
//            int uvMax = 255;
//            int u = 0;
//            int v = 0;
//            for (int face = 0; face < model.getTrianglesCount(); ++face){
//                // UVs
//                String uvMap = String.format("uv %s %s\n", offSet + u * step, offSet + v * step);
//                int color3 = color3s[face];
//                if(color3 == -2){
////					modelData.append(String.format("uv 0.0 0.0\n"));
////					modelData.append(String.format("uv 0.0 0.0\n"));
////					modelData.append(String.format("uv 0.0 0.0\n"));
//                }else {
//                    modelData.append(uvMap);
//                    modelData.append(uvMap);
//                    modelData.append(uvMap);
//
//                    u++;
//                    if (u > uvMax / 2 / 2) {
//                        u = 0;
//                        v++;
//                    }
//                }
//            }
//            for (int face = 0; face < model.getTrianglesCount(); ++face)
//            {
//                int x = model.getTrianglesX()[face] + 1;
//                int y = model.getTrianglesY()[face] + 1;
//                int z = model.getTrianglesZ()[face] + 1;
//                int color3 = color3s[face];
//                if(color3 == -2) {
//                    continue;
//                }
//
//                String faceFormat = String.format("tr %d/%d %d/%d %d/%d\n",x ,(3*face+0)+1,y,(3*face+1)+1,z,(3*face+2)+1);
//                modelData.append(faceFormat);
//            }
//
//            // Colors
//            modelData.append("ts " + (uvMax + 1) + "\n");
//            for (int face = 0; face < model.getTrianglesCount(); ++face){
//                int color1 = color1s[face];
//                int color2 = color2s[face];
//                int color3 = color3s[face];
//
//                if (color3 == -1)
//                {
//                    color2 = color3 = color1;
//                }
//                else if (color3 == -2)
//                {
//                    continue;
//                }
//
//                // c2 is the primary face color, c1 is used as a booster when c3 is flagged as far as I can tell.
//                // I dont know if we actually want to continue with c3 = -2, but it should draw it black so it will at least be easy to debug.
////				Color c1 = new Color(JagexColor.HSLtoRGB((short)color1, JagexColor.BRIGTHNESS_MIN));
////				Color c2 = new Color(JagexColor.HSLtoRGB((short)color2, JagexColor.BRIGTHNESS_MIN));
////				Color c3 = new Color(JagexColor.HSLtoRGB((short)color3, JagexColor.BRIGTHNESS_MIN));
//
////				double r = c2.getRed() / 255.0;
////				double g = c2.getGreen()/ 255.0;
////				double b = c2.getBlue() / 255.0;
//
//                int rgb1 = JagexColor.HSLtoRGB((short) color1, JagexColor.BRIGTHNESS_MIN);
//                int rgb2 = JagexColor.HSLtoRGB((short) color2, JagexColor.BRIGTHNESS_MIN);
//                int rgb3 = JagexColor.HSLtoRGB((short) color3, JagexColor.BRIGTHNESS_MIN);
//                Color c1 = new Color(rgb1);
//                Color c2 = new Color(rgb2);
//                Color c3 = new Color(rgb3);
//                float r1 = c1.getRed() / 255.0f;
//                float g1 = c1.getGreen() / 255.0f;
//                float b1 = c1.getBlue() / 255.0f;
//                float r2 = c2.getRed() / 255.0f;
//                float g2 = c2.getGreen() / 255.0f;
//                float b2 = c2.getBlue() / 255.0f;
//                float r3 = c3.getRed() / 255.0f;
//                float g3 = c3.getGreen() / 255.0f;
//                float b3 = c3.getBlue() / 255.0f;
//                double r = Math.sqrt((r1 * r1 + r2 * r2 + r3 * r3) / 3);
//                double g = Math.sqrt((g1 * g1 + g2 * g2 + g3 * g3) / 3);
//                double b = Math.sqrt((b1 * b1 + b2 * b2 + b3 * b3) / 3);
//                String colorFormat = String.format("fc %.4f %.4f %.4f\n", r, g, b);
//                modelData.append(colorFormat);
//            }
//
//            modelData.append("**br**\n");
//            return  modelData.toString();
//        }
//    }
//
//    private  void ExportScene(StringBuilder mapSb) throws IOException{
//        Scene scene = client.getScene();
//        int regionID = lastTileOnMenuOpen.getWorldLocation().getRegionID();
//        Tile[][][] tiles = scene.getTiles();
//        String path = RuneLite.RUNELITE_DIR + "//models//";
//        final int[][][] tileHeights = client.getTileHeights();
//        for (int z = 0; z < tiles.length; ++z) {
//            StringBuilder sb = new StringBuilder();
//            StringBuilder uvb = new StringBuilder();
//            StringBuilder fb = new StringBuilder();
//            StringBuilder cb = new StringBuilder();
//            StringBuilder Scenesb = new StringBuilder();
//            StringBuilder Sceneuvb = new StringBuilder();
//            StringBuilder Scenefb = new StringBuilder();
//            StringBuilder Scenecb = new StringBuilder();
//            int xFirst = -1;
//            int yFirst = -1;
//            int xSFirst = -1;
//            int ySFirst = -1;
//            int lastTri = 1;
//            int lastSTri = 1;
//            double offSet = 0.00390625 * 2/8;
//            double step = 0.0078125 * 2/8;
//            int uvMax = 2047;
//            int u = 0;
//            int v = 0;
//            int su = 0;
//            int sv = 0;
//            for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
//                for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
//                    Tile tile = tiles[z][x][y];
//                    boolean flag = true;
//                    if (tile != null && tile.getWorldLocation().getRegionID() == regionID && tile.getSceneTilePaint() != null) {
//                        SceneTilePaint paint = tile.getSceneTilePaint();
//                        if (xFirst == -1 && yFirst == -1) {
//                            String name = String.format("%d_%d,%d,%d,0_Plane,%d_ID_%d", regionID, x, y, z, regionID, z);
//                            sb.append("nm " + name).append("\n");
//                            mapSb.append(name).append("\n");
//                            xFirst = x - tile.getWorldLocation().getRegionX();//x;
//                            yFirst = y - tile.getWorldLocation().getRegionY();//y;
//                        }
//
//                        int swHeight = tileHeights[z][x][y];
//                        int seHeight = tileHeights[z][x + 1][y];
//                        int neHeight = tileHeights[z][x + 1][y + 1];
//                        int nwHeight = tileHeights[z][x][y + 1];
//
//
////						int rgb = JagexColor.adjustForBrightness(paint.getRBG(), JagexColor.BRIGTHNESS_MIN);
////						if (rgb == 0)
////						{
////							rgb = 1;
////						}
////						Color colorFace = new Color(paint.getRBG());
////						float rFace = colorFace.getRed() / 255.0f;
////						float gFace = colorFace.getGreen() / 255.0f;
////						float bFace = colorFace.getBlue() / 255.0f;
//                        final int neColor = paint.getNeColor();
//                        final int nwColor = paint.getNwColor();
//                        final int seColor = paint.getSeColor();
//                        final int swColor = paint.getSwColor();
//                        final int texture = paint.getTexture();
//
//
//                        if (neColor == 12345678) {
//                            continue;
//                        }
//
//                        // 0,0 - 0
//                        float vertexDx = (x * 1.28f - xFirst * 1.28f) * 100;
//                        float vertexDy = (y * 1.28f - yFirst * 1.28f) * 100;
//                        float vertexDz = swHeight;
//                        Color c0 = new Color(JagexColor.HSLtoRGB((short) swColor, JagexColor.BRIGTHNESS_MIN));
//                        float r0 = c0.getRed() / 255.0f;
//                        float g0 = c0.getGreen() / 255.0f;
//                        float b0 = c0.getBlue() / 255.0f;
//
//                        // 1,0 - 1
//                        float vertexCx = (x * 1.28f + 1.28f - xFirst * 1.28f) * 100;
//                        float vertexCy = (y * 1.28f - yFirst * 1.28f) * 100;
//                        float vertexCz = seHeight;
//                        Color c1 = new Color(JagexColor.HSLtoRGB((short) seColor, JagexColor.BRIGTHNESS_MIN));
//                        float r1 = c1.getRed() / 255.0f;
//                        float g1 = c1.getGreen() / 255.0f;
//                        float b1 = c1.getBlue() / 255.0f;
//
//                        // 1,1 - 3
//                        float vertexAx = (x * 1.28f + 1.28f - xFirst * 1.28f) * 100;
//                        float vertexAy = (y * 1.28f + 1.28f - yFirst * 1.28f) * 100;
//                        float vertexAz = neHeight;
//                        Color c2 = new Color(JagexColor.HSLtoRGB((short) neColor, JagexColor.BRIGTHNESS_MIN));
//                        float r2 = c2.getRed() / 255.0f;
//                        float g2 = c2.getGreen() / 255.0f;
//                        float b2 = c2.getBlue() / 255.0f;
//
//                        // 0,1 - 2
//                        float vertexBx = (x * 1.28f - xFirst * 1.28f) * 100;
//                        float vertexBy = (y * 1.28f + 1.28f - yFirst * 1.28f) * 100;
//                        float vertexBz = nwHeight;
//                        Color c3 = new Color(JagexColor.HSLtoRGB((short) nwColor, JagexColor.BRIGTHNESS_MIN));
//                        float r3 = c3.getRed() / 255.0f;
//                        float g3 = c3.getGreen() / 255.0f;
//                        float b3 = c3.getBlue() / 255.0f;
//
//
//                        sb.append(String.format("vt %s %s %s\n", vertexDx, -vertexDz, -vertexDy)); // 0
//                        sb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
//                        sb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
//
//                        sb.append(String.format("vt %s %s %s\n", vertexBx, -vertexBz, -vertexBy)); // 2
//                        sb.append(String.format("vt %s %s %s\n", vertexCx, -vertexCz, -vertexCy)); // 1
//                        sb.append(String.format("vt %s %s %s\n", vertexAx, -vertexAz, -vertexAy)); // 3
//
//                        if(texture == -1) {
//                            String uvMap = String.format("uv %s %s\n", offSet + u * step, offSet + v * step);
//                            uvb.append(String.format(uvMap));
//                            uvb.append(String.format(uvMap));
//                            uvb.append(String.format(uvMap));
//                            u++;
//                            if (u > uvMax / 2 / 2) {
//                                u = 0;
//                                v++;
//                            }
//                            uvMap = String.format("uv %s %s\n", offSet + u * step, offSet + v * step);
//                            uvb.append(String.format(uvMap));
//                            uvb.append(String.format(uvMap));
//                            uvb.append(String.format(uvMap));
//                            u++;
//                            if (u > uvMax / 2 / 2) {
//                                u = 0;
//                                v++;
//                            }
//                        }else{
//                            double textureX = texture - 16.0 * Math.floor(texture / 16.0);
//                            double textureY = Math.floor(texture / 16.0);
//
//                            double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
//                            double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));
//
//                            double uvx1 = (1.0 + (textureX * -0.0625));
//                            double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));
//
//                            double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
//                            double uvy2 = (1.0 + (textureY * -0.0625));
//
//                            double uvx3 = (1.0 + (textureX * -0.0625));
//                            double uvy3 = (1.0 + (textureY * -0.0625));
//
//                            String uvMap0 = String.format("uv %s %s\n",uvx0 ,uvy0);
//                            String uvMap1 = String.format("uv %s %s\n",uvx1 ,uvy1);
//                            String uvMap2 = String.format("uv %s %s\n",uvx2 ,uvy2);
//                            String uvMap3 = String.format("uv %s %s\n",uvx3 ,uvy3);
//
//                            uvb.append(String.format(uvMap0));
//                            uvb.append(String.format(uvMap1));
//                            uvb.append(String.format(uvMap2));
//
//                            uvb.append(String.format(uvMap2));
//                            uvb.append(String.format(uvMap1));
//                            uvb.append(String.format(uvMap3));
//                        }
//
//                        fb.append(String.format("tr %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                                lastTri, lastTri, FormatVertexColor(swColor),
//                                lastTri + 1, lastTri + 1, FormatVertexColor(seColor),
//                                lastTri + 2, lastTri + 2, FormatVertexColor(nwColor)));
//                        lastTri += 3;
//                        fb.append(String.format("tr %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                                lastTri, lastTri, FormatVertexColor(nwColor),
//                                lastTri + 1, lastTri + 1, FormatVertexColor(seColor),
//                                lastTri + 2, lastTri + 2, FormatVertexColor(neColor)));
//                        lastTri += 3;
//
//
//                        if(texture == -1) {
//                            // c0, c1, c2
//                            double r012 = Math.sqrt((r0 * r0 + r1 * r1 + r2 * r2) / 3);
//                            double g012 = Math.sqrt((g0 * g0 + g1 * g1 + g2 * g2) / 3);
//                            double b012 = Math.sqrt((b0 * b0 + b1 * b1 + b2 * b2) / 3);
//                            // c2, c1, c3
//                            double r213 = Math.sqrt((r2 * r2 + r1 * r1 + r3 * r3) / 3);
//                            double g213 = Math.sqrt((g2 * g2 + g1 * g1 + g3 * g3) / 3);
//                            double b213 = Math.sqrt((b2 * b2 + b1 * b1 + b3 * b3) / 3);
//
//                            double r = Math.sqrt((r213 * r213 + r012 * g012) / 2);
//                            double g = Math.sqrt((g012 * g012 + g213 * g213) / 2);
//                            double b = Math.sqrt((b012 * b012 + b213 * b213) / 2);
//
////						double rF = Math.sqrt((r * r + rFace * rFace) / 2);
////						double gF = Math.sqrt((g * g + gFace * gFace) / 2);
////						double bF = Math.sqrt((b * b + bFace * bFace) / 2);
//                            cb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
//                            cb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
//                        }
//
//                    }
//                    if (flag == true && tile != null && tile.getWorldLocation().getRegionID() == regionID && tile.getSceneTileModel() != null){
//                        SceneTileModel sceneTileModel = tile.getSceneTileModel();
//                        if (xSFirst == -1 && ySFirst == -1) {
//                            String name = String.format("%d_%d,%d,%d,0_Scene,%d_ID_%d", regionID, x, y, z, regionID, z);
//                            Scenesb.append("nm " + name).append("\n");
//                            mapSb.append(name).append("\n");
//                            xSFirst = x - tile.getWorldLocation().getRegionX();//x;
//                            ySFirst = y - tile.getWorldLocation().getRegionY();//y;
//                        }
//
//                        final int[] faceX = sceneTileModel.getFaceX();
//                        final int[] faceY = sceneTileModel.getFaceY();
//                        final int[] faceZ = sceneTileModel.getFaceZ();
//
//                        final int[] vertexX = sceneTileModel.getVertexX();
//                        final int[] vertexY = sceneTileModel.getVertexY();
//                        final int[] vertexZ = sceneTileModel.getVertexZ();
//
//                        final int[] triangleColorA = sceneTileModel.getTriangleColorA();
//                        final int[] triangleColorB = sceneTileModel.getTriangleColorB();
//                        final int[] triangleColorC = sceneTileModel.getTriangleColorC();
//
//                        final int[] triangleTextures = sceneTileModel.getTriangleTextureId();
//                        final int faceCount = faceX.length;
//
//                        int cnt = 0;
//                        for (int i = 0; i < faceCount; ++i)
//                        {
//                            int texture = -1;
//                            if(triangleTextures != null){
//                                texture = triangleTextures[i];
//                            }
//                            final int triangleA = faceX[i];
//                            final int triangleB = faceY[i];
//                            final int triangleC = faceZ[i];
//
//                            int colorA = triangleColorA[i];
//                            int colorB = triangleColorB[i];
//                            int colorC = triangleColorC[i];
//
//                            if (colorA == 12345678)
//                            {
//                                continue;
//                            }
//
//                            cnt += 3;
//
//                            // vertexes are stored in scene local, convert to tile local
//                            double vertexXA = vertexX[triangleA] - xSFirst*1.28*100;
//                            double vertexZA = vertexZ[triangleA] - ySFirst*1.28*100;
//
//                            double vertexXB = vertexX[triangleB] - xSFirst*1.28*100;
//                            double vertexZB = vertexZ[triangleB] - ySFirst*1.28*100;
//
//                            double vertexXC = vertexX[triangleC] - xSFirst*1.28*100;
//                            double vertexZC = vertexZ[triangleC] - ySFirst*1.28*100;
//
//                            Scenesb.append(String.format("vt %s %s %s\n", vertexXA, -vertexY[triangleA], -vertexZA)); // 0
//                            Scenesb.append(String.format("vt %s %s %s\n", vertexXB, -vertexY[triangleB], -vertexZB)); // 1
//                            Scenesb.append(String.format("vt %s %s %s\n", vertexXC, -vertexY[triangleC], -vertexZC)); // 2
//
//
//                            String uvMap = String.format("uv %s %s\n", offSet + su * step, offSet + sv * step);
//                            if(texture == -1) {
////								if(colorC == -2){
////									uvb.append(String.format("uv 1.0 1.0\n"));
////									uvb.append(String.format("uv 1.0 1.0\n"));
////									uvb.append(String.format("uv 1.0 1.0\n"));
////								}else {
//                                Sceneuvb.append(String.format(uvMap));
//                                Sceneuvb.append(String.format(uvMap));
//                                Sceneuvb.append(String.format(uvMap));
//                                su++;
//                                if (su > uvMax / 2 / 2) {
//                                    su = 0;
//                                    sv++;
//                                }
////								}
//                            }else{
//                                double textureX = texture - 16.0 * Math.floor(texture / 16.0);
//                                double textureY = Math.floor(texture / 16.0);
//
//                                double uvx0 = (1.0 + (-0.0625 + textureX * -0.0625));
//                                double uvy0 = (1.0 + (-0.0625 + textureY * -0.0625));
//
//                                double uvx1 = (1.0 + (textureX * -0.0625));
//                                double uvy1 = (1.0 + (-0.0625 + textureY * -0.0625));
//
//                                double uvx2 = (1.0 + (-0.0625 + textureX * -0.0625));
//                                double uvy2 = (1.0 + (textureY * -0.0625));
//
//                                double uvx3 = (1.0 + (textureX * -0.0625));
//                                double uvy3 = (1.0 + (textureY * -0.0625));
//
//                                String uvMap0 = String.format("uv %s %s\n",uvx0 ,uvy0);
//                                String uvMap1 = String.format("uv %s %s\n",uvx1 ,uvy1);
//                                String uvMap2 = String.format("uv %s %s\n",uvx2 ,uvy2);
//                                String uvMap3 = String.format("uv %s %s\n",uvx3 ,uvy3);
//
//                                Sceneuvb.append(String.format(uvMap0));
//                                Sceneuvb.append(String.format(uvMap1));
//                                Sceneuvb.append(String.format(uvMap2));
//                            }
//
////							if (colorA == -1)
////							{
////								colorB = colorA = colorC;
////							}
////							else if (texture == -1 && colorC == -2)
////							{
////								lastSTri += 3;
////								continue;
////							}
//                            Scenefb.append(String.format("tr %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                                    lastSTri, lastSTri, FormatVertexColor(colorA),
//                                    lastSTri + 1, lastSTri + 1, FormatVertexColor(colorB),
//                                    lastSTri + 2, lastSTri + 2, FormatVertexColor(colorC)));
//                            lastSTri += 3;
//
//                            if(texture == -1) {
//                                int rgb1 = JagexColor.HSLtoRGB((short) colorA, JagexColor.BRIGTHNESS_MIN);
//                                int rgb2 = JagexColor.HSLtoRGB((short) colorB, JagexColor.BRIGTHNESS_MIN);
//                                int rgb3 = JagexColor.HSLtoRGB((short) colorC, JagexColor.BRIGTHNESS_MIN);
//                                Color color1 = new Color(rgb1);
//                                Color color2 = new Color(rgb2);
//                                Color color3 = new Color(rgb3);
//                                float r1 = color1.getRed() / 255.0f;
//                                float g1 = color1.getGreen() / 255.0f;
//                                float b1 = color1.getBlue() / 255.0f;
//                                float r2 = color2.getRed() / 255.0f;
//                                float g2 = color2.getGreen() / 255.0f;
//                                float b2 = color2.getBlue() / 255.0f;
//                                float r3 = color3.getRed() / 255.0f;
//                                float g3 = color3.getGreen() / 255.0f;
//                                float b3 = color3.getBlue() / 255.0f;
//                                double r = Math.sqrt((r1 * r1 + r2 * r2 + r3 * r3) / 3);
//                                double g = Math.sqrt((g1 * g1 + g2 * g2 + g3 * g3) / 3);
//                                double b = Math.sqrt((b1 * b1 + b2 * b2 + b3 * b3) / 3);
//                                Scenecb.append(String.format("fc %.4f %.4f %.4f\n", r, g, b));
//                            }
//                        }
//                    }
//                }
//            }
//
//            sb.append(uvb.toString());
//            sb.append(fb.toString());
//            sb.append("ts 2048\n");
//            sb.append(cb.toString());
//            sb.append("**br**\n");
//
//            Scenesb.append(Sceneuvb.toString());
//            Scenesb.append(Scenefb.toString());
//            Scenesb.append("ts 2048\n");
//            Scenesb.append(Scenecb.toString());
//            Scenesb.append("**br**\n");
//
//            String name = "Plane_" + regionID + "_" + z + ".txt";
//            File modelOutput = new File(path + name);
//            FileWriter modelWriter = new FileWriter(modelOutput);
//            modelWriter.write(sb.toString());
//            modelWriter.flush();
//            modelWriter.close();
//
//            String sname = "Scene_" + regionID + "_" + z + ".txt";
//            File sOutput = new File(path + sname);
//            FileWriter sWriter = new FileWriter(sOutput);
//            sWriter.write(Scenesb.toString());
//            sWriter.flush();
//            sWriter.close();
//        }
//    }
//
//    public void ExportAsFile(Model model, String name, StringBuilder regionSB) throws IOException
//    {
//        StringBuilder modelSB = new StringBuilder();
//        StringBuilder vertexSB = new StringBuilder();
//        StringBuilder uvSB = new StringBuilder();
//        StringBuilder triSB = new StringBuilder();
//        StringBuilder colorSB = new StringBuilder();
//
//        modelSB.append("nm " + name.replace(".txt","")+"\n");
//
//        final int triangleCount = model.getTrianglesCount();
//
//        final int vertexCount = model.getVerticesCount();
//        final int[] vertexX = model.getVerticesX();
//        final int[] vertexY = model.getVerticesY();
//        final int[] vertexZ = model.getVerticesZ();
//
//        final int[] trianglesX = model.getTrianglesX();
//        final int[] trianglesY = model.getTrianglesY();
//        final int[] trianglesZ = model.getTrianglesZ();
//
//        final int[] color1s = model.getFaceColors1();
//        final int[] color2s = model.getFaceColors2();
//        final int[] color3s = model.getFaceColors3();
//
//        final byte[] transparencies = model.getTriangleTransparencies();
//        final short[] faceTextures = model.getFaceTextures();
//        final byte[] facePriorities = model.getFaceRenderPriorities();
//
//        float[][] us = model.getFaceTextureUCoordinates();
//        float[][] vs = model.getFaceTextureVCoordinates();
//
//        double textureOffSet = 0.00390625;
//        double textureStep = 0.0078125;
//        int textureUvMax = 255;
//        int textureU = 0;
//        int textureV = 0;
//
//        double flatOffSet = 0.00390625 * 2;
//        double flatStep = 0.0078125 * 2;
//        int flatUvMax = 255;
//        int flatU = 0;
//        int flatV = 0;
//
//        int len = 0;
//        for (int vertex = 0; vertex < vertexCount; ++vertex)
//        {
//            vertexSB.append(String.format("vt %d %d %d\n", vertexX[vertex], -vertexY[vertex], -vertexZ[vertex]));
//        }
//
//        int lastFace = 0;
//        for (int face = 0; face < triangleCount; ++face)
//        {
//            int color1 = color1s[face];
//            int color2 = color2s[face];
//            int color3 = color3s[face];
//
//            int triangleA = trianglesX[face];
//            int triangleB = trianglesY[face];
//            int triangleC = trianglesZ[face];
//
//            boolean colorBreak = false;
//            if (color3 == -1)
//            {
//                color2 = color3 = color1;
//            }
//            else if (color3 == -2)
//            {
//                colorBreak = true;
//            }
//
//            if (faceTextures != null)
//            {
//                short textureID = faceTextures[face];
//                if (us[face] != null && vs[face] != null && textureID != -1)
//                {
//                    for (int j = 0; j < us[face].length; ++j)
//                    {
//                        uvSB.append(String.format("fu %d %s %s\n",
//                                textureID,
//                                us[face][j],
//                                vs[face][j]));
//                    }
//                }
//                else
//                {
//                    if (colorBreak)
//                    {
//                        uvSB.append(String.format("uv 0.0 0.0\n"));
//                        uvSB.append(String.format("uv 0.0 0.0\n"));
//                        uvSB.append(String.format("uv 0.0 0.0\n"));
//                    }
//                    else
//                    {
//                        String uvMap = String.format("uv %s %s\n", textureOffSet + textureU * textureStep, textureOffSet + textureV * textureStep);
//                        uvSB.append(uvMap);
//                        uvSB.append(uvMap);
//                        uvSB.append(uvMap);
//
//                        textureU++;
//                        if (textureU > textureUvMax / 2 / 2) {
//                            textureU = 0;
//                            textureV++;
//                        }
//                    }
//
//                    if(!colorBreak)
//                    {
//                        colorSB.append(FormatColor(color1, color2, color3));
//                    }
//                }
//
//                String faceFormat = String.format("tr %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                        triangleA + 1, (3 * face + 0) + 1, FormatVertexColor(color1),
//                        triangleB + 1, (3 * face + 1) + 1, FormatVertexColor(color2),
//                        triangleC + 1, (3 * face + 2) + 1, FormatVertexColor(color3));
//                triSB.append(faceFormat);
//            }
//            else
//            {
//                if(!colorBreak)
//                {
//                    String uvMap = String.format("uv %s %s\n", flatOffSet + flatU * flatStep, flatOffSet + flatV * flatStep);
//                    uvSB.append(uvMap);
//                    uvSB.append(uvMap);
//                    uvSB.append(uvMap);
//
//                    flatU++;
//                    if (flatU > flatUvMax / 2 / 2) {
//                        flatU = 0;
//                        flatV++;
//                    }
//
//                    String faceFormat = String.format("tr %d/%d/%s %d/%d/%s %d/%d/%s\n",
//                            triangleA + 1, (3 * lastFace + 0) + 1, FormatVertexColor(color1),
//                            triangleB + 1, (3 * lastFace + 1) + 1, FormatVertexColor(color2),
//                            triangleC + 1, (3 * lastFace + 2) + 1, FormatVertexColor(color3));
//                    triSB.append(faceFormat);
//                    colorSB.append(FormatColor(color1,color2,color3));
//                    lastFace++;
//                }
//            }
//        }
//
//        modelSB.append(vertexSB.toString());
//        modelSB.append(uvSB.toString());
//        if(faceTextures != null) {
//            modelSB.append("ts " + 512 + "\n");
//        }else{
//            modelSB.append("ts " + 256 + "\n");
//        }
//        modelSB.append(triSB.toString());
//        modelSB.append(colorSB.toString());
//        modelSB.append("**br**\n");
//
//        if(regionSB != null)
//        {
//            regionSB.append(modelSB.toString());
//        }
//        else
//        {
//            String path = RuneLite.RUNELITE_DIR + "//models//";
//            File modelOutput = new File(path + name);
//            FileWriter modelWriter = new FileWriter(modelOutput);
//            modelWriter.write(modelSB.toString());
//            modelWriter.flush();
//            modelWriter.close();
//        }
//    }


//    public void exportRegionMap() throws IOException {
//        Scene scene = client.getScene();
//        int regionID = lastTileOnMenuOpen.getWorldLocation().getRegionID();
//        Tile[][][] tiles = scene.getTiles();
//        StringBuilder sb = new StringBuilder();
//        for (int z = 0; z < tiles.length; ++z) {
//            for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
//                for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
//                    Tile tile = tiles[z][x][y];
//                    if (tile != null && tile.getWorldLocation().getRegionID() == regionID){
//                        String name = "";
//                        String tileFormat = String.format("%d_%d,%d,%d_", regionID, tile.getWorldLocation().getRegionX(), tile.getWorldLocation().getRegionY(), z);
//                        if (tile.getWallObject() != null) {
//                            ObjectComposition wComp = client.getObjectDefinition(tile.getWallObject().getId());
//                            name = wComp.getName();
//                            if (name == null || name.contains("null")) {
//                                name = "" + tile.getWallObject().getId();
//                            }else{
//                                name = name + "_" + tile.getWallObject().getId();
//                            }
//                            sb.append(tileFormat + "Wall1_" + name+"\n");
//                            sb.append(tileFormat + "Wall2_" + name+"\n");
//                        }
//                        if (tile.getDecorativeObject() != null) {
//                            ObjectComposition dComp = client.getObjectDefinition(tile.getDecorativeObject().getId());
//                            name = dComp.getName();
//                            if (name == null || name.contains("null")) {
//                                name = "" + tile.getDecorativeObject().getId();
//                            }
//                            else{
//                                name = name + "_" + tile.getDecorativeObject().getId();
//                            }
//                            sb.append(tileFormat + "Decorative1_" + name+"\n");
//                            sb.append(tileFormat + "Decorative2_" + name+"\n");
//                        }
//                        if (tile.getGroundObject() != null) {
//                            ObjectComposition gComp = client.getObjectDefinition(tile.getGroundObject().getId());
//                            name = gComp.getName();
//                            if (name == null || name.contains("null")) {
//                                name = "" + tile.getGroundObject().getId();
//                            }
//                            else{
//                                name = name + "_" + tile.getGroundObject().getId();
//                            }
//                            sb.append(tileFormat + "Ground_" + name+"\n");
//                        }
//                        if (tile.getGameObjects() != null) {
//                            for (GameObject go : tile.getGameObjects()) {
//                                if (go == null) {
//                                    continue;
//                                }
//                                ObjectComposition goComp = client.getObjectDefinition(go.getId());
//                                name = Text.removeFormattingTags(goComp.getName());
//                                if (name.contains("null")) {
//                                    name = "" + go.getId();
//                                }
//                                else{
//                                    name = name + "_" + go.getId();
//                                }
//                                sb.append(tileFormat + "GameObject_" + name+"\n");
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        String name = "Map_" + regionID + ".txt";
//        String path = RuneLite.RUNELITE_DIR + "//models//";
//        File modelOutput = new File(path + name);
//        FileWriter stringWriter = new FileWriter(modelOutput);
//        stringWriter.write(sb.toString());
//        stringWriter.flush();
//        stringWriter.close();
//    }
//    private void TryExportRenderable(String name, Renderable renderable, boolean exportTextures) throws  IOException{
//        if(renderable != null && renderable instanceof  Model){
//            DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//            export((Model) renderable, name + ".obj", exportTextures);
//        }
//        else {
//            if(renderable != null && renderable.getModel() != null){
//                DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                export(renderable.getModel(), name + ".obj", exportTextures);
//            }
//        }
//    }
//
//    private void exportAllAvailableModels() throws  IOException {
//        Scene scene = client.getScene();
//        Tile tile = lastTileOnMenuOpen;
//        Tile[][][] tiles = scene.getTiles();
//        int sx = tile.getSceneLocation().getX();
//        int sy = tile.getSceneLocation().getY();
//        log.info("X: " + sx + " " + "Y: " + sy);
//        for (int z = 0; z < tiles.length; ++z) {
//            tile = tiles[z][sx][sy];
//            String name = "";
//            if (tile != null) {
//                String tileFormat = String.format("[%d] [%d,%d,%d] ",tile.getWorldLocation().getRegionID(), tile.getWorldLocation().getRegionX(),tile.getWorldLocation().getRegionY(),z);
//                if (tile.getWallObject() != null) {
//                    ObjectComposition wComp = client.getObjectDefinition(tile.getWallObject().getId());
//                    name = wComp.getName();
//                    if(name == null || name == "null"){
//                        name = ""+tile.getWallObject().getId();
//                    }
//                    TryExportRenderable(tileFormat + "Wall1_" + name, tile.getWallObject().getRenderable1(), true);
//                    TryExportRenderable(tileFormat + "Wall2_" + name, tile.getWallObject().getRenderable2(),true);
//                }
//                if (tile.getDecorativeObject() != null) {
//                    ObjectComposition dComp = client.getObjectDefinition(tile.getDecorativeObject().getId());
//                    name = dComp.getName();
//                    if(name == null || name == "null"){
//                        name = ""+tile.getDecorativeObject().getId();
//                    }
//                    TryExportRenderable(tileFormat + "Decorative1_" + name, tile.getDecorativeObject().getRenderable(), true);
//                    TryExportRenderable(tileFormat + "Decorative2_" + name, tile.getDecorativeObject().getRenderable2(), true);
//                }
//                if (tile.getGroundObject() != null) {
//                    ObjectComposition gComp = client.getObjectDefinition(tile.getGroundObject().getId());
//                    name = gComp.getName();
//                    if(name == null || name == "null"){
//                        name = ""+tile.getGroundObject().getId();
//                    }
//                    TryExportRenderable(tileFormat + "Ground_" + name, tile.getGroundObject().getRenderable(), true);
//                }
//                if (tile.getGameObjects() != null) {
//                    for (GameObject go : tile.getGameObjects()) {
//                        if (go == null) {
//                            continue;
//                        }
//                        ObjectComposition goComp = client.getObjectDefinition(go.getId());
//                        name = Text.removeFormattingTags(goComp.getName());
//                        if(name == null || name == "null"){
//                            name = ""+go.getId();
//                        }
//                        TryExportRenderable(tileFormat + "GameObject_" + name, go.getRenderable(), true);
//                    }
//                }
//                if (tile.getGroundItems() != null) {
//                    for (TileItem gi : tile.getGroundItems()) {
//                        if (gi == null) {
//                            continue;
//                        }
//                        ItemComposition giComp = client.getItemDefinition(gi.getId());
//                        name = Text.removeFormattingTags(giComp.getName());
//                        if(name == null || name == "null"){
//                            name = ""+gi.getId();
//                        }
//                        TryExportRenderable("Item_" + name, gi.getModel(), true);
//                    }
//                }
////				exportTile(tile.getSceneTileModel(), tileFormat + ".obj");
////				exportTilePaint(tile.getSceneTilePaint(), tileFormat);
//            }
//        }
//    }
//
//    private void exportTargetModel(String menuTarget, int id) throws IOException {
//        if (id < 0 || id >= client.getCachedNPCs().length) {
//            return;
//        }
//        NPC npc = client.getCachedNPCs()[id];
//        if (npc == null) {
//            return;
//        }
//        log.info(npc.getName() + " | " + Text.removeFormattingTags(menuTarget));
//        DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        export(npc.getModel(), "NPC_" + Text.removeFormattingTags(menuTarget) + " " + TIME_FORMAT.format(new Date()) + ".obj", true);
//    }
//
//    private void exportLocalPlayerModel() throws IOException	{
//        Player localPlayer = client.getLocalPlayer();
//        if (config.forceRestPose())
//        {
//            localPlayer.setAnimation(2566);
//            localPlayer.setAnimationFrame(0);
//        }
//        DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        export(localPlayer.getModel(), "Player " + client.getLocalPlayer().getName() + " " + TIME_FORMAT.format(new Date()) + ".obj", true);
//    }
//
//    private void exportObjectModel(String menuTarget, int id) throws IOException {
//        Scene scene = client.getScene();
//        Tile[][][] tiles = scene.getTiles();
//        log.warn("Trying to export object model");
//        int z = client.getPlane();
//        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
//            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
//                Tile tile = tiles[z][x][y];
//                if (tile != null) {
//                    GameObject[] gameObjects = tile.getGameObjects();
//                    Collection<GroundItem> groundItemsOnTile = groundItems.row(tile.getWorldLocation()).values();
//
//                    for (int i = 0; i < gameObjects.length; i++) {
//                        if (gameObjects[i] != null && gameObjects[i].getId() == id) {
//                            if (gameObjects[i].getRenderable() != null && gameObjects[i].getRenderable() instanceof Model) {
//                                DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                                export((Model)gameObjects[i].getRenderable(), "Object " + Text.removeFormattingTags(menuTarget) + " " + TIME_FORMAT.format(new Date()) + ".obj", false);
//                            }else {
//                                if (gameObjects[i].getRenderable() != null && gameObjects[i].getRenderable().getModel() != null) {
//                                    DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                                    export(gameObjects[i].getRenderable().getModel(), "Object " + Text.removeFormattingTags(menuTarget) + " " + TIME_FORMAT.format(new Date()) + ".obj", false);
//                                }
//                            }
//                        }
//                    }
//
//                    for (Iterator<GroundItem> iterator = groundItemsOnTile.iterator(); iterator.hasNext(); ) {
//                        GroundItem groundItem = iterator.next();
//
//                        if (groundItem.getId() == id && groundItem.getModel() != null) {
//                            DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                            export(groundItem.getModel(), "Item " + Text.removeFormattingTags(menuTarget) + " " + TIME_FORMAT.format(new Date()) + ".obj", false);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void exportNpcModel(String menuTarget, int identifier) throws IOException	{
//        NPC npc = client.getCachedNPCs()[identifier];
//        npc.setAnimation(npc.getIdlePoseAnimation());
//        npc.setAnimationFrame(0);
//        export(npc.getModel(), "NPC " + Text.removeFormattingTags(menuTarget) + ".obj", true);
//    }
//
//    private void exportPetModel(String menuTarget, int identifier) throws IOException	{
//        NPC npc=null;
//        for(NPC npC:client.getNpcs())
//        {
//            if(npC.getId() == identifier)
//            {
//                npc = npC;
//            }
//        }
//
//        DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        if(npc!=null)
//        {
//            export(npc.getModel(), "Pet " + Text.removeFormattingTags(menuTarget) + " " + TIME_FORMAT.format(new Date()) + ".obj", true);
//        }
//    }
//
//    private void exportPlayerModel(String menuTarget) throws IOException	{
//        Pattern REMOVE_TAGS_SECONDARY = Pattern.compile("\\(.+?\\)");
//        Matcher m = REMOVE_TAGS_SECONDARY.matcher(menuTarget);
//        String trgt = m.replaceAll("");
//        trgt = Text.sanitize(Text.removeFormattingTags(trgt.trim()));
//
//        for (int i = 0; i < client.getPlayers().size(); i++)
//        {
//            if (client.getPlayers().get(i).getName().equals(trgt))
//            {
//                DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                export(client.getPlayers().get(i).getModel(), "Player " + trgt + " " + TIME_FORMAT.format(new Date()) + ".obj", true);
//            }
//        }
//    }
//
//    public void exportRegion() throws  IOException {
//        Scene scene = client.getScene();
//        int regionID = lastTileOnMenuOpen.getWorldLocation().getRegionID();
//        Tile[][][] tiles = scene.getTiles();
//        int[][][] heights = client.getTileHeights();
//        for (int z = 0; z < tiles.length; ++z) {
//            for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
//                for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
//                    Tile tile = tiles[z][x][y];
//                    if (tile != null && tile.getWorldLocation().getRegionID() == regionID) {
//                        String name = "";
//                        String tileFormat = String.format("(%d) [%d,%d,%d] ",regionID, tile.getWorldLocation().getRegionX(),tile.getWorldLocation().getRegionY(),z);
//                        if (tile.getWallObject() != null) {
//                            ObjectComposition wComp = client.getObjectDefinition(tile.getWallObject().getId());
//                            name = wComp.getName();
//                            if(name == null || name == "null"){
//                                name = ""+tile.getWallObject().getId();
//                            }
//                            TryExportRenderable(tileFormat + "Wall1_" + name, tile.getWallObject().getRenderable1(), false);
//                            TryExportRenderable(tileFormat + "Wall2_" + name, tile.getWallObject().getRenderable2(), false);
//                        }
//                        if (tile.getDecorativeObject() != null) {
//                            ObjectComposition dComp = client.getObjectDefinition(tile.getDecorativeObject().getId());
//                            name = dComp.getName();
//                            if(name == null || name == "null"){
//                                name = ""+tile.getDecorativeObject().getId();
//                            }
//                            TryExportRenderable(tileFormat + "Decorative1_" + name, tile.getDecorativeObject().getRenderable(), false);
//                            TryExportRenderable(tileFormat + "Decorative2_" + name, tile.getDecorativeObject().getRenderable2(), false);
//                        }
//                        if (tile.getGroundObject() != null) {
//                            ObjectComposition gComp = client.getObjectDefinition(tile.getGroundObject().getId());
//                            name = gComp.getName();
//                            if(name == null || name == "null"){
//                                name = ""+tile.getGroundObject().getId();
//                            }
//                            TryExportRenderable(tileFormat + "Ground_" + name, tile.getGroundObject().getRenderable(), false);
//                        }
//                        if (tile.getGameObjects() != null) {
//                            for (GameObject go : tile.getGameObjects()) {
//                                if (go == null) {
//                                    continue;
//                                }
//                                ObjectComposition goComp = client.getObjectDefinition(go.getId());
//                                name = Text.removeFormattingTags(goComp.getName());
//                                if(name == null || name == "null"){
//                                    name = ""+go.getId();
//                                }
//                                TryExportRenderable(tileFormat + "GameObject_" + name, go.getRenderable(), false);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public void export(Model model, String name, boolean exportWithTextures) throws IOException	{
//        name = name.replace(" ", "_");
//        StringBuilder modelData = new StringBuilder("mtllib " + name.replace(".obj", ".mtl"));
////		String materialData = "";
//        modelData.append("\no ").append(name).append("\n");
//
//        final int[] color1s = model.getFaceColors1();
//        final int[] color2s = model.getFaceColors2();
//        final int[] color3s = model.getFaceColors3();
//
//        for (int i = 0; i < model.getVerticesCount(); ++i) {
//            int vx = model.getVerticesX()[i];
//            int	vy = model.getVerticesY()[i] * -1;
//            int vz = model.getVerticesZ()[i] * -1;
//            modelData.append(String.format("v %d %d %d\n", vx, vy, vz));
//        }
//
//        boolean textured = false;
//        if(exportWithTextures && model.getFaceTextures() != null) {
//            textured = true;
//            log.info("Has Face Textures " + model.getFaceTextures().length);
//            short[] faceTextures = model.getFaceTextures();
//            TextureProvider provider = client.getTextureProvider();
//            for (int i = 0; i < faceTextures.length; ++i) {
//
//                short textureID = faceTextures[i];
//                if (textureID < 0) {
//                    continue;
//                }
//                log.info("Texture ID: " + textureID);
//                int[] pixels = provider.load(textureID);
//                if (pixels == null) {
//                    continue;
//                }
//                int textureSize = (int) Math.sqrt(pixels.length);
//                log.info("Texture Size: " + textureSize);
//                BufferedImage texture = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_RGB);
//                for (int x = 0; x < textureSize; ++x) {
//                    for (int y = 0; y < textureSize; ++y) {
//                        texture.setRGB(x,y, pixels[y * textureSize + x]);
//                    }
//                }
//                File imageFile = new File(RuneLite.RUNELITE_DIR + "//models//", "Texture " + i + " " + name.replace(".obj", ".png"));
//                ImageIO.write(texture, "png", imageFile);
//            }
//
//            log.info("U: " + model.getFaceTextureUCoordinates().length);
//            log.info("V: " + model.getFaceTextureVCoordinates().length);
//            for (int i = 0; i < model.getFaceTextureUCoordinates().length; ++i){
//                if(model.getFaceTextureUCoordinates()[i] == null || model.getFaceTextureVCoordinates()[i] == null)
//                {
//                    modelData.append(String.format("vt %s %s\n", 0,0));
//                    modelData.append(String.format("vt %s %s\n", 0,0));
//                    modelData.append(String.format("vt %s %s\n", 0,0));
//                }else{
//                    for (int j = 0; j < model.getFaceTextureUCoordinates()[i].length; ++j){
//                        modelData.append(String.format("vt %s %s\n", model.getFaceTextureUCoordinates()[i][j],
//                                model.getFaceTextureVCoordinates()[i][j]));
//                    }
//                }
//            }
//
//            int faceOffset = 0;//3 * (model.getTrianglesCount()-1) + 2 + 1;
//            modelData.append("usemtl Texture " + name + "\n");
//            for (int face = 0; face < model.getTrianglesCount(); ++face) {
//                int x = model.getTrianglesX()[face] + 1;
//                int y = model.getTrianglesY()[face] + 1;
//                int z = model.getTrianglesZ()[face] + 1;
//                modelData.append("f ").append(x).append("/").append(faceOffset + (3*face+0)+1).append(" ").append(y).append("/").append(faceOffset + (3*face+1)+1).append(" ").append(z).append("/").append(faceOffset + (3*face+2)+1).append("\n");
//                modelData.append("" + "\n");
//            }
//
//        }else{
//            double offSet = 0.015625;
//            double step = 0.03125;
//            int uvMax = 31;
//            if(model.getTrianglesCount() > 1024){
//
//            }else if (model.getTrianglesCount() > 4096){
//
//            }else {
//
//            }
//
//            int u = 0;
//            int v = 0;
//            log.info(""+model.getTrianglesCount());
//            for (int face = 0; face < model.getTrianglesCount(); ++face){
//                String uvMap = String.format("vt %s %s\n", offSet + u * step, offSet + v * step);
//                modelData.append(uvMap);
//                modelData.append(uvMap);
//                modelData.append(uvMap);
//
//                u++;
//                if(u > uvMax){
//                    u = 0;
//                    v++;
//                }
//                log.info(u+" "+v);
//            }
//
//            modelData.append("usemtl " + name + "\n");
//            int pixelX = 0;
//            int pixelY = 0;
//            BufferedImage image = new BufferedImage(uvMax+1,uvMax+1, BufferedImage.TYPE_INT_RGB);
//            for (int face = 0; face < model.getTrianglesCount(); ++face)
//            {
//                int x = model.getTrianglesX()[face] + 1;
//                int y = model.getTrianglesY()[face] + 1;
//                int z = model.getTrianglesZ()[face] + 1;
//
//                Color color1 = new Color(JagexColor.HSLtoRGB((short)color1s[face], JagexColor.BRIGTHNESS_MIN));
//                Color color2 = new Color(JagexColor.HSLtoRGB((short)color2s[face], JagexColor.BRIGTHNESS_MIN));
//                Color color3 = new Color(JagexColor.HSLtoRGB((short)color3s[face], JagexColor.BRIGTHNESS_MIN));
//                double r = (color1.getRed() / 255.0 + color2.getRed() / 255.0 + color3.getRed() / 255.0) / 3;
//                double g = (color1.getGreen() / 255.0 + color2.getGreen() / 255.0 + color3.getGreen() / 255.0) / 3;
//                double b = (color1.getBlue() / 255.0 + color2.getBlue() / 255.0 + color3.getBlue() / 255.0) / 3;
//
//                Color c = new Color((float)r,(float)g,(float)b);
//                image.setRGB(pixelX,uvMax - pixelY,c.getRGB());
//                pixelX++;
//                if(pixelX > uvMax){
//                    pixelX = 0;
//                    pixelY++;
//                }
////			materialData += String.format("Kd %.4f %.4f %.4f\n", r, g, b);
//                modelData.append("f ").append(x).append("/").append((3*face+0)+1).append(" ").append(y).append("/").append((3*face+1)+1).append(" ").append(z).append("/").append((3*face+2)+1).append("\n");
//                modelData.append("" + "\n");
//            }
//
//            File imageFile = new File(RuneLite.RUNELITE_DIR + "//models//", name.replace(".obj", ".png"));
//            ImageIO.write(image, "png", imageFile);
//        }
//
//        String path = RuneLite.RUNELITE_DIR + "//models//";
//        File outputDir = new File(path);
//        if(!outputDir.exists())
//        {
//            outputDir.mkdirs();
//        }
//
//        File modelOutput = new File(path + name);
//        FileWriter modelWriter = new FileWriter(modelOutput);
//        modelWriter.write(modelData.toString());
//        modelWriter.flush();
//        modelWriter.close();
//
//
////		if(config.material())
////		{
////			File materialOutput = new File(path + name.replace(".obj", ".mtl"));
////			FileWriter materialWriter = new FileWriter(materialOutput);
////			materialWriter.write(materialData);
////			materialWriter.flush();
////			materialWriter.close();
////		}
//    }
//
//    public  void exportTile(SceneTileModel model, String name) throws  IOException{
//        if(model == null){return;}
//        name = name.replace(" ", "_");
//        StringBuilder modelData = new StringBuilder("mtllib " + name.replace(".obj", ".mtl"));
//        modelData.append("\no ").append(name).append("\n");
//
//        final int[] color1s = model.getTriangleColorA();
//        final int[] color2s = model.getTriangleColorB();
//        final int[] color3s = model.getTriangleColorC();
//
//        for (int i = 0; i < model.getVertexX().length; ++i) {
//            int vx = model.getVertexX()[i];
//            int	vy = model.getVertexY()[i] * -1;
//            int vz = model.getVertexZ()[i] * -1;
//            modelData.append(String.format("v %d %d %d\n", vx, vy, vz));
//        }
//
//        double offSet = 0.015625;
//        double step = 0.03125;
//        int uvMax = 31;
//        int u = 0;
//        int v = 0;
//        log.info(""+model.getFaceX().length);
//        for (int face = 0; face < model.getFaceX().length; ++face){
//            String uvMap = String.format("vt %s %s\n", offSet + u * step, offSet + v * step);
//            modelData.append(uvMap);
//            modelData.append(uvMap);
//            modelData.append(uvMap);
//
//            u++;
//            if(u > uvMax){
//                u = 0;
//                v++;
//            }
//            log.info(u+" "+v);
//        }
//
//        modelData.append("usemtl " + name + "\n");
//        int pixelX = 0;
//        int pixelY = 0;
//        BufferedImage image = new BufferedImage(uvMax+1,uvMax+1, BufferedImage.TYPE_INT_RGB);
//        for (int face = 0; face < model.getFaceX().length; ++face)
//        {
//            int x = model.getFaceX()[face] + 1;
//            int y = model.getFaceY()[face] + 1;
//            int z = model.getFaceZ()[face] + 1;
//
//            Color color1 = new Color(JagexColor.HSLtoRGB((short)color1s[face], JagexColor.BRIGTHNESS_MIN));
//            Color color2 = new Color(JagexColor.HSLtoRGB((short)color2s[face], JagexColor.BRIGTHNESS_MIN));
//            Color color3 = new Color(JagexColor.HSLtoRGB((short)color3s[face], JagexColor.BRIGTHNESS_MIN));
//            double r = (color1.getRed() / 255.0 + color2.getRed() / 255.0 + color3.getRed() / 255.0) / 3;
//            double g = (color1.getGreen() / 255.0 + color2.getGreen() / 255.0 + color3.getGreen() / 255.0) / 3;
//            double b = (color1.getBlue() / 255.0 + color2.getBlue() / 255.0 + color3.getBlue() / 255.0) / 3;
//
//            Color c = new Color((float)r,(float)g,(float)b);
//            image.setRGB(pixelX,uvMax - pixelY,c.getRGB());
//            pixelX++;
//            if(pixelX > uvMax){
//                pixelX = 0;
//                pixelY++;
//            }
////			materialData += String.format("Kd %.4f %.4f %.4f\n", r, g, b);
//            modelData.append("f ").append(x).append("/").append((3*face+0)+1).append(" ").append(y).append("/").append((3*face+1)+1).append(" ").append(z).append("/").append((3*face+2)+1).append("\n");
//            modelData.append("" + "\n");
//        }
//
//        File imageFile = new File(RuneLite.RUNELITE_DIR + "//models//", name + ".png");
//        ImageIO.write(image, "png", imageFile);
//
//        String path = RuneLite.RUNELITE_DIR + "//models//";
//        File outputDir = new File(path);
//        if(!outputDir.exists())
//        {
//            outputDir.mkdirs();
//        }
//
//        File modelOutput = new File(path + name);
//        FileWriter modelWriter = new FileWriter(modelOutput);
//        modelWriter.write(modelData.toString());
//        modelWriter.flush();
//        modelWriter.close();
//    }
//
//    public  void exportTilePaint(SceneTilePaint paint, String name) throws IOException{
//        if(paint == null){log.info("There is no paint here"); return;}
//        int rbg = paint.getRBG();
//        BufferedImage paintRBG = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
//        for (int u = 0; u < 8; ++u) {
//            for (int v = 0; v < 8; ++v) {
//                paintRBG.setRGB(u, v, rbg);
//            }
//        }
//
//        File paintFile = new File(RuneLite.RUNELITE_DIR + "//models//", "Paint_" + name + ".png");
//        ImageIO.write(paintRBG, "png", paintFile);
//
//        if(paint.getTexture() > 0) {
//            int[] pixels = client.getTextureProvider().load(paint.getTexture());
//            if (pixels != null) {
//                int textureSize = (int) Math.sqrt(pixels.length);
//                log.info("Tile Paint Texture Size: " + textureSize);
//                BufferedImage texture = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_RGB);
//                for (int x = 0; x < textureSize; ++x) {
//                    for (int y = 0; y < textureSize; ++y) {
//                        texture.setRGB(x, textureSize - y - 1, pixels[y * textureSize + x]);
//                    }
//                }
//                File imageFile = new File(RuneLite.RUNELITE_DIR + "//models//", "Texture " + " " + name + ".png");
//                ImageIO.write(texture, "png", imageFile);
//            }
//        }
//    }
//
//    @Subscribe
//    public void onGameStateChanged(GameStateChanged event)	{
//        if (event.getGameState() == GameState.LOADING)
//        {
//            groundItems.clear();
//        }
//    }
//
//    // borrowed from official Ground Items plugin
//    @Subscribe
//    public void onItemSpawned(ItemSpawned itemSpawned)	{
//        TileItem item = itemSpawned.getItem();
//        Tile tile = itemSpawned.getTile();
//
//        final GroundItem groundItem = GroundItem.builder()
//                .id(item.getId())
//                .item(item)
//                .location(tile.getWorldLocation())
//                .quantity(item.getQuantity())
//                .build();
//
//        GroundItem existing = groundItems.get(tile.getWorldLocation(), item.getId());
//        if (existing != null)
//        {
//            existing.setQuantity(existing.getQuantity() + groundItem.getQuantity());
//        }
//        else
//        {
//            groundItems.put(tile.getWorldLocation(), item.getId(), groundItem);
//        }
//    }
//
//    // borrowed from official Ground Items plugin
//    @Subscribe
//    public void onItemDespawned(ItemDespawned itemDespawned)	{
//        TileItem item = itemDespawned.getItem();
//        Tile tile = itemDespawned.getTile();
//
//        GroundItem groundItem = groundItems.get(tile.getWorldLocation(), item.getId());
//        if (groundItem == null)
//        {
//            return;
//        }
//
//        if (groundItem.getQuantity() <= item.getQuantity())
//        {
//            groundItems.remove(tile.getWorldLocation(), item.getId());
//        }
//        else
//        {
//            groundItem.setQuantity(groundItem.getQuantity() - item.getQuantity());
//        }
//    }
//
//    // borrowed from official Ground Items plugin
//    @Subscribe
//    public void onItemQuantityChanged(ItemQuantityChanged itemQuantityChanged)	{
//        TileItem item = itemQuantityChanged.getItem();
//        Tile tile = itemQuantityChanged.getTile();
//        int oldQuantity = itemQuantityChanged.getOldQuantity();
//        int newQuantity = itemQuantityChanged.getNewQuantity();
//
//        int diff = newQuantity - oldQuantity;
//        GroundItem groundItem = groundItems.get(tile.getWorldLocation(), item.getId());
//        if (groundItem != null)
//        {
//            groundItem.setQuantity(groundItem.getQuantity() + diff);
//        }
//    }
//
//    //The following code is taken from the Pet Info plugin by Micro Tavor with permission
//    @Subscribe
//    public void onClientTick(ClientTick clientTick)	{
//        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
//        {
//            return;
//        }
//        else if(client.isKeyPressed(KeyCode.KC_SHIFT))
//        {
//            addMenus();
//        }
//    }
//
//    private void addMenus()	{
//        Point mouseCanvasPosition = client.getMouseCanvasPosition();
//
//        java.util.List<NPC> petsUnderCursor = getPetsUnderCursor(mouseCanvasPosition);
//        if (!petsUnderCursor.isEmpty())
//        {
//            for (NPC pet : petsUnderCursor)
//            {
//                addPetInfoMenu(pet);
//            }
//        }
//    }
//
//    private void addPetInfoMenu(NPC pet)	{
//        final MenuEntry exportMenuEntry = new MenuEntry();
//        exportMenuEntry.setOption(EXPORT_MODEL);
//        exportMenuEntry.setTarget(pet.getName());
//        exportMenuEntry.setType(MenuAction.RUNELITE.getId());
//        exportMenuEntry.setIdentifier(pet.getId());
//        exportMenuEntry.setParam1(4);
//        addEntry(exportMenuEntry);
//    }
//
//    private final java.util.List<NPC> pets = new ArrayList<>();
//
//    private List<NPC> getPetsUnderCursor(Point mouseCanvasPosition)	{
//        return pets.stream().filter(p -> {
//            return isClickable(p, mouseCanvasPosition);
//        }).collect(Collectors.toList());
//    }
//
//    @Subscribe
//    public void onNpcSpawned(NpcSpawned npcSpawned)	{
//        NPC npc = npcSpawned.getNpc();
//        Pet pet = Pet.findPet(npc.getId());
//
//        if (pet != null)
//        {
//            pets.add(npc);
//        }
//    }
//
//    private boolean isClickable(NPC npc, Point mouseCanvasPosition)	{
//        Shape npcHull = npc.getConvexHull();
//
//        if (npcHull != null)
//        {
//            return npcHull.contains(mouseCanvasPosition.getX(), mouseCanvasPosition.getY());
//        }
//
//        return false;
//    }
//
//    public void Log(String message){
//        log.info(message);
//    }
}
