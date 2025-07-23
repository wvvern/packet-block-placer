package wvv.packetblockplacer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import org.lwjgl.glfw.GLFW;

public class PacketBlockPlacerClient implements ClientModInitializer {
    private static KeyBinding placeKeyBinding;

    @Override
    public void onInitializeClient() {
        // Register configurable keybinding
        placeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.packet-block-placer.place", // Translation key
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B, // Default key
            "category.packet-block-placer" // Category translation key
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if keybinding was pressed this tick
            while (placeKeyBinding.wasPressed()) {
                handleBlockPlacement(client);
            }
        });
    }

    private void handleBlockPlacement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        // Check if player has a block item in hand
        if (!(player.getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }

        // Use the game's built-in crosshair target (what the player is looking at)
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
            
            // Create packet with the hit result
            PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND,
                hitResult,
                0 // Sequence number - 0 is fine for this use case
            );
            
            // Send the packet
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendPacket(packet);
            }
        }
    }
}
