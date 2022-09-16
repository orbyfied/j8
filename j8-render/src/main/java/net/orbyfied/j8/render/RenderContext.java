package net.orbyfied.j8.render;

public abstract class RenderContext {

    // the owning worker for this context
    RenderWorker owner;

    // the render graphics
    protected RenderGraphics graphics;

    /**
     * Sets up the context to be ready for rendering.
     */
    protected void setup() {
        // create graphics
        this.graphics = createGraphics();
    }

    /* Getters */

    public RenderGraphics getGraphics() {
        return graphics;
    }

    public RenderWorker getWorker() {
        return owner;
    }

    /* ---- Functionality ---- */

    protected abstract void update(RenderWorker worker, float dt);

    protected abstract RenderGraphics createGraphics();

}
