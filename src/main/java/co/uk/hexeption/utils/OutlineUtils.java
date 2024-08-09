package co.uk.hexeption.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import static org.lwjgl.opengl.GL11.*;

import java.awt.*;

/**
 * Outline ESP
 *
 * @author Hexeption
 */
public class OutlineUtils {

    public static void renderOne(final float lineWidth) {
        checkSetupFBO();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glDisable(GL_ALPHA_TEST);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glLineWidth(lineWidth);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_STENCIL_TEST);
        glClear(GL_STENCIL_BUFFER_BIT);
        glClearStencil(0xF);
        glStencilFunc(GL_NEVER, 1, 0xF);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    public static void renderTwo() {
        glStencilFunc(GL_NEVER, 0, 0xF);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public static void renderThree() {
        glStencilFunc(GL_EQUAL, 1, 0xF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    public static void renderFour(final Color color) {
        setColor(color);
        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_POLYGON_OFFSET_LINE);
        glPolygonOffset(1.0F, -2000000F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderFive() {
        glPolygonOffset(1.0F, 2000000F);
        glDisable(GL_POLYGON_OFFSET_LINE);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        glEnable(GL_BLEND);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);
        glPopAttrib();
    }

    public static void setColor(final Color color) {
        glColor4d(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public static void checkSetupFBO() {
        final Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();

        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    /**
     * Sets up the FBO with depth and stencil
     *
     * @param fbo Framebuffer
     */
    private static void setupFBO(final Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        final int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
    }
}
