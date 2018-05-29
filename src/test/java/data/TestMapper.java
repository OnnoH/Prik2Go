package data;

import domein.Prik2GoException;
import io.github.glytching.junit.extension.exception.ExpectedException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testen van Mapper")
public class TestMapper {

    @Test
    @Disabled
    @ExpectedException(type = Prik2GoException.class, messageIs = "Fout bij het maken van verbinding met het database.")
    @DisplayName("Verbinding maken mislukt")
    void makeConnectionTest() throws Prik2GoException {
        Mapper mapper = new Mapper();
        mapper.setDatabaseUrl("wijst-nergens-naar");
        // TODO Mapper class refactoren.
    }

}
