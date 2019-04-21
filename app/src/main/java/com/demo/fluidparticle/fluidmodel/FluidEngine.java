package com.demo.fluidparticle.fluidmodel;

import java.util.ArrayList;
import java.util.List;

public class FluidEngine {

    private static final String TAG = "FLUID_ENGINE";

    private List<FluidParticle> particles = new ArrayList<>();
    private FluidBounds bounds;
    private FluidGrid grid;

    private float gX = 0f;
    private float gY = 0.0001f;

    public FluidEngine() {
        bounds = new FluidBounds();
        grid = new FluidGrid(bounds);

        for (int i = 0; i < 40; i++) {
            float x = i / 40f * 0.8f + 0.1f;
            for (int j = 0; j < 40; j++) {
                float y = j / 40f * 0.4f + 0.2f;
                particles.add(new FluidParticle(x, y));
            }
        }
    }

    public void simulate() {
        updateGridParticles();
        applyGravity();
        ddra();
        updateVelocity();
    }

    public List<FluidParticle> getParticles() {
        return particles;
    }

    public void setgX(float gX) {
        this.gX = gX;
    }

    public void setgY(float gY) {
        this.gY = gY;
    }

    private void updateGridParticles() {
        grid.clearParticles();

        for (FluidParticle particle : particles) {
            grid.addParticle(particle);
        }
    }

    private void applyGravity() {
        for (FluidParticle particle : particles) {
            particle.vX += gX;
            particle.vY += gY;
            particle.oldX = particle.x;
            particle.oldY = particle.y;
            particle.x += particle.vX;
            particle.y += particle.vY;
        }
    }

    private void ddra() {
        for (FluidParticle coreParticle : particles) {

            List<FluidParticle> nearGridParticles = new ArrayList<>();
            float pressure = 0f;
            float pressureNear = 0f;

            List<List<FluidParticle>> nearGroups = grid.getParticleNearGroups(coreParticle);
            for (List<FluidParticle> nearGroup : nearGroups) {
                for (FluidParticle nearParticle : nearGroup) {
                    if (nearParticle == coreParticle) {
                        continue;
                    }

                    float dx = nearParticle.x - coreParticle.x;
                    float dy = nearParticle.y - coreParticle.y;
                    float len = (float) Math.sqrt(dx*dx + dy*dy);
                    if (len >= FluidParticle.RADIUS) {
                        continue;
                    }

                    float p = 1f - len / FluidParticle.RADIUS;
                    pressure += p * p;
                    pressureNear += p * p * p;

                    nearParticle.p = p;
                    nearParticle.vlx = dx * p / len;
                    nearParticle.vly = dy * p / len;
                    nearGridParticles.add(nearParticle);
                }
            }

            boundaryLimit(coreParticle);
            relaxation(coreParticle, nearGridParticles, pressure, pressureNear);
            boundaryLimit(coreParticle);
        }
    }

    private void boundaryLimit(FluidParticle particle) {
        if (particle.x < FluidParticle.RADIUS) {
            float q = 1f - Math.abs(particle.x / FluidParticle.RADIUS);
            particle.x += q * q * FluidParticle.P_K;
        } else if (particle.x > bounds.width - FluidParticle.RADIUS) {
            float q = 1f - Math.abs((bounds.width - particle.x) / FluidParticle.RADIUS);
            particle.x -= q * q * FluidParticle.P_K;
        }

        if (particle.y < FluidParticle.RADIUS) {
            float q = 1f - Math.abs(particle.y / FluidParticle.RADIUS);
            particle.y += q * q * FluidParticle.P_K;
        } else if (particle.y > bounds.height - FluidParticle.RADIUS) {
            float q = 1f - Math.abs((bounds.height - particle.y) / FluidParticle.RADIUS);
            particle.y -= q * q * FluidParticle.P_K;
        }

        if (particle.x < 0f) {
            particle.x = 0f;
        } else if (particle.x > bounds.width) {
            particle.x = bounds.width;
        }

        if (particle.y < 0f) {
            particle.y = 0f;
        } else if (particle.y > bounds.height) {
            particle.y = bounds.height;
        }
    }

    private void relaxation(FluidParticle particle, List<FluidParticle> nearParticles, float pressure, float pressureNear) {
        pressure = (pressure - 0.006f) * FluidParticle.P_K;
        pressureNear *= FluidParticle.P_K;
        for (FluidParticle nearParticle : nearParticles) {
            float p = pressure + pressureNear * nearParticle.p;
            float dx = nearParticle.vlx * p / 2f;
            float dy = nearParticle.vly * p / 2f;
            nearParticle.x += dx;
            nearParticle.y += dy;
            particle.x -= dx;
            particle.y -= dy;
        }
    }

    private void updateVelocity() {
        for (FluidParticle particle : particles) {
            particle.vX = particle.x - particle.oldX;
            particle.vY = particle.y - particle.oldY;
        }
    }
}
