package com.itmecasey.barelypipes.dev;

import java.lang.reflect.Method;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
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
                case "inv" -> mc.setScreenAndShow(new InventoryScreen(mc.player));
                case "pipebuild" -> ShotTour.buildPipe();
                case "pipescreen" -> ShotTour.openPipeScreen();
                case "give" -> ShotTour.server("give @p " + arg);
                case "esc" -> mc.setScreenAndShow(null);
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
                x, y);
    }

    private static void button(Minecraft mc, String arg, boolean down) throws ReflectiveOperationException {
        String[] parts = arg.split(",");
        move(mc, parts[0] + "," + parts[1]);
        int button = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        method(mc.mouseHandler.getClass(), "onButton").invoke(mc.mouseHandler, mc.getWindow().handle(),
                new MouseButtonInfo(button, 0), down ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE);
    }

    private static void key(Minecraft mc, String arg) throws ReflectiveOperationException {
        int mods = 0;
        String name = arg.toLowerCase(Locale.ROOT);
        while (name.contains("+")) {
            String mod = name.substring(0, name.indexOf('+'));
            name = name.substring(name.indexOf('+') + 1);
            mods |= mod.equals("ctrl") ? GLFW.GLFW_MOD_CONTROL : mod.equals("shift") ? GLFW.GLFW_MOD_SHIFT : 0;
        }
        int code = keyCode(name);
        Method press = method(mc.keyboardHandler.getClass(), "keyPress");
        long window = mc.getWindow().handle();
        KeyEvent event = new KeyEvent(code, GLFW.glfwGetKeyScancode(code), mods);
        press.invoke(mc.keyboardHandler, window, GLFW.GLFW_PRESS, event);
        press.invoke(mc.keyboardHandler, window, GLFW.GLFW_RELEASE, event);
    }

    private static int keyCode(String name) {
        if (name.length() == 1) {
            char c = Character.toUpperCase(name.charAt(0));
            return c == '/' ? GLFW.GLFW_KEY_SLASH : c;
        }
        return switch (name) {
            case "enter" -> GLFW.GLFW_KEY_ENTER;
            case "tab" -> GLFW.GLFW_KEY_TAB;
            case "escape" -> GLFW.GLFW_KEY_ESCAPE;
            case "backspace" -> GLFW.GLFW_KEY_BACKSPACE;
            default -> Integer.parseInt(name);
        };
    }

    private static void type(Minecraft mc, String text) throws ReflectiveOperationException {
        Method typed = method(mc.keyboardHandler.getClass(), "charTyped");
        for (char c : text.toCharArray()) {
            typed.invoke(mc.keyboardHandler, mc.getWindow().handle(), new CharacterEvent(c));
        }
    }
}
