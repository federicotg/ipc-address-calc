/*
 * Copyright (C) 2025 fede
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fede.calculator.chart;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.fede.calculator.report.ConsoleReports;
import org.jfree.chart.JFreeChart;

/**
 * Renders charts as palette-optimized, maximally compressed PNG files.
 *
 * @author fede
 */
public class IndexedPNGStrategy implements ChartStrategy {

    private static final int MAX_COLORS = 256;

    @Override
    public void saveChart(String file, JFreeChart chart, int width, int height) throws IOException {

        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = rgb.createGraphics();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
        g2.dispose();

        BufferedImage indexed = quantize(rgb);

        Path output = Path.of(ConsoleReports.CHARTS_PREFIX + file + ".png");
        writePngMaxCompression(indexed, output);
    }

    private static BufferedImage quantize(BufferedImage source) {
        final int width = source.getWidth();
        final int height = source.getHeight();
        final int[] pixels = source.getRGB(0, 0, width, height, null, 0, width);

        final Map<Integer, Integer> histogram = HashMap.newHashMap(512);
        for (int pixel : pixels) {
            histogram.merge(pixel & 0x00_ff_ff_ff, 1, Integer::sum);
        }

        final int[] palette = histogram.size() <= MAX_COLORS
                ? histogram.keySet().stream().mapToInt(Integer::intValue).toArray()
                : medianCut(histogram, MAX_COLORS);

        final int paletteSize = palette.length;
        final byte[] reds = new byte[paletteSize];
        final byte[] greens = new byte[paletteSize];
        final byte[] blues = new byte[paletteSize];
        final Map<Integer, Integer> colorToIndex = HashMap.newHashMap(paletteSize * 2);

        for (int i = 0; i < paletteSize; i++) {
            final int color = palette[i];
            reds[i] = (byte) ((color >> 16) & 0xff);
            greens[i] = (byte) ((color >> 8) & 0xff);
            blues[i] = (byte) (color & 0xff);
            colorToIndex.put(color, i);
        }

        final IndexColorModel colorModel = new IndexColorModel(8, paletteSize, reds, greens, blues);
        final BufferedImage indexed = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
        final byte[] data = ((DataBufferByte) indexed.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < pixels.length; i++) {
            final int color = pixels[i] & 0x00_ff_ff_ff;
            Integer index = colorToIndex.get(color);
            if (index == null) {
                index = nearestIndex(color, palette);
                colorToIndex.put(color, index);
            }
            data[i] = index.byteValue();
        }

        return indexed;
    }

    private static int[] medianCut(Map<Integer, Integer> histogram, int maxColors) {
        final PriorityQueue<ColorBox> boxes = new PriorityQueue<>(
                Comparator.comparingInt(ColorBox::longestRange).reversed());

        final ColorBox initial = new ColorBox(histogram.entrySet().stream()
                .map(e -> new ColorCount(e.getKey(), e.getValue()))
                .toList());
        boxes.add(initial);

        while (boxes.size() < maxColors) {
            final List<ColorBox> unsplittable = new ArrayList<>();
            ColorBox box = null;
            while (!boxes.isEmpty()) {
                final ColorBox candidate = boxes.poll();
                if (candidate.colors.size() >= 2) {
                    box = candidate;
                    break;
                }
                unsplittable.add(candidate);
            }
            boxes.addAll(unsplittable);
            if (box == null) {
                break;
            }
            final ColorBox[] split = box.split();
            boxes.add(split[0]);
            boxes.add(split[1]);
        }

        return boxes.stream()
                .mapToInt(ColorBox::averageColor)
                .toArray();
    }

    private static int nearestIndex(int color, int[] palette) {
        final int r = (color >> 16) & 0xff;
        final int g = (color >> 8) & 0xff;
        final int b = color & 0xff;

        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < palette.length; i++) {
            final int pr = (palette[i] >> 16) & 0xff;
            final int pg = (palette[i] >> 8) & 0xff;
            final int pb = palette[i] & 0xff;
            final int dr = r - pr;
            final int dg = g - pg;
            final int db = b - pb;
            final int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static void writePngMaxCompression(BufferedImage image, Path output) throws IOException {
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IOException("No PNG ImageWriter available");
        }

        final ImageWriter writer = writers.next();
        try {
            final ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                final String[] types = param.getCompressionTypes();
                if (types != null && types.length > 0) {
                    param.setCompressionType(types[0]);
                }
                // PNG writer: 0.0 = maximum compression, 1.0 = maximum speed
                param.setCompressionQuality(0.0f);
            }

            try (var out = new BufferedOutputStream(Files.newOutputStream(output));
                    ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            }
        } finally {
            writer.dispose();
        }
    }

    private record ColorCount(int rgb, int count) {
    }

    private static final class ColorBox {

        private final List<ColorCount> colors;
        private final int rMin;
        private final int rMax;
        private final int gMin;
        private final int gMax;
        private final int bMin;
        private final int bMax;

        private ColorBox(List<ColorCount> colors) {
            this.colors = List.copyOf(colors);

            int localRMin = 255;
            int localRMax = 0;
            int localGMin = 255;
            int localGMax = 0;
            int localBMin = 255;
            int localBMax = 0;

            for (ColorCount color : this.colors) {
                final int r = (color.rgb() >> 16) & 0xff;
                final int g = (color.rgb() >> 8) & 0xff;
                final int b = color.rgb() & 0xff;
                localRMin = Math.min(localRMin, r);
                localRMax = Math.max(localRMax, r);
                localGMin = Math.min(localGMin, g);
                localGMax = Math.max(localGMax, g);
                localBMin = Math.min(localBMin, b);
                localBMax = Math.max(localBMax, b);
            }

            this.rMin = localRMin;
            this.rMax = localRMax;
            this.gMin = localGMin;
            this.gMax = localGMax;
            this.bMin = localBMin;
            this.bMax = localBMax;
        }

        private int longestRange() {
            return Math.max(rMax - rMin, Math.max(gMax - gMin, bMax - bMin));
        }

        private ColorBox[] split() {
            final int rRange = rMax - rMin;
            final int gRange = gMax - gMin;
            final int bRange = bMax - bMin;

            final Comparator<ColorCount> byChannel;
            if (rRange >= gRange && rRange >= bRange) {
                byChannel = Comparator.comparingInt(c -> (c.rgb() >> 16) & 0xff);
            } else if (gRange >= bRange) {
                byChannel = Comparator.comparingInt(c -> (c.rgb() >> 8) & 0xff);
            } else {
                byChannel = Comparator.comparingInt(c -> c.rgb() & 0xff);
            }

            final List<ColorCount> sorted = new ArrayList<>(colors);
            sorted.sort(byChannel);

            final long total = sorted.stream().mapToLong(ColorCount::count).sum();
            long running = 0;
            int splitAt = sorted.size() / 2;
            for (int i = 0; i < sorted.size(); i++) {
                running += sorted.get(i).count();
                if (running >= total / 2) {
                    splitAt = Math.max(1, Math.min(i + 1, sorted.size() - 1));
                    break;
                }
            }

            return new ColorBox[]{
                new ColorBox(sorted.subList(0, splitAt)),
                new ColorBox(sorted.subList(splitAt, sorted.size()))
            };
        }

        private int averageColor() {
            long r = 0;
            long g = 0;
            long b = 0;
            long total = 0;

            for (ColorCount color : colors) {
                final long count = color.count();
                r += (long) ((color.rgb() >> 16) & 0xff) * count;
                g += (long) ((color.rgb() >> 8) & 0xff) * count;
                b += (long) (color.rgb() & 0xff) * count;
                total += count;
            }

            if (total == 0) {
                return 0;
            }

            return ((int) (r / total) << 16)
                    | ((int) (g / total) << 8)
                    | (int) (b / total);
        }
    }

}
