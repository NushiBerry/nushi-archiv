package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.inspect.ArchivStructureVoxelSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.Locale;

/**
 * Isometric renderer for .schem/.schematic structure previews.
 *
 * KEY: there is NO flipVertically call here. The painter's algorithm
 * already draws in correct screen order (ground at bottom, top at top).
 * Adding a flip would invert the entire image — do NOT add it back.
 *
 * Rendering strategy:
 *  - Only renders exposed surface blocks (blocks with at least one air neighbor)
 *  - Uses real Minecraft textures from the active resource pack
 *  - Separate top and side textures for blocks that have them (logs, grass, etc.)
 *  - Directional lighting: top face bright, right face medium, left face dark
 *  - Painter's algorithm: bottom-to-top layers, back-to-front within each layer
 *  - Large structures are sampled (stepX/Y/Z > 1) to keep render time reasonable
 */
public class ArchivIsometricRenderer {

    // Output resolution
    private static final int OUTPUT_WIDTH  = 960;
    private static final int OUTPUT_HEIGHT = 540;

    // Isometric tile size — all face geometry is derived from these two constants
    private static final int TILE_W     = 32;  // tile width in pixels
    private static final int TILE_H     = 16;  // top-face diamond height  (= TILE_W / 2)
    private static final int TILE_DEPTH = 16;  // side-face height

    // Maximum voxel dimensions before sampling kicks in
    private static final int MAX_DIM = 56;

    // Directional lighting factors (0.0 = black, 1.0 = full brightness)
    private static final float LIGHT_TOP   = 1.00f;
    private static final float LIGHT_RIGHT = 0.78f;
    private static final float LIGHT_LEFT  = 0.58f;

    // Texture caches (null = texture not found, use color fallback)
    private final Map<String, BufferedImage> topCache  = new HashMap<>();
    private final Map<String, BufferedImage> sideCache = new HashMap<>();

    // =========================================================================
    // Entry point
    // =========================================================================

    public Path render(ArchivStructureVoxelSnapshot snapshot, Path outputPath) throws IOException {
        if (snapshot == null || !snapshot.isReadable() || snapshot.getNonAirBlocks() == 0) {
            return null;
        }

        // Restrict to the useful (non-air) bounds
        int x0 = snapshot.hasUsefulBounds() ? snapshot.getMinX() : 0;
        int y0 = snapshot.hasUsefulBounds() ? snapshot.getMinY() : 0;
        int z0 = snapshot.hasUsefulBounds() ? snapshot.getMinZ() : 0;
        int x1 = snapshot.hasUsefulBounds() ? snapshot.getMaxX() : snapshot.getWidth()  - 1;
        int y1 = snapshot.hasUsefulBounds() ? snapshot.getMaxY() : snapshot.getHeight() - 1;
        int z1 = snapshot.hasUsefulBounds() ? snapshot.getMaxZ() : snapshot.getLength() - 1;

        int rangeX = x1 - x0 + 1;
        int rangeY = y1 - y0 + 1;
        int rangeZ = z1 - z0 + 1;

        // Sampling step so we never render more than MAX_DIM blocks per axis
        int stepX = Math.max(1, (int) Math.ceil((double) rangeX / MAX_DIM));
        int stepY = Math.max(1, (int) Math.ceil((double) rangeY / MAX_DIM));
        int stepZ = Math.max(1, (int) Math.ceil((double) rangeZ / MAX_DIM));

        int cols   = (int) Math.ceil((double) rangeX / stepX);
        int rows   = (int) Math.ceil((double) rangeZ / stepZ);
        int layers = (int) Math.ceil((double) rangeY / stepY);

        // Canvas size — must fit the entire projected isometric grid
        int canvasW = (cols + rows) * (TILE_W / 2) + TILE_W + 64;
        int canvasH = (cols + rows) * (TILE_H / 2) + layers * TILE_DEPTH + TILE_H + 64;

        // Draw onto an intermediate transparent canvas
        BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.clearRect(0, 0, canvasW, canvasH);

        // Isometric origin: top-left corner, offset down by total layer height
        // so that even the tallest structure fits without clipping at the top.
        int originX = rows * (TILE_W / 2) + 32;
        int originY = 32 + layers * TILE_DEPTH;

        // Draw the ground shadow first (always behind all blocks)
        drawShadow(g, originX, originY, cols, rows);

        // Collect and sort visible blocks, then draw them back-to-front
        List<RenderBlock> blocks = collectVisibleBlocks(
                snapshot, x0, y0, z0, x1, y1, z1, stepX, stepY, stepZ, cols, rows, layers);

        for (RenderBlock block : blocks) {
            // Convert grid indices to isometric screen position:
            // xi increases → screen goes right and slightly down
            // zi increases → screen goes left and slightly down
            // yi increases → screen goes up (subtracting TILE_DEPTH)
            int sx = originX + (block.xi - block.zi) * (TILE_W / 2);
            int sy = originY + (block.xi + block.zi) * (TILE_H / 2) - block.yi * TILE_DEPTH;
            drawBlock(g, sx, sy, block);
        }

        g.dispose();

        // Composite onto final output with solid background
        BufferedImage output = new BufferedImage(OUTPUT_WIDTH, OUTPUT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D out = output.createGraphics();
        out.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Scale canvas to fit, with 5% padding on all sides
        double scale  = Math.min((double) OUTPUT_WIDTH / canvasW, (double) OUTPUT_HEIGHT / canvasH) * 0.90;
        int drawW = Math.max(1, (int) Math.round(canvasW * scale));
        int drawH = Math.max(1, (int) Math.round(canvasH * scale));
        int drawX = (OUTPUT_WIDTH  - drawW) / 2;
        int drawY = (OUTPUT_HEIGHT - drawH) / 2;

        out.drawImage(canvas, drawX, drawY, drawW, drawH, null);
        out.dispose();

        // IMPORTANT: do NOT call flipVertically here.
        // The math above already places ground at the bottom and the top of the
        // structure at the top of the image. Flipping it would invert everything.
        return saveImage(output, outputPath);
    }

    // =========================================================================
    // Block collection — painter's algorithm order
    // =========================================================================

    private List<RenderBlock> collectVisibleBlocks(
            ArchivStructureVoxelSnapshot snap,
            int x0, int y0, int z0,
            int x1, int y1, int z1,
            int sx, int sy, int sz,
            int cols, int rows, int layers
    ) {
        List<RenderBlock> result = new ArrayList<>();

        // Draw order: yi ascending (ground first), zi descending (back first), xi ascending
        for (int yi = 0; yi < layers; yi++) {
            int wy = y0 + yi * sy;

            for (int zi = rows - 1; zi >= 0; zi--) {
                int wz = z0 + zi * sz;

                for (int xi = 0; xi < cols; xi++) {
                    int wx = x0 + xi * sx;

                    String bs = snap.getBlockState(wx, wy, wz);
                    if (ArchivStructureVoxelSnapshot.isAirBlock(bs)) continue;

                    // A face is visible if its neighbour is air or outside the structure bounds
                    boolean topVis   = airAt(snap, wx,      wy + sy, wz,      x0, y0, z0, x1, y1, z1);
                    boolean leftVis  = airAt(snap, wx - sx, wy,      wz,      x0, y0, z0, x1, y1, z1)
                            || airAt(snap, wx,      wy,      wz + sz, x0, y0, z0, x1, y1, z1);
                    boolean rightVis = airAt(snap, wx + sx, wy,      wz,      x0, y0, z0, x1, y1, z1)
                            || airAt(snap, wx,      wy,      wz - sz, x0, y0, z0, x1, y1, z1);

                    // Skip fully buried blocks — nothing to draw
                    if (!topVis && !leftVis && !rightVis) continue;

                    result.add(new RenderBlock(xi, yi, zi, bs, topVis, leftVis, rightVis));
                }
            }
        }

        return result;
    }

    private boolean airAt(ArchivStructureVoxelSnapshot snap,
                          int x, int y, int z,
                          int x0, int y0, int z0, int x1, int y1, int z1) {
        if (x < x0 || x > x1 || y < y0 || y > y1 || z < z0 || z > z1) return true;
        return ArchivStructureVoxelSnapshot.isAirBlock(snap.getBlockState(x, y, z));
    }

    // =========================================================================
    // Shadow (drawn before all blocks so it's always behind them)
    // =========================================================================

    private void drawShadow(Graphics2D g, int originX, int originY, int cols, int rows) {
        int ellipseW = Math.max(84, (cols + rows) * (TILE_W / 3));
        int ellipseH = Math.max(22, ellipseW / 4);
        int sx = originX - ellipseW / 2 + TILE_W / 2;
        int sy = originY + TILE_H / 2 + 4;

        // Dark semi-transparent ellipse — NOT white
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(sx, sy, ellipseW, ellipseH);
        g.setColor(new Color(0, 0, 0, 25));
        g.fillOval(sx - 8, sy - 4, ellipseW + 16, ellipseH + 8);
    }

    // =========================================================================
    // Single block rendering
    // =========================================================================

    private void drawBlock(Graphics2D g, int sx, int sy, RenderBlock block) {
        BufferedImage topTex  = getTopTexture(block.bs);
        BufferedImage sideTex = getSideTexture(block.bs);
        Color base = baseColor(block.bs, topTex != null ? topTex : sideTex);

        // Diamond top face
        int[] tx = { sx,          sx + TILE_W/2, sx + TILE_W, sx + TILE_W/2 };
        int[] ty = { sy,          sy - TILE_H/2, sy,          sy + TILE_H/2  };

        // Left parallelogram (darker)
        int[] lx = { sx,          sx + TILE_W/2, sx + TILE_W/2, sx           };
        int[] ly = { sy,          sy + TILE_H/2, sy + TILE_H/2 + TILE_DEPTH, sy + TILE_DEPTH };

        // Right parallelogram (medium)
        int[] rx = { sx + TILE_W/2, sx + TILE_W, sx + TILE_W, sx + TILE_W/2 };
        int[] ry = { sy + TILE_H/2, sy,           sy + TILE_DEPTH, sy + TILE_H/2 + TILE_DEPTH };

        // Draw left face first (behind right and top)
        if (block.leftVis) {
            if (sideTex != null) {
                drawTexturedQuad(g, sideTex, lx, ly);
                shadeQuad(g, lx, ly, 1.0f - LIGHT_LEFT);
            } else {
                g.setColor(darken(base, LIGHT_LEFT));
                g.fillPolygon(lx, ly, 4);
            }
            g.setColor(new Color(0, 0, 0, 55));
            g.drawPolygon(lx, ly, 4);
        }

        // Right face
        if (block.rightVis) {
            if (sideTex != null) {
                drawTexturedQuad(g, sideTex, rx, ry);
                shadeQuad(g, rx, ry, 1.0f - LIGHT_RIGHT);
            } else {
                g.setColor(darken(base, LIGHT_RIGHT));
                g.fillPolygon(rx, ry, 4);
            }
            g.setColor(new Color(0, 0, 0, 40));
            g.drawPolygon(rx, ry, 4);
        }

        // Top face (drawn last so it's on top of both side faces)
        if (block.topVis) {
            if (topTex != null) {
                drawTexturedQuad(g, topTex, tx, ty);
            } else {
                g.setColor(darken(base, LIGHT_TOP));
                g.fillPolygon(tx, ty, 4);
            }
            g.setColor(new Color(0, 0, 0, 35));
            g.drawPolygon(tx, ty, 4);
        }
    }

    /**
     * Maps a rectangular texture onto a quadrilateral via AffineTransform.
     *
     * px[0]/py[0] = top-left of the face on screen
     * px[1]/py[1] = top-right
     * px[3]/py[3] = bottom-left
     */
    private void drawTexturedQuad(Graphics2D g, BufferedImage tex, int[] px, int[] py) {
        Graphics2D tg = (Graphics2D) g.create();
        tg.setClip(new Polygon(px, py, 4));
        tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        double tw = tex.getWidth();
        double th = tex.getHeight();

        // Map texture corners to face corners:
        //   texture (0,0) → screen px[0],py[0]  (top-left)
        //   texture (tw,0) → screen px[1],py[1] (top-right)
        //   texture (0,th) → screen px[3],py[3] (bottom-left)
        AffineTransform at = new AffineTransform(
                (px[1] - px[0]) / tw, (py[1] - py[0]) / tw,
                (px[3] - px[0]) / th, (py[3] - py[0]) / th,
                px[0], py[0]
        );

        tg.drawRenderedImage(tex, at);
        tg.dispose();
    }

    /** Overlays a semi-transparent black quad to darken a face. */
    private void shadeQuad(Graphics2D g, int[] px, int[] py, float alpha) {
        if (alpha <= 0) return;
        Graphics2D sg = (Graphics2D) g.create();
        sg.setColor(new Color(0f, 0f, 0f, Math.min(1.0f, alpha)));
        sg.fillPolygon(px, py, 4);
        sg.dispose();
    }

    // =========================================================================
    // Texture loading
    // =========================================================================

    private BufferedImage getTopTexture(String bs) {
        if (topCache.containsKey(bs)) return topCache.get(bs);
        BufferedImage img = loadTexture(bs, true);
        topCache.put(bs, img);
        return img;
    }

    private BufferedImage getSideTexture(String bs) {
        if (sideCache.containsKey(bs)) return sideCache.get(bs);
        BufferedImage img = loadTexture(bs, false);
        sideCache.put(bs, img);
        return img;
    }

    private BufferedImage loadTexture(String bs, boolean topFace) {
        if (bs == null || bs.startsWith("legacy:") || bs.startsWith("unknown:")) return null;

        // Strip blockstate properties: "minecraft:oak_stairs[facing=north]" → "oak_stairs"
        String raw  = bs.contains("[") ? bs.substring(0, bs.indexOf('[')) : bs;
        String name = raw.contains(":") ? raw.substring(raw.indexOf(':') + 1) : raw;

        // Build candidate texture names in priority order
        List<String> candidates = buildCandidates(name, topFace);

        for (String candidate : candidates) {
            BufferedImage img = fetchFromResourcePack(candidate, topFace);
            if (img != null) return img;
        }
        return null;
    }

    private List<String> buildCandidates(String name, boolean topFace) {
        LinkedHashSet<String> out = new LinkedHashSet<>();

        if (topFace) {
            String mapped = TOP_MAP.get(name);
            if (mapped != null) out.add(mapped);
            out.add(name + "_top");
            out.add(name);
            // Normalize variant suffixes: stairs/slab/wall → base block
            out.add(stripVariantSuffix(name) + "_top");
            out.add(stripVariantSuffix(name));
        } else {
            String mapped = SIDE_MAP.get(name);
            if (mapped != null) out.add(mapped);
            out.add(name + "_side");
            out.add(name);
            out.add(stripVariantSuffix(name) + "_side");
            out.add(stripVariantSuffix(name));
            out.add(stripVariantSuffix(name) + "_top");
        }

        return new ArrayList<>(out);
    }

    private String stripVariantSuffix(String name) {
        for (String suffix : new String[]{
                "_stairs", "_slab", "_wall", "_fence_gate", "_fence",
                "_button", "_pressure_plate", "_pane", "_trapdoor", "_door"
        }) {
            if (name.endsWith(suffix)) return name.substring(0, name.length() - suffix.length());
        }
        return name;
    }

    /**
     * Loads a PNG from the active Minecraft resource pack.
     * Top textures → TILE_W × TILE_W (square)
     * Side textures → TILE_W × TILE_DEPTH (matches the parallelogram face height)
     */
    private BufferedImage fetchFromResourcePack(String texName, boolean topFace) {
        if (texName == null || texName.isBlank()) return null;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return null;

            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/" + texName + ".png");
            try (InputStream stream = mc.getResourceManager().open(loc)) {
                BufferedImage raw = ImageIO.read(stream);
                if (raw == null) return null;
                return topFace ? scaleTo(raw, TILE_W, TILE_W) : scaleTo(raw, TILE_W, TILE_DEPTH);
            }
        } catch (Exception e) {
            return null; // texture not found — will use color fallback
        }
    }

    private BufferedImage scaleTo(BufferedImage src, int w, int h) {
        if (src.getWidth() == w && src.getHeight() == h) return src;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    // =========================================================================
    // Color fallback (used when no texture is available)
    // =========================================================================

    private Color baseColor(String bs, BufferedImage tex) {
        if (tex != null) return avgColor(tex);

        String s = bs == null ? "" : bs.toLowerCase(Locale.ROOT);

        if (s.contains("stone_brick"))  return new Color(115,115,115);
        if (s.contains("cobblestone"))  return new Color(115,115,115);
        if (s.contains("deepslate"))    return new Color( 84, 84, 98);
        if (s.contains("stone"))        return new Color(125,125,125);
        if (s.contains("tuff"))         return new Color(111,112, 96);
        if (s.contains("calcite"))      return new Color(224,226,224);
        if (s.contains("grass"))        return new Color( 95,159, 53);
        if (s.contains("podzol"))       return new Color(135, 96, 57);
        if (s.contains("mycelium"))     return new Color(110, 88,110);
        if (s.contains("dirt"))         return new Color(134, 96, 67);
        if (s.contains("mud"))          return new Color(100, 85, 70);
        if (s.contains("gravel"))       return new Color(130,127,125);
        if (s.contains("red_sand"))     return new Color(190,120, 68);
        if (s.contains("sand"))         return new Color(218,210,158);
        if (s.contains("clay"))         return new Color(155,155,165);
        if (s.contains("moss"))         return new Color( 90,120, 42);
        if (s.contains("cherry"))       return new Color(226,176,176);
        if (s.contains("dark_oak"))     return new Color( 66, 43, 20);
        if (s.contains("acacia"))       return new Color(169, 91, 53);
        if (s.contains("jungle"))       return new Color(150,109, 68);
        if (s.contains("birch"))        return new Color(216,203,158);
        if (s.contains("spruce"))       return new Color(114, 84, 48);
        if (s.contains("mangrove"))     return new Color(100, 41, 41);
        if (s.contains("bamboo"))       return new Color(180,190, 80);
        if (s.contains("oak"))          return new Color(162,130, 78);
        if (s.contains("planks")
                || s.contains("log")
                || s.contains("wood"))         return new Color(162,130, 78);
        if (s.contains("leaves"))       return new Color( 64,110, 30);
        if (s.contains("sandstone"))    return new Color(218,207,145);
        if (s.contains("netherrack"))   return new Color(114, 20, 20);
        if (s.contains("nether_brick")) return new Color( 50, 30, 30);
        if (s.contains("crimson"))      return new Color(135, 40, 60);
        if (s.contains("warped"))       return new Color( 40,120,110);
        if (s.contains("obsidian"))     return new Color( 25, 18, 37);
        if (s.contains("end_stone"))    return new Color(219,221,160);
        if (s.contains("purpur"))       return new Color(170,110,170);
        if (s.contains("blue_ice"))     return new Color(110,160,230);
        if (s.contains("packed_ice"))   return new Color(140,180,220);
        if (s.contains("ice"))          return new Color(150,180,230);
        if (s.contains("snow"))         return new Color(220,235,255);
        if (s.contains("water"))        return new Color( 63,118,228);
        if (s.contains("lava"))         return new Color(232,120, 30);
        if (s.contains("glass"))        return new Color(175,212,220);
        if (s.contains("netherite"))    return new Color( 70, 60, 65);
        if (s.contains("diamond"))      return new Color(100,224,213);
        if (s.contains("emerald"))      return new Color( 47,196, 78);
        if (s.contains("gold"))         return new Color(249,236, 79);
        if (s.contains("iron"))         return new Color(219,219,212);
        if (s.contains("copper"))       return new Color(196,127, 84);
        if (s.contains("lapis"))        return new Color( 45, 72,163);
        if (s.contains("redstone"))     return new Color(196, 40, 25);
        if (s.contains("quartz"))       return new Color(235,228,218);
        if (s.contains("amethyst"))     return new Color(150,100,200);
        if (s.contains("white"))        return new Color(235,235,235);
        if (s.contains("light_gray"))   return new Color(155,155,155);
        if (s.contains("gray"))         return new Color( 80, 80, 80);
        if (s.contains("black"))        return new Color( 25, 25, 25);
        if (s.contains("brown"))        return new Color(110, 75, 40);
        if (s.contains("red"))          return new Color(165, 48, 48);
        if (s.contains("orange"))       return new Color(220,130, 40);
        if (s.contains("yellow"))       return new Color(220,200, 50);
        if (s.contains("lime"))         return new Color( 90,175, 50);
        if (s.contains("green"))        return new Color( 55,110, 35);
        if (s.contains("cyan"))         return new Color( 20,140,150);
        if (s.contains("light_blue"))   return new Color( 95,165,210);
        if (s.contains("blue"))         return new Color( 45, 70,160);
        if (s.contains("purple"))       return new Color(120, 55,175);
        if (s.contains("magenta"))      return new Color(185, 75,185);
        if (s.contains("pink"))         return new Color(225,140,165);
        if (s.contains("concrete"))     return new Color(180,180,180);
        if (s.contains("terracotta"))   return new Color(159, 95, 66);
        if (s.contains("wool"))         return new Color(220,210,195);
        if (s.contains("glowstone"))    return new Color(250,230,110);
        if (s.contains("sea_lantern"))  return new Color(172,210,207);
        if (s.contains("prismarine"))   return new Color( 80,155,140);
        if (s.contains("magma"))        return new Color(160, 70, 30);
        if (s.contains("bedrock"))      return new Color( 80, 80, 80);

        return new Color(145, 145, 145);
    }

    private Color avgColor(BufferedImage img) {
        long r = 0, g = 0, b = 0, n = 0;
        int step = Math.max(1, img.getWidth() / 8);
        for (int y = 0; y < img.getHeight(); y += step)
            for (int x = 0; x < img.getWidth(); x += step) {
                int px = img.getRGB(x, y);
                if (((px >> 24) & 0xFF) < 10) continue;
                r += (px >> 16) & 0xFF;
                g += (px >>  8) & 0xFF;
                b +=  px        & 0xFF;
                n++;
            }
        if (n == 0) return new Color(145, 145, 145);
        return new Color((int)(r/n), (int)(g/n), (int)(b/n));
    }

    private Color darken(Color c, float f) {
        return new Color(
                Math.max(0, Math.min(255, (int)(c.getRed()   * f))),
                Math.max(0, Math.min(255, (int)(c.getGreen() * f))),
                Math.max(0, Math.min(255, (int)(c.getBlue()  * f))),
                c.getAlpha());
    }

    // =========================================================================
    // Texture lookup maps (block name → texture file name)
    // =========================================================================

    private static final Map<String, String> TOP_MAP  = new HashMap<>();
    private static final Map<String, String> SIDE_MAP = new HashMap<>();

    private static final List<String> MC_COLORS = List.of(
            "white","orange","magenta","light_blue","yellow","lime","pink",
            "gray","light_gray","cyan","purple","blue","brown","green","red","black");

    static {
        // Grass / dirt
        TOP_MAP.put("grass_block",  "grass_block_top");
        SIDE_MAP.put("grass_block", "grass_block_side");
        TOP_MAP.put("podzol",       "podzol_top");
        SIDE_MAP.put("podzol",      "podzol_side");
        TOP_MAP.put("mycelium",     "mycelium_top");
        SIDE_MAP.put("mycelium",    "mycelium_side");
        TOP_MAP.put("farmland",     "farmland_moist");
        SIDE_MAP.put("farmland",    "dirt");

        // Logs
        for (String w : new String[]{"oak","spruce","birch","jungle","acacia","dark_oak","mangrove","cherry"}) {
            TOP_MAP.put(w  + "_log",            w  + "_log_top");
            SIDE_MAP.put(w + "_log",            w  + "_log");
            TOP_MAP.put("stripped_"  + w + "_log",  "stripped_" + w + "_log_top");
            SIDE_MAP.put("stripped_" + w + "_log",  "stripped_" + w + "_log");
        }

        // Sandstone
        TOP_MAP.put("sandstone",       "sandstone_top");
        SIDE_MAP.put("sandstone",      "sandstone");
        TOP_MAP.put("red_sandstone",   "red_sandstone_top");
        SIDE_MAP.put("red_sandstone",  "red_sandstone");
        TOP_MAP.put("smooth_sandstone","sandstone_top");

        // Quartz
        TOP_MAP.put("quartz_block",   "quartz_block_top");
        SIDE_MAP.put("quartz_block",  "quartz_block_side");
        TOP_MAP.put("quartz_pillar",  "quartz_pillar_top");
        SIDE_MAP.put("quartz_pillar", "quartz_pillar");

        // Purpur
        TOP_MAP.put("purpur_pillar",  "purpur_pillar_top");
        SIDE_MAP.put("purpur_pillar", "purpur_pillar");

        // Deepslate
        TOP_MAP.put("deepslate",  "deepslate_top");
        SIDE_MAP.put("deepslate", "deepslate");

        // Crafting / utility
        TOP_MAP.put("crafting_table",  "crafting_table_top");
        SIDE_MAP.put("crafting_table", "crafting_table_front");
        TOP_MAP.put("furnace",         "furnace_top");
        SIDE_MAP.put("furnace",        "furnace_front_off");
        TOP_MAP.put("blast_furnace",   "blast_furnace_top");
        TOP_MAP.put("smoker",          "smoker_top");
        TOP_MAP.put("barrel",          "barrel_top");
        SIDE_MAP.put("barrel",         "barrel_side");
        TOP_MAP.put("hay_block",       "hay_block_top");
        SIDE_MAP.put("hay_block",      "hay_block_side");
        TOP_MAP.put("tnt",             "tnt_top");
        SIDE_MAP.put("tnt",            "tnt_side");

        // Sea / ocean
        TOP_MAP.put("sea_lantern",        "sea_lantern");
        TOP_MAP.put("prismarine",         "prismarine");
        TOP_MAP.put("prismarine_bricks",  "prismarine_bricks");
        TOP_MAP.put("dark_prismarine",    "dark_prismarine");

        // Coloured blocks (wool, concrete, terracotta, stained glass)
        for (String c : MC_COLORS) {
            TOP_MAP.put(c  + "_wool",              c + "_wool");
            SIDE_MAP.put(c + "_wool",              c + "_wool");
            TOP_MAP.put(c  + "_concrete",          c + "_concrete");
            SIDE_MAP.put(c + "_concrete",          c + "_concrete");
            TOP_MAP.put(c  + "_concrete_powder",   c + "_concrete_powder");
            SIDE_MAP.put(c + "_concrete_powder",   c + "_concrete_powder");
            TOP_MAP.put(c  + "_terracotta",        c + "_terracotta");
            SIDE_MAP.put(c + "_terracotta",        c + "_terracotta");
            TOP_MAP.put(c  + "_glazed_terracotta", c + "_glazed_terracotta");
            SIDE_MAP.put(c + "_glazed_terracotta", c + "_glazed_terracotta");
        }

        // Copper variants
        for (String v : new String[]{
                "copper_block","exposed_copper","weathered_copper","oxidized_copper",
                "cut_copper","exposed_cut_copper","weathered_cut_copper","oxidized_cut_copper"}) {
            TOP_MAP.put(v,  v);
            SIDE_MAP.put(v, v);
        }
    }

    // =========================================================================
    // File I/O
    // =========================================================================

    private Path saveImage(BufferedImage image, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        Path tmp = outputPath.resolveSibling(outputPath.getFileName() + ".tmp");
        try {
            ImageIO.write(image, "png", tmp.toFile());
            try {
                Files.move(tmp, outputPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[Archiv] Preview saved: " + outputPath.getFileName());
            return outputPath;
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    // =========================================================================
    // Data class
    // =========================================================================

    private static class RenderBlock {
        final int xi, yi, zi;
        final String bs;
        final boolean topVis, leftVis, rightVis;

        RenderBlock(int xi, int yi, int zi, String bs,
                    boolean topVis, boolean leftVis, boolean rightVis) {
            this.xi = xi; this.yi = yi; this.zi = zi;
            this.bs = bs;
            this.topVis = topVis; this.leftVis = leftVis; this.rightVis = rightVis;
        }
    }
}