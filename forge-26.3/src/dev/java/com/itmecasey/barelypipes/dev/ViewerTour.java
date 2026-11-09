package com.itmecasey.barelypipes.dev;

import java.lang.reflect.Method;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Development-only checks of the optional integrations (item and recipe viewers, Jade) on a real client. It is part of the
 * screenshot tour, selected with {@code BARELYPIPES_SHOTS=viewers}. It runs a script of
 * steps taken from {@code BARELYPIPES_VIEWER_SCRIPT}, for example {@code inv;wait:40;shot:jei_inventory;key:ctrl+f;type:bars}.
 *
 * <p>Input goes straight into the game's own mouse and keyboard handlers, so nothing touches the real desktop. Steps:
 * {@code inv} opens the inventory, {@code give:barelypipes:gold_item_pipe} gives an item, {@code esc} closes the screen, {@code wait:N} ticks, {@code shot:name}, {@code move:x,y},
 * {@code click:x,y[,button]}, {@code key:[ctrl+]name}, {@code type:text}. Coordinates are window pixels.
 */
final class ViewerTour {
    private static final Logger LOGGER = LoggerFactory.getLogger("BarelyPipes");

    private ViewerTour() {
    }

    static void plan() {
        String script = System.getenv().getOrDefault("BARELYPIPES_VIEWER_SCRIPT", "inv;wait:40;shot:viewer_inventory");
        for (String step : script.split(";")) {
            String text = step.trim();
            if (!text.isEmpty()) {
                if (text.startsWith("click:")) {
                    ShotTour.then(2, () -> run("down:" + text.substring(6)));
                    ShotTour.then(4, () -> run("up:" + text.substring(6)));
                    continue;
                }
                ShotTour.then(2, () -> run(text));
                if (text.startsWith("shot:")) {
                    String name = text.substring(5);
                    ShotTour.shoot(name, 1);
                }
                if (text.startsWith("wait:")) {
                    ShotTour.then(Integer.parseInt(text.substring(5)), () -> {
                    });
                }
            }
        }
    }

    private static void run(String step) {
        Minecraft mc = Minecraft.getInstance();
        String[] parts = step.split(":", 2);
        String arg = parts.length > 1 ? parts[1] : "";
        try {
            switch (parts[0]) {
                case "inv" -> mc.gui.setScreen(new InventoryScreen(mc.player));
                case "give" -> ShotTour.server("give @p " + arg);
                case "esc" -> mc.gui.setScreen(null);
                case "move" -> move(mc, arg);
                case "down" -> button(mc, arg, true);
                case "up" -> button(mc, arg, false);
                case "key" -> key(mc, arg);
                case "type" -> type(mc, arg);
                default -> {
                }
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("BARELYPIPES-SHOTS viewer step '{}' failed", step, e);
        }
        LOGGER.info("BARELYPIPES-SHOTS viewer step {}", step);
    }

    private static Method method(Class<?> type, String name) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                m.setAccessible(true);
                return m;
            }
        }
        throw new IllegalStateException("no method " + name + " in " + type);
    }

    private static void move(Minecraft mc, String arg) throws ReflectiveOperationException {
        String[] xy = arg.split(",");
        // Values up to 1.5 are fractions of the window, so a check does not depend on how big the window turned out.
        double x = Double.parseDouble(xy[0]);
        double y = Double.parseDouble(xy[1]);
        if (x <= 1.5) {
            x *= mc.getWindow().getScreenWidth();
        }
        if (y <= 1.5) {
            y *= mc.getWindow().getScreenHeight();
        }
        method(mc.mouseHandler.getClass(), "onMove").invoke(mc.mouseHandler, mc.getWindow().handle(),
                x, y, 0.0, 0.0);
    }

    private static void button(Minecraft mc, String arg, boolean down) throws ReflectiveOperationException {
        String[] parts = arg.split(",");
        move(mc, parts[0] + "," + parts[1]);
        // 0 is left, 1 right in the script; this version numbers them 1 and 3.
        int button = parts.length > 2 && parts[2].equals("1") ? InputConstants.MOUSE_BUTTON_RIGHT : InputConstants.MOUSE_BUTTON_LEFT;
        method(mc.mouseHandler.getClass(), "onButton").invoke(mc.mouseHandler, mc.getWindow().handle(),
                new MouseButtonInfo(button, 0), down ? InputConstants.PRESS : InputConstants.RELEASE);
    }

    private static void key(Minecraft mc, String arg) throws ReflectiveOperationException {
        int mods = 0;
        String name = arg.toLowerCase(Locale.ROOT);
        while (name.contains("+")) {
            String mod = name.substring(0, name.indexOf('+'));
            name = name.substring(name.indexOf('+') + 1);
            mods |= mod.equals("ctrl") ? InputConstants.MOD_CONTROL : mod.equals("shift") ? InputConstants.MOD_SHIFT : 0;
        }
        int code = keyCode(name);
        Method press = method(mc.keyboardHandler.getClass(), "keyPress");
        long window = mc.getWindow().handle();
        KeyEvent event = new KeyEvent(code, shortcut(name), mods);
        press.invoke(mc.keyboardHandler, window, InputConstants.PRESS, event);
        press.invoke(mc.keyboardHandler, window, InputConstants.RELEASE, event);
    }

    private static int shortcut(String name) {
        return name.length() == 1 ? Character.toUpperCase(name.charAt(0)) : keyCode(name);
    }

    /** The game's own key numbers (InputConstants), so this does not depend on how the window library numbers keys. */
    private static int keyCode(String name) {
        String field = name.length() == 1 ? (name.equals("/") ? "KEY_SLASH" : "KEY_" + name.toUpperCase(Locale.ROOT))
                : "KEY_" + name.toUpperCase(Locale.ROOT);
        try {
            return InputConstants.class.getField(field).getInt(null);
        } catch (ReflectiveOperationException e) {
            return Integer.parseInt(name);
        }
    }

    private static void type(Minecraft mc, String text) throws ReflectiveOperationException {
        Method typed = method(mc.keyboardHandler.getClass(), "charTyped");
        for (char c : text.toCharArray()) {
            typed.invoke(mc.keyboardHandler, mc.getWindow().handle(), new CharacterEvent(c));
        }
    }
}
