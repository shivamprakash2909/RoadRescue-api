package com.shivam.roadrescue.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoUtilsTest {

    @Test
    void testHaversineDistanceZeroForSamePoint() {
        double dist = GeoUtils.haversineDistanceKm(28.6139, 77.2090, 28.6139, 77.2090);
        assertEquals(0.0, dist, 0.0001);
    }

    @Test
    void testHaversineDistanceNewDelhiToMumbai() {
        // New Delhi: 28.6139 N, 77.2090 E
        // Mumbai: 19.0760 N, 72.8777 E
        // Approximate distance: ~1150 km
        double dist = GeoUtils.haversineDistanceKm(28.6139, 77.2090, 19.0760, 72.8777);
        assertTrue(dist > 1140 && dist < 1170, "Distance should be approx 1150 km, was " + dist);
    }
}
