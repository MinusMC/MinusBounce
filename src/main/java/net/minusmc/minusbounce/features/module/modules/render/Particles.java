/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.minusmc.minusbounce.features.module.modules.render;

import net.minusmc.minusbounce.event.AttackEvent;
import net.minusmc.minusbounce.event.EventTarget;
import net.minusmc.minusbounce.event.MotionEvent;
import net.minusmc.minusbounce.event.Render3DEvent;
import net.minusmc.minusbounce.features.module.Module;
import net.minusmc.minusbounce.features.module.ModuleCategory;
import net.minusmc.minusbounce.features.module.ModuleInfo;
import net.minusmc.minusbounce.utils.particles.EvictingList;
import net.minusmc.minusbounce.utils.particles.Particle;
import net.minusmc.minusbounce.utils.particles.Vec3;
import net.minusmc.minusbounce.utils.render.RenderUtils;
import net.minusmc.minusbounce.utils.timer.ParticleTimer;
import net.minecraft.entity.EntityLivingBase;
import net.minusmc.minusbounce.value.BoolValue;
import net.minusmc.minusbounce.value.IntegerValue;

import java.util.List;

@ModuleInfo(name = "Particles", description = "Particles", category = ModuleCategory.RENDER)
public final class Particles extends Module { // recode nốt

    private final IntegerValue amount = new IntegerValue("Amount", 10, 1, 20);

    private final BoolValue physics = new BoolValue("Physics", true);

    private final List<Particle> particles = new EvictingList<>(100);
    private final ParticleTimer timer = new ParticleTimer();
    private EntityLivingBase target;

    @EventTarget
    public void onAttack(final AttackEvent event) {
        if (event.getTargetEntity() instanceof EntityLivingBase)
            target = (EntityLivingBase) event.getTargetEntity();
    }

    @EventTarget
    public void onMotion(final MotionEvent event) {
        if (target != null && target.hurtTime >= 9 && mc.thePlayer.getDistance(target.posX, target.posY, target.posZ) < 10) {
            for (int i = 0; i < amount.get(); i++)
                particles.add(new Particle(new Vec3(target.posX + (Math.random() - 0.5) * 0.5, target.posY + Math.random() * 1 + 0.5, target.posZ + (Math.random() - 0.5) * 0.5)));

            target = null;
        }
    }

    @EventTarget
    public void onRender3D(final Render3DEvent event) {
        if (particles.isEmpty())
            return;

        for (int i = 0; i <= timer.getElapsedTime() / 1E+11; i++) {
            if (physics.get())
                particles.forEach(Particle::update);
            else
                particles.forEach(Particle::updateWithoutPhysics);
        }

        particles.removeIf(particle -> mc.thePlayer.getDistanceSq(particle.position.xCoord, particle.position.yCoord, particle.position.zCoord) > 50 * 10);

        timer.reset();

        RenderUtils.INSTANCE.renderParticles(particles);
    }
}