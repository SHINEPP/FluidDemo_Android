package com.demo.fluidparticle.fluidmodel;

public class FluidParticle {
    static final float RADIUS = 0.03f;
    static final float P_K = 0.001f;

    public float x;
    public float y;

    float oldX;
    float oldY;
    float vX;
    float vY;
    float p;
    float vlx;
    float vly;

    FluidParticle(float x, float y) {
        this.x = x;
        this.y = y;
        this.oldX = x;
        this.oldY = y;
    }
}
