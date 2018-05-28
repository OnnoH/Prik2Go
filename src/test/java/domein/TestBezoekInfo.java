package domein;

import static domein.BezoekInfo.schoonPostcodesOp;

import io.github.glytching.junit.extension.exception.ExpectedException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.contentEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testen van Bezoekinfo")
class TestBezoekInfo {

    private String lineSeparator = "\r\n";


    @Test
    @ExpectedException(type = Prik2GoException.class, messageIs = "Specificatie van doelbestand mag niet gelijk zijn aan het bronbestand!")
    @DisplayName("Doel- en bronbestand zijn gelijk")
    void targetAndSourceAreEqualTest() throws Prik2GoException {
        schoonPostcodesOp("same-file.csv", "same-file.csv");
    }

    @Test
    @ExpectedException(type = Prik2GoException.class, messageIs = "Specificatie van zowel het bron- als doelbestand is niet aanwezig.")
    @DisplayName("Doel- en bronbestand ontbreken beiden")
    void targetAndSourceAreBothMissingTest() throws Prik2GoException {
        schoonPostcodesOp(null, null);
    }

    @Test
    @ExpectedException(type = Prik2GoException.class, messageIs = "Specificatie van zowel het bron- als doelbestand is niet aanwezig.")
    @DisplayName("Doel- en bronbestand zijn allebei leeg")
    void targetAndSourceAreBothEmptyTest() throws Prik2GoException {
        schoonPostcodesOp("", "");
    }

    @Test
    @ExpectedException(type = Prik2GoException.class, messageIs = "Specificatie van het bronbestand ontbreekt.")
    @DisplayName("Bronbestand ontbreekt")
    void sourceIsMissingTest() throws Prik2GoException {
        schoonPostcodesOp(null, "only-target.csv");
    }

    @Test
    @ExpectedException(type = Prik2GoException.class, messageIs = "Specificatie van het doelbestand ontbreekt.")
    @DisplayName("Doelbestand ontbreekt")
    void targetIsMissingTest() throws Prik2GoException {
        schoonPostcodesOp("only-source.csv", null);
    }

    @Test
    @ExpectedException(type = Prik2GoException.class, messageStartsWith = "Onvindbaar bestand")
    @DisplayName("Bronbestand niet aanwezig")
    void sourceNotFoundTest() throws Prik2GoException {
        schoonPostcodesOp("only-source.csv", "some-output.csv");
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @ExpectedException(type = Prik2GoException.class, messageStartsWith = "Lezen niet mogelijk van bestand")
    @DisplayName("Bronbestand mag niet worden gelezen")
    void sourceCanNotBeReadTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("unreadable.csv");

        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Data1, Data2, Data3");
        fileWriter.flush();
        fileWriter.close();

        if (sourceFile.setReadable(false)) {
            schoonPostcodesOp(sourceFile.getPath(), "file-out.csv");
        } else {
            throw new Prik2GoException("Onvoldoende rechten om bestandsattributen aan te passen.");
        }
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @ExpectedException(type = Prik2GoException.class, messageStartsWith = "Geen gegevens aanwezig in")
    @DisplayName("Bronbestand bevat geen data")
    void sourceIsEmptyTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("empty-file.csv");

        schoonPostcodesOp(sourceFile.getPath(), "file-out.csv");
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @ExpectedException(type = Prik2GoException.class, messageStartsWith = "Er is een fout opgestreden bij het maken van de writer")
    @DisplayName("Doelbestand mag niet worden beschreven")
    void targetCanNotBeWrittenTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("some-file.csv");

        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Data1, Data2, Data3");
        fileWriter.flush();
        fileWriter.close();

        File targetFolder = temporaryFolder.createDirectory("output");
        if (targetFolder.setWritable(false)) {
            schoonPostcodesOp(sourceFile.getPath(), targetFolder.getPath()+File.separator+"unwritable.csv");
        } else {
            throw new Prik2GoException("Onvoldoende privileges om bestandsattributen aan te passen.");
        }
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @ExpectedException(type = Prik2GoException.class, messageStartsWith = "Reeds bestaand bestand")
    @DisplayName("Doelbestand bestaat al")
    void targetAlreadyExistsTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("some-file.csv");

        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Data1, Data2, Data3");
        fileWriter.flush();
        fileWriter.close();

        File targetFile = temporaryFolder.createFile("already-exists.csv");

        schoonPostcodesOp(sourceFile.getPath(), targetFile.getPath());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @DisplayName("Doelbestand is na verwerking gelijk aan bronbestand")
    void schoonPostcodesOpUnchangedTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("some-file.csv");
        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Bezoeknr,Datum,Vestiging,Klant,Postcode,Land,Infectie,Vaccinatiedatum"+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567HE,Mexico,DTP,2016-05-17"+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567HE,Mexico,Hepatitis A,2016-05-17"+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567HE,Mexico,Hepatitis A,2016-06-02"+lineSeparator);
        fileWriter.flush();
        fileWriter.close();

        File targetFolder = temporaryFolder.createDirectory("output");
        String targetFile = targetFolder.getPath()+File.separator+"same-file.csv";
        schoonPostcodesOp(sourceFile.getPath(), targetFile);
        assertTrue(contentEquals(sourceFile, new File(targetFile)));

    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @DisplayName("Doelbestand is na verwerking ongelijk aan bronbestand (minder regels)")
    void schoonPostcodesOpLessLinesTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("file-with-empty-lines.csv");
        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Bezoeknr,Datum,Vestiging,Klant,Postcode,Land,Infectie,Vaccinatiedatum"+lineSeparator+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567HE,Mexico,DTP,2016-05-17"+lineSeparator+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567HE,Mexico,Hepatitis A,2016-05-17"+lineSeparator+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567HE,Mexico,Hepatitis A,2016-06-02"+lineSeparator+lineSeparator);
        fileWriter.flush();
        fileWriter.close();

        File targetFolder = temporaryFolder.createDirectory("output");
        String targetFile = targetFolder.getPath()+File.separator+"shorter-file.csv";
        schoonPostcodesOp(sourceFile.getPath(), targetFile);
        assertFalse(contentEquals(sourceFile, new File(targetFile)));

    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    @DisplayName("Doelbestand is na verwerking ongelijk aan bronbestand (gewijzigde postcodes)")
    void schoonPostcodesOpChangedCodeTest(TemporaryFolder temporaryFolder) throws Prik2GoException, IOException {
        File sourceFile = temporaryFolder.createFile("some-file.csv");
        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Bezoeknr,Datum,Vestiging,Klant,Postcode,Land,Infectie,Vaccinatiedatum"+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567 HE,Mexico,DTP,2016-05-17"+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567he,Mexico,Hepatitis A,2016-05-17"+lineSeparator);
        fileWriter.write("3,2016-05-13,Bolsward,1054,8567 he ,Mexico,Hepatitis A,2016-06-02"+lineSeparator);
        fileWriter.flush();
        fileWriter.close();

        File targetFolder = temporaryFolder.createDirectory("output");
        String targetFile = targetFolder.getPath()+File.separator+"different-file.csv";
        schoonPostcodesOp(sourceFile.getPath(), targetFile);
        assertFalse(contentEquals(sourceFile, new File(targetFile)));

    }

}
