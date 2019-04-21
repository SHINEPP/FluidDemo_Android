package com.demo.fluidparticle.fluidmodel;

import java.util.ArrayList;
import java.util.List;

class FluidGrid {

    private static final float GRID_SIZE = 0.04f;

    private List<List<FluidParticle>> groups = new ArrayList<>();
    private final int row;
    private final int col;

    FluidGrid(FluidBounds bounds) {
        row = (int) Math.ceil(bounds.height / GRID_SIZE) + 1;
        col = (int) Math.ceil(bounds.width / GRID_SIZE) + 1;

        for (int i = 0; i < row * col; i++) {
            groups.add(new ArrayList<FluidParticle>());
        }
    }

    void addParticle(FluidParticle particle) {
        int x = (int) (particle.x / GRID_SIZE);
        int y = (int) (particle.y / GRID_SIZE);
        groups.get(y * col + x).add(particle);
    }

    List<List<FluidParticle>> getParticleNearGroups(FluidParticle particle) {
        List<List<FluidParticle>> nearGroups = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = (int) (particle.x / GRID_SIZE) + i;
                int y = (int) (particle.y / GRID_SIZE) + j;
                if (x < 0 || x >= col || y < 0 || y >= row) {
                    continue;
                }

                nearGroups.add(groups.get(y * col + x));
            }
        }

        return nearGroups;
    }

    void clearParticles() {
        for (List<FluidParticle> particles : groups) {
            particles.clear();
        }
    }
}
