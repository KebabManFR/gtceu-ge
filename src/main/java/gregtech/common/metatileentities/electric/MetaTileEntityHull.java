package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IPassthroughHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MetaTileEntityHull extends MetaTileEntityMultiblockPart
                                implements IPassthroughHatch, IMultiblockAbilityPart<IPassthroughHatch> {

    protected IEnergyContainer energyContainer;
    private AENetworkProxy gridProxy;

    public MetaTileEntityHull(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        reinitializeEnergyContainer();
    }

    @Override
    public gregtech.api.metatileentity.MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHull(metaTileEntityId, getTier());
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 16L, tierVoltage, 1L, tierVoltage, 1L);
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation,
                PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VNF[getTier()];
        tooltip.add(I18n.format("gregtech.machine.hull.tooltip"));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.voltage_in_out", energyContainer.getInputVoltage(), tierName));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_out", 1));
    }

    @Override
    public void update() {
        super.update();
        if (isFirstTick() && Loader.isModLoaded(GTValues.MODID_APPENG)) {
            if (getProxy() != null) getProxy().onReady();
        }
    }

    @Nonnull
    @Override
    @Optional.Method(modid = GTValues.MODID_APPENG)
    public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        return AECableType.SMART;
    }

    @Nullable
    @Override
    @Optional.Method(modid = GTValues.MODID_APPENG)
    public AENetworkProxy getProxy() {
        if (gridProxy == null && getHolder() instanceof MetaTileEntityHolder) {
            gridProxy = new AENetworkProxy((MetaTileEntityHolder) getHolder(), "proxy", getStackForm(), true);
        }
        return gridProxy;
    }

    @Override
    public MultiblockAbility<IPassthroughHatch> getAbility() {
        return MultiblockAbility.PASSTHROUGH_HATCH;
    }

    @Override
    public void registerAbilities(@Nonnull List<IPassthroughHatch> abilityList) {
        abilityList.add(this);
    }

    @Nonnull
    @Override
    public Class<?> getPassthroughType() {
        return IEnergyContainer.class;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
