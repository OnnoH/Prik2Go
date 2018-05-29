package data;

import domein.Prik2GoException;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static domein.BezoekInfo.*;

public class VulDatabase {

    private static Mapper mapper = new Mapper();

    /**
     * Vul de database met gegevens uit het CSV bestand
     * @param bronbestand
     */
    public static void vul(String bronbestand) {

        BufferedReader fileReader = null;
        CSVParser csvParser = null;
        Connection connection = null;
        Map<String, String> csvMap;

        if (bronbestand != null) {
            try {
                connection = mapper.getConnection();
                fileReader = getReader(bronbestand);
                csvParser = getParser(fileReader);
                for (CSVRecord csvRecord : csvParser) {
                    if (csvRecord.getRecordNumber() > 1) {
                        csvMap = csvRecord.toMap();
                        // Klanten zonder postcode verwerken we niet
                        if (!"".equals(csvMap.get("Postcode"))) {
                            if (!leesKlant(Integer.parseInt(csvMap.get("Klant")))) {
                                schrijfKlant(csvMap);
                            }
                            if (!leesBezoek(Integer.parseInt(csvMap.get("Bezoeknr")))) {
                                schrijfBezoek(csvMap);
                            }
                            if (!leesLandBezoek(csvMap.get("Land"), Integer.parseInt(csvMap.get("Bezoeknr")))) {
                                schrijfLandBezoek(csvMap);
                            }
                            schrijfVaccinatie(csvMap);
                        } else {
                            logMessage("Klant " + csvMap.get("Klant") + " is niet verwerkt." + csvMap.toString());
                        }
                    }
                }

            } catch(Prik2GoException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileReader != null) {
                        fileReader.close();
                    }
                    if (csvParser != null) {
                        csvParser.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException s) {
                    s.printStackTrace();
                }
            }
        }

    }

    /**
     * Bepaal de landcode op basis van de gegeven landnaam
     * @param landnaam
     * @return code van het land, null indien niet gevonden.
     */
    private static String leesLand(String landnaam) {
        String landcode = null;
        PreparedStatement leesLand = mapper.getFindLandStatement();
        try {
            leesLand.setString(1, landnaam);
            ResultSet resultSet = leesLand.executeQuery();
            while (resultSet.next()) {
                landcode = resultSet.getString("CODE");
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            return landcode;
        }
    }

    /**
     * Controleer of een KLANT met de gegeven klantnummer bestaat
     * @param klant
     * @return bestaat (true) of niet (false)
     */
    private static Boolean leesKlant(Integer klant) {
        Boolean bezoekFound = false;
        PreparedStatement leesKlant = mapper.getFindKlantStatement();
        try {
            leesKlant.setLong(1, klant);
            ResultSet resultSet = leesKlant.executeQuery();
            while (resultSet.next()) {
                bezoekFound = (resultSet.getInt("ROWCOUNT") == 1);
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            return bezoekFound;
        }

    }

    /**
     * Schrijf KLANT-record weg naar de database.
     * @param csvMap
     */
    private static void schrijfKlant(Map<String, String> csvMap) {
        PreparedStatement schrijfKlant = mapper.getInsertKlantStatement();
        try {
            schrijfKlant.setInt(1, Integer.parseInt(csvMap.get("Klant")));
            schrijfKlant.setString(2, csvMap.get("Postcode"));
            schrijfKlant.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    /**
     * Controleer of een BEZOEK met de gegeven bezoeknummer bestaat
     * @param bezoek
     * @return bestaat (true) of niet (false)
     */
    private static Boolean leesBezoek(Integer bezoek) {
        Boolean bezoekFound = false;
        PreparedStatement leesBezoek = mapper.getFindBezoekStatement();
        try {
            leesBezoek.setInt(1, bezoek);
            ResultSet resultSet = leesBezoek.executeQuery();
            while (resultSet.next()) {
                bezoekFound = (resultSet.getInt("ROWCOUNT") == 1);
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            return bezoekFound;
        }
    }

    /**
     * Schrijf BEZOEK-record weg naar de database.
     * @param csvMap
     */
    private static void schrijfBezoek(Map<String, String> csvMap) {
        PreparedStatement schrijfBezoek = mapper.getInsertBezoekStatement();
        try {
            schrijfBezoek.setInt(1, Integer.parseInt(csvMap.get("Bezoeknr")));
            schrijfBezoek.setDate(2, mapper.fromStringToSQLDate(csvMap.get("Datum")));
            schrijfBezoek.setString(3, csvMap.get("Vestiging"));
            schrijfBezoek.setInt(4, Integer.parseInt(csvMap.get("Klant")));
            schrijfBezoek.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
        }

    }

    /**
     * Controleer of een LANDBEZOEK met de gegeven landnaam/code en bezoeknummer bestaat
     * @param land
     * @param bezoekNr
     * @return bestaat (true) of niet (false)
     */
    private static Boolean leesLandBezoek(String land, Integer bezoekNr) {
        Boolean landBezoekFound = false;
        PreparedStatement leesLandBezoek = mapper.getFindLandBezoekStatement();
        try {
            leesLandBezoek.setString(1, leesLand(land));
            leesLandBezoek.setInt(2, bezoekNr);
            ResultSet resultSet = leesLandBezoek.executeQuery();
            while (resultSet.next()) {
                landBezoekFound = (resultSet.getInt("ROWCOUNT") == 1);
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            return landBezoekFound;
        }

    }

    /**
     * Schrijf LANDBEZOEK-record weg naar de database. Bepaal landcode op basis van landnaam.
     * @param csvMap
     */
    private static void schrijfLandBezoek(Map<String, String> csvMap) {
        PreparedStatement schrijfLandBezoek = mapper.getInsertLandBezoekStatement();
        try {
            schrijfLandBezoek.setString(1, leesLand(csvMap.get("Land")));
            schrijfLandBezoek.setInt(2, Integer.parseInt(csvMap.get("Bezoeknr")));
            schrijfLandBezoek.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
        }

    }

    /**
     * Bepaal het VACCINATIESCHEMA Id op basis van de infectie en een volgnr.
     * @param infectie
     * @param volgNr
     * @return het Id van het vaccinatieschema
     */
    private static Integer leesVaccinatie(String infectie, Integer volgNr) {
        Integer vaccinatieId = null;
        PreparedStatement leesVaccinatie = mapper.getFindVaccinatieStatement();
        try {
            leesVaccinatie.setString(1, infectie);
            leesVaccinatie.setInt(2, volgNr);
            ResultSet resultSet = leesVaccinatie.executeQuery();
            while (resultSet.next()) {
                vaccinatieId = resultSet.getInt("ID");
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            return vaccinatieId;
        }
    }

    /**
     * Controleer of een VACCINATIESCHEMA met het gegeven Id bestaat
     * @param id
     * @return id bestaat (true) of niet (false)
     */
    private static Boolean leesVaccinatieSchema(Integer id) {
        Boolean vaccinatieFound = false;

        try {
            if (id != null) {
                PreparedStatement controleerVaccinatie = mapper.getControleerVaccinatie();
                controleerVaccinatie.setInt(1, id);
                ResultSet resultSet = controleerVaccinatie.executeQuery();
                while (resultSet.next()) {
                    vaccinatieFound = (resultSet.getInt("ROWCOUNT") == 1);
                }
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            return vaccinatieFound;
        }
    }

    /**
     * Schrijf VACCINATIE-record weg naar de database. Bepaal eerstvolgende vaccinatieId op basis van volgnr.
     * @param csvMap
     */
    private static void schrijfVaccinatie(Map<String, String> csvMap) {
        PreparedStatement schrijfVaccinatie = mapper.getInsertVaccinatieStatement();
        try {
            // Zoek de laatste vaccinatie
            String infectie = csvMap.get("Infectie");
            Integer volgNr = 1;
            Integer id;
            do {
                id = leesVaccinatie(infectie, volgNr++);
            } while (leesVaccinatieSchema(id));

            if (id != null) {
                schrijfVaccinatie.setInt(1, id);
                schrijfVaccinatie.setInt(2, Integer.parseInt(csvMap.get("Bezoeknr")));
                schrijfVaccinatie.setDate(3, mapper.fromStringToSQLDate(csvMap.get("Vaccinatiedatum")));
                schrijfVaccinatie.executeUpdate();
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

}
