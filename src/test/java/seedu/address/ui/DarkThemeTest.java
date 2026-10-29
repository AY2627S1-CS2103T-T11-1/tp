package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class DarkThemeTest {

    private static final Path THEME = Path.of("src", "main", "resources", "view", "DarkTheme.css");

    @Test
    public void theme_noBoldFontWeight() throws IOException {
        // On macOS, JavaFX 17 ignores bold in the system font and JavaFX 25 clips the right edges of its glyphs
        String css = Files.readString(THEME).replaceAll("(?s)/\\*.*?\\*/", "");
        assertFalse(css.matches("(?s).*-fx-font-weight\\s*:\\s*(bold|bolder|[6-9]00).*"),
                "DarkTheme.css should emphasise text by size and color, not bold");
    }
}
