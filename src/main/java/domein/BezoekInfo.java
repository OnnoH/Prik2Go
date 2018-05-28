package domein;

import org.apache.commons.csv.*;
import java.io.*;
import java.util.*;

import static domein.Postcode.herstelPostcode;

public class BezoekInfo {

    private static final String[] csvHeader = {"Bezoeknr", "Datum" ,"Vestiging", "Klant", "Postcode", "Land", "Infectie", "Vaccinatiedatum"};
    private static final Character delimiter = ',';
    private static final Boolean debugMode = true;

    /**
     * Schrijft regels met correcte of correct gemaakte
     * postcodes uit bronbestand naar doelbestand.
     * @param bronbestand het bronbestand
     * @param doelbestand het doelbestand
     * @throws Prik2GoException bij IOException
     */
    public static void schoonPostcodesOp(String bronbestand , String doelbestand) throws Prik2GoException {

        BufferedReader fileReader = null;
        BufferedWriter fileWriter = null;
        CSVParser csvParser = null;
        CSVPrinter csvPrinter = null;
        Map<String, String> csvMap;

        try {
            String message = checkParameters(bronbestand, doelbestand);
            if (message == null) {

                fileReader = getReader(bronbestand);
                fileWriter = getWriter(doelbestand);
                csvParser = getParser(fileReader);
                csvPrinter = getPrinter(fileWriter);

                for (CSVRecord csvRecord : csvParser) {
                    if (csvRecord.getRecordNumber() > 1) {
                        // sla de eerste regel over
                        csvMap = processRecord(csvRecord);
                        csvPrinter.printRecord(csvMap.values());
                    }
                }
            } else {
                throw new Prik2GoException(message);
            }

        } catch (IOException e) {
            throw new Prik2GoException("Fout bij het lezen of schrijven van de bestanden.\n" + e.getMessage());
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
                if (csvParser != null) {
                    csvParser.close();
                }
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
                if (csvPrinter != null){
                    csvPrinter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Controleert de opgegeven bestandsnamen en geeft een melding indien ongeldig
     * @param bronbestand het bronbestand
     * @param doelbestand het doelbestand
     * @return tekst met foutmelding of null indien geldig
     */
    private static String checkParameters(String bronbestand, String doelbestand) {

        String message = null;

        bronbestand = ("".equals(bronbestand)) ? null : bronbestand;
        doelbestand = ("".equals(doelbestand)) ? null : doelbestand;

        if (bronbestand != null && doelbestand != null) {
            if (bronbestand.equals(doelbestand)) {
                message = "Specificatie van doelbestand mag niet gelijk zijn aan het bronbestand!";
            }
        } else {
            if (bronbestand == null && doelbestand == null) {
                message = "Specificatie van zowel het bron- als doelbestand is niet aanwezig.";
            } else {
                if (bronbestand == null) {
                    message = "Specificatie van het bronbestand ontbreekt.";
                } else {
                    message = "Specificatie van het doelbestand ontbreekt.";
                }
            }
        }

        logMessage(message);

        return message;
    }

    /**
     * Maakt een CSV reader
     * @param filename de naam van de reader
     * @return instantie van reader
     * @throws Prik2GoException bij IOException of ongeldige bestandskenmerken
     */
    private static BufferedReader getReader(String filename) throws Prik2GoException {
        File file;
        FileReader fileReader;
        BufferedReader bufferedReader;

        try {
            file = new File(filename);
            if (!file.exists()) {
                throw new Prik2GoException("Onvindbaar bestand " + filename);
            }
            if (file.length() == 0) {
                throw new Prik2GoException("Geen gegevens aanwezig in " + filename);
            }
            if (!file.canRead()) {
                throw new Prik2GoException("Lezen niet mogelijk van bestand " + filename);
            }
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
        } catch (IOException e) {
            throw new Prik2GoException("Er is een fout opgestreden bij het maken van de reader " + filename + ".\n" + e.getMessage());
        }

        return bufferedReader;
    }

    /**
     * Maakt een CSV writer
     * @param filename de naam van de writer
     * @return instantie van writer
     * @throws Prik2GoException bij IOException of ongeldige bestandskenmerken
     */
    private static BufferedWriter getWriter(String filename) throws Prik2GoException {
        File file;
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;

        try {
            file = new File(filename);
            if (file.exists()) {
                throw new Prik2GoException("Reeds bestaand bestand " + filename);
            }
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            throw new Prik2GoException("Er is een fout opgestreden bij het maken van de writer " + filename + ".\n" + e.getMessage());
        }

        return bufferedWriter;
    }

    /**
     * Maakt een CSV parser
     * @param bufferedReader de handle van de reader
     * @return instantie van parser
     * @throws Prik2GoException bij IOException
     */
    private static CSVParser getParser(BufferedReader bufferedReader) throws Prik2GoException {

        CSVParser csvParser;

        try {
            csvParser = new CSVParser(bufferedReader, CSVFormat
                    .DEFAULT
                    .withDelimiter(delimiter)
                    .withHeader(csvHeader)
                    .withIgnoreEmptyLines(true)
                    .withTrim());

        } catch (IOException e) {
            throw new Prik2GoException("Er is een fout opgetreden bij het maken van een parser\n" + e.getMessage());
        }

        return csvParser;
    }

    /**
     * Maakt een CSV printer
     * @param bufferedWriter de handle van de writer
     * @return instantie van printer
     * @throws Prik2GoException bij IOException
     */
    private static CSVPrinter getPrinter(BufferedWriter bufferedWriter) throws Prik2GoException {
        CSVPrinter csvPrinter;

        try {
            csvPrinter =  new CSVPrinter(bufferedWriter, CSVFormat
                    .DEFAULT
                    .withDelimiter(delimiter)
                    .withHeader(csvHeader));

        } catch (IOException e) {
            throw new Prik2GoException("Er is een fout opgetreden bij het maken van een printer\n" + e.getMessage());
        }

        return csvPrinter;
    }

    /**
     * Verwerkt een CSV record, toont een verslag in debugMode
     * @param csvRecord het te verwerken CSV record
     * @return map van het aangepaste record
     */
    private static Map<String, String> processRecord(CSVRecord csvRecord) {

        // herstel postcode
        String postcodeOud = csvRecord.get("Postcode");
        String postcodeNieuw = herstelPostcode(postcodeOud);

        if (debugMode) {
            if (postcodeNieuw == null) {
                System.out.println("Record: " + csvRecord.getRecordNumber() + " - " + postcodeOud + " verwijderd ");
            } else {
                if (!postcodeNieuw.equals(postcodeOud)) {
                    System.out.println("Record: " + csvRecord.getRecordNumber() + " - " + postcodeOud + " omgezet in " + postcodeNieuw);
                }
            }
        }

        // maak nieuw csvRecord van het type LinkedHashMap om de volgorde gelijk te houden
        Map<String, String> csvMap = new LinkedHashMap<>();
        for (String key : csvHeader) {
            if (key != null) {
                csvMap.put(key, csvRecord.get(key));
            }
        }

        // vervang de oude postcode door de nieuwe
        csvMap.replace("Postcode", postcodeNieuw);

        return csvMap;

    }

    /**
     * Print een bericht naar de console
     * @param message de af te drukken tekst
     */
    private static void logMessage(String message) {
        if (message != null) {
            System.out.println(message);
        }
    }

}
