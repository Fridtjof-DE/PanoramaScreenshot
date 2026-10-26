package me.fridtjof.panorama_screenshot;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class PanoramaCraft implements ClientModInitializer {

	private static final String MOD_ID = "panorama_screenshot";
	private static final String PANORAMA_NAMES = "panorama_0.png – panorama_5.png";
	private final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
	private final Path SCREENSHOT_DIR = GAME_DIR.resolve("screenshots");
	private final Path PANORAMA_BASE_DIR = SCREENSHOT_DIR.resolve("panoramas");
	private final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		// register keybind
		KeyMapping panoramaKeyBinding = new KeyMapping(
				"key.panorama_screenshot.take", // translation key
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_F4,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "panorama_screenshot"))
		);
		KeyMappingHelper.registerKeyMapping(panoramaKeyBinding);


		// register panorama screenshot event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (panoramaKeyBinding.consumeClick() && client.player != null) {

				// setup folder
				Path panoramaDir = PANORAMA_BASE_DIR.resolve(timestamp);
				try {
					Files.createDirectories(panoramaDir);
				} catch (IOException e) {
					LOGGER.error("Unable to create panorama screenshot directory. Path: {}", panoramaDir, e);
					client.player.sendSystemMessage(
							Component.translatable("panorama_screenshot.error.create_folder")
									.withStyle(ChatFormatting.RED)
					);
					return;
				}

				// take panorama
				client.grabPanoramixScreenshot(panoramaDir.toFile());

				// attach "/screenshots" because Mojangs takePanorama() does it
				panoramaDir = panoramaDir.resolve("screenshots");

				// build player message
				Path finalPanoramaDir = panoramaDir;
				Component panoramaTakenText = Component.literal(PANORAMA_NAMES)
						.withStyle(ChatFormatting.UNDERLINE)
						.withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(finalPanoramaDir.toAbsolutePath().toString())));

				// send messages
				client.player.sendSystemMessage(Component.translatable("screenshot.success", new Object[]{panoramaTakenText}));
			}
		});
	}
}