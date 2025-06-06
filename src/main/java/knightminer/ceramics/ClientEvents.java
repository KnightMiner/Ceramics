package knightminer.ceramics;

import knightminer.ceramics.blocks.GaugeBlock;
import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.CrackedModel;
import knightminer.ceramics.client.renderer.ChannelBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternFluids;
import knightminer.ceramics.client.renderer.FaucetBlockEntityRenderer;
import knightminer.ceramics.client.screen.KilnScreen;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.mantle.client.render.ChannelFluids;
import slimeknights.mantle.client.render.FaucetFluid;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.MOD, value=Dist.CLIENT)
public class ClientEvents {
  @SubscribeEvent
  static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
    FaucetFluid.initialize(event);
    ChannelFluids.initialize(event);
    event.registerReloadListener(CisternFluids.REGISTRY);
  }

  @SubscribeEvent
  static void setupClient(FMLClientSetupEvent event) {
    RenderType cutout = RenderType.cutout();
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_GAUGE.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(Registration.PORCELAIN_GAUGE.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_CISTERN.get(), cutout);
    Registration.COLORED_CISTERN.forEach(cistern -> ItemBlockRenderTypes.setRenderLayer(cistern, cutout));
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_FAUCET.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_CHANNEL.get(), cutout);

    MenuScreens.register(Registration.KILN_MENU.get(), KilnScreen::new);

    MinecraftForge.EVENT_BUS.addListener(ClientEvents::renderGaugeTooltip);
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(Registration.CISTERN_BLOCK_ENTITY.get(), CisternBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(Registration.FAUCET_BLOCK_ENTITY.get(), FaucetBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(Registration.CHANNEL_BLOCK_ENTITY.get(), ChannelBlockEntityRenderer::new);
  }

  @SubscribeEvent
  static void registerModels(RegisterGeometryLoaders event) {
    event.register("fluid_bucket", ClayBucketModel.FLUID_LOADER);
    event.register("solid_bucket", ClayBucketModel.SOLID_LOADER);
    event.register("cracked", CrackedModel.LOADER);
  }

  /** Renders the tooltip when targeting the gauge block */
  private static void renderGaugeTooltip(RenderGuiOverlayEvent.Post event) {
    if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
      return;
    }
    // must not be in a screen, though chat is fine
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null && minecraft.screen.getClass() != ChatScreen.class) {
      return;
    }
    // must have a hit result
    if (minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
      return;
    }
    BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;
    BlockPos pos = blockHit.getBlockPos();

    // must be targeting a gauge
    BlockState targeted = minecraft.level.getBlockState(blockHit.getBlockPos());
    if (!targeted.is(CeramicsTags.Blocks.GAUGES)) {
      return;
    }
    Direction side = targeted.getValue(BlockStateProperties.HORIZONTAL_FACING);
    // must have a block entity behind the gauge
    BlockEntity gaugeContainer = minecraft.level.getBlockEntity(pos.relative(side.getOpposite()));
    if (gaugeContainer == null) {
      return;
    }
    // block entity must have a fluid handler
    IFluidHandler handler = gaugeContainer.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(EmptyFluidHandler.INSTANCE);
    if (handler.getTanks() <= 0) {
      return;
    }
    // if the fluid is empty, just render the capacity
    FluidStack fluid = handler.getFluidInTank(0);
    List<Component> tooltip;
    if (fluid.isEmpty()) {
      tooltip = List.of(Component.translatable(GaugeBlock.EMPTY_KEY, GaugeBlock.COMMA_FORMAT.format(handler.getTankCapacity(0))));
    } else {
      // render full fluid tooltip
      tooltip = FluidTooltipHandler.getFluidTooltip(fluid);
    }

    int x = minecraft.getWindow().getGuiScaledWidth() / 2;
    int y = minecraft.getWindow().getGuiScaledHeight() / 2;
    event.getGuiGraphics().renderTooltip(minecraft.font, tooltip, Optional.empty(), x, y);
  }
}
