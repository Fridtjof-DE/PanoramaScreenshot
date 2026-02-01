package me.fridtjof.panorama_screenshot;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
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
		KeyBinding panoramaKeyBinding = new KeyBinding(
				"key.panorama_screenshot.take", // translation key
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F4,
				KeyBinding.Category.create(Identifier.of(MOD_ID, "panorama_screenshot"))
				);
		KeyBindingHelper.registerKeyBinding(panoramaKeyBinding);


		// register panorama screenshot event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (panoramaKeyBinding.wasPressed() && client.player != null) {

				// setup folder
				Path panoramaDir = PANORAMA_BASE_DIR.resolve(timestamp);
				try {
					Files.createDirectories(panoramaDir);
				} catch (IOException e) {
					LOGGER.error("Unable to create panorama screenshot directory. Path: {}", panoramaDir, e);
					client.player.sendMessage(
							Text.translatable("panorama_screenshot.error.create_folder")
									.formatted(Formatting.RED),
							false
					);
					return;
				}

				// take panorama
				client.takePanorama(panoramaDir.toFile());

				// attach "/screenshots" because Mojangs takePanorama() does it
				panoramaDir = panoramaDir.resolve("screenshots");

				// build player message
				Path finalPanoramaDir = panoramaDir;
				Text panoramaTakenText = Text.literal(PANORAMA_NAMES)
						.formatted(Formatting.UNDERLINE)
						.styled(style -> style.withClickEvent(new ClickEvent.OpenFile(finalPanoramaDir.toAbsolutePath().toString())));

				// send messages
                client.player.sendMessage(Text.translatable("screenshot.success", new Object[]{panoramaTakenText}), false);
			}
		});
	}
}