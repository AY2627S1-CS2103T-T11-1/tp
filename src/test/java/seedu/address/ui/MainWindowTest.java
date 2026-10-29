package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

/** Tests restoration of the main window's saved position. */
public class MainWindowTest {

    private static final Rectangle2D PRIMARY_SCREEN = new Rectangle2D(100, 40, 1280, 680);
    private static final Rectangle2D LEFT_SCREEN = new Rectangle2D(-1500, 20, 1600, 900);
    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 600;
    private static final Point2D CENTRE_OF_PRIMARY = new Point2D(240, 80);

    @Test
    public void getRestoredWindowPosition_noSavedPosition_leavesPositionUnset() {
        assertTrue(MainWindow.getRestoredWindowPosition(new GuiSettings(), WINDOW_WIDTH, WINDOW_HEIGHT,
                List.of(PRIMARY_SCREEN), PRIMARY_SCREEN).isEmpty());
    }

    @Test
    public void getRestoredWindowPosition_validConnectedScreenPositions_preservesPosition() {
        assertEquals(new Point2D(200, 80), restoredPosition(200, 80, List.of(PRIMARY_SCREEN, LEFT_SCREEN)));
        assertEquals(new Point2D(-1400, 100), restoredPosition(-1400, 100,
                List.of(PRIMARY_SCREEN, LEFT_SCREEN)));
    }

    @Test
    public void getRestoredWindowPosition_beyondRightOrBottom_centresOnPrimary() {
        assertEquals(CENTRE_OF_PRIMARY, restoredPosition(381, 80, List.of(PRIMARY_SCREEN)));
        assertEquals(CENTRE_OF_PRIMARY, restoredPosition(200, 121, List.of(PRIMARY_SCREEN)));
    }

    @Test
    public void getRestoredWindowPosition_whollyOutsideLeftOrTop_centresOnPrimary() {
        assertEquals(CENTRE_OF_PRIMARY, restoredPosition(-1001, 80, List.of(PRIMARY_SCREEN)));
        assertEquals(CENTRE_OF_PRIMARY, restoredPosition(200, -601, List.of(PRIMARY_SCREEN)));
    }

    @Test
    public void getRestoredWindowPosition_disconnectedSecondary_centresOnPrimary() {
        assertEquals(CENTRE_OF_PRIMARY, restoredPosition(1500, 100, List.of(PRIMARY_SCREEN)));
    }

    private Point2D restoredPosition(int savedX, int savedY, List<Rectangle2D> screens) {
        GuiSettings settings = new GuiSettings(WINDOW_WIDTH, WINDOW_HEIGHT, savedX, savedY);
        return MainWindow.getRestoredWindowPosition(settings, WINDOW_WIDTH, WINDOW_HEIGHT, screens, PRIMARY_SCREEN)
                .orElseThrow();
    }
}
