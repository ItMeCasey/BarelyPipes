package com.itmecasey.barelypipes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Casey's rule: no hints in the game. The language file holds names, values and short factual messages, never tooltips that explain a
 * control, never sentences that tell the player what to click or hold.
 */
class NoHintsTest {
    private static final Path LANG = Path.of("..", "common", "src", "main", "resources", "assets", "barelypipes", "lang", "en_us.json");
    private static final Pattern ENTRY = Pattern.compile("^\\s*\"([^\"]+)\":\\s*\"(.*)\",?\\s*$");
    private static final List<String> TELLING_PHRASES = List.of("click with", "click to", "right-click", "left-click", "hold shift", "hold ", "sneak-use",
            "use the tool", "did you know", "tip:", "hint", "you can ", "you need", "press ", "try ", "fill it", "to install");

    /** The language file is a flat object with one entry per line, so no JSON library is needed. */
    private static Map<String, String> lang() throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        for (String line : Files.readAllLines(LANG)) {
            Matcher matcher = ENTRY.matcher(line);
            if (matcher.matches()) {
                entries.put(matcher.group(1), matcher.group(2));
            }
        }
        return entries;
    }

    @Test
    void theLanguageFileWasRead() throws IOException {
        assertFalse(lang().isEmpty());
        assertTrue(lang().containsKey("block.barelypipes.conduit_bundle"));
    }

    @Test
    void noKeyIsAnExplanatoryTooltipOrHint() throws IOException {
        List<String> bad = new ArrayList<>();
        for (String key : lang().keySet()) {
            if (key.endsWith(".tooltip") || key.endsWith(".hint") || key.contains(".tutorial") || key.contains(".help")) {
                bad.add(key);
            }
        }
        assertTrue(bad.isEmpty(), "hint keys: " + bad);
    }

    @Test
    void noTextTellsThePlayerWhatToDo() throws IOException {
        List<String> bad = new ArrayList<>();
        for (Map.Entry<String, String> entry : lang().entrySet()) {
            String text = entry.getValue().toLowerCase(Locale.ROOT);
            for (String phrase : TELLING_PHRASES) {
                if (text.contains(phrase)) {
                    bad.add(entry.getKey() + " = " + entry.getValue());
                    break;
                }
            }
        }
        assertTrue(bad.isEmpty(), "texts that tell the player what to do: " + bad);
    }
}
