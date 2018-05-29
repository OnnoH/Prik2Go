package data;

import domein.Prik2GoException;
import io.github.glytching.junit.extension.exception.ExpectedException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testen van VulDatabase")
public class TestVulDatabase {

    @Test
    @Disabled
    @ExpectedException(type = Exception.class)
    @DisplayName("Vullen database mislukt.")
    void makeConnectionTest() throws Prik2GoException {
        VulDatabase.vul("bestaat-niet.csv");
        // TODO VulDatabase class refactoren (in ieder geval hernoemen ;-).
    }

}