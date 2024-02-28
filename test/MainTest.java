import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static edu.cvtc.bigram.Main.createBigrams;
import static org.junit.jupiter.api.Assertions.*;

import edu.cvtc.bigram.*;

@SuppressWarnings({"SpellCheckingInspection"})
class MainTest {
    @Test
    void createConnection() {
        assertDoesNotThrow(
                () -> {
                    Connection db = Main.createConnection();
                    assertNotNull(db);
                    assertFalse(db.isClosed());
                    db.close();
                    assertTrue(db.isClosed());
                }, "Failed to create and close connection."
        );
    }

    @Test
    void reset() {
        Main.reset();
        assertFalse(Files.exists(Path.of(Main.DATABASE_PATH)));
    }

    @Test
    void mainArgs() {
        assertAll(
                () -> {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    System.setOut(new PrintStream(out));
                    Main.main(new String[]{"--version"});
                    String output = out.toString();
                    assertTrue(output.startsWith("Version "));
                },
                () -> {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    System.setOut(new PrintStream(out));
                    Main.main(new String[]{"--help"});
                    String output = out.toString();
                    assertTrue(output.startsWith("Add bigrams"));
                },
                () -> assertDoesNotThrow(() -> {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    System.setErr(new PrintStream(out));
                    Main.main(new String[]{"--reset"});
                    String output = out.toString();
                    assertTrue(output.startsWith("Expected"));
                }),
                () -> assertDoesNotThrow(() -> Main.main(new String[]{"./sample-texts/non-existant-file.txt"})),
                () -> assertDoesNotThrow(() -> Main.main(new String[]{"./sample-texts/empty.txt"}))
        );
    }

    // TODO: Create your test(s) below. /////////////////////////////////////////

    @Test
    public void CreateBigrams() throws SQLException {
        Connection db = Main.createConnection();
        String src = "Hello, world!";
        createBigrams(db, src);

        Statement command = db.createStatement();
        ResultSet rows = command.executeQuery("SELECT * FROM bigrams WHERE words_id = (SELECT id FROM words WHERE string = 'Hello') AND next_words_id = (SELECT id FROM words WHERE string = 'world')");

        assertFalse(rows.next()); // This will fail because "Hello," and "world!" are inserted into the database instead of "Hello" and "world"
    }
}
