/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.mapviewer.core;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.base.FieldFacet2D;

import com.google.common.base.Stopwatch;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class FieldRasterizer implements Rasterizer {

    private static final Logger logger = LoggerFactory.getLogger(FieldRasterizer.class);

    @Override
    public BufferedImage raster(Region region, Class<? extends FieldFacet2D> facetClass) {

        FieldFacet2D facet = region.getFacet(facetClass);

        Stopwatch sw = Stopwatch.createStarted();

        Vector3i extent = region.getRegion().size();
        int width = extent.x;
        int height = extent.z;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int z = 0; z < width; z++) {
            for (int x = 0; x < height; x++) {
                float val = facet.get(x, z);
                int c = mapFloat(val);
                img.setRGB(x, z, c);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Rendered regions in {}ms.", sw.elapsed(TimeUnit.MILLISECONDS));
        }

        return img;
    }

    private int mapFloat(float val) {
        int g = TeraMath.clamp((int) (val * 4), 0, 255);
        return g | (g << 8) | (g << 16);
    }

}
