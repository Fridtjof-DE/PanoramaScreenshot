package me.fridtjof.panorama_screenshot;

import net.fabricmc.api.ModInitializer;

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

import java.io.File;

public class PanoramaCraft implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("panorama_screenshot");

	private final File GAME_DIR = new File(FabricLoader.getInstance().getGameDir().toString());
	private final File SCREENSHOT_DIR = new File(FabricLoader.getInstance().getGameDir().toString() + "/screenshots/");
	private final String PANORAMA_NAMES = "panorama_0.png – panorama_5.png";

	@Override
	public void onInitialize() {

		//now translation needed
		KeyBinding panoramaKeyBinding = new KeyBinding("Take Panorama Screenshot", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, KeyBinding.Category.create(Identifier.of("panorama_screenshot")));
		KeyBindingHelper.registerKeyBinding(panoramaKeyBinding);


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (panoramaKeyBinding.wasPressed()) {

				//client.takePanorama(GAME_DIR, 1024, 1024);
				//resolution now hardcoded by Mojang
				client.takePanorama(GAME_DIR);

				Text panoramaTakenText = Text.literal(PANORAMA_NAMES).formatted(Formatting.UNDERLINE).styled((style) -> {
					//return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, SCREENSHOT_DIR.getAbsolutePath()));
					return style.withClickEvent(new ClickEvent.OpenFile(SCREENSHOT_DIR.getAbsolutePath()));
				});
				client.player.sendMessage(Text.translatable("screenshot.success", new Object[]{panoramaTakenText}), false);
			}
		});
	}
}