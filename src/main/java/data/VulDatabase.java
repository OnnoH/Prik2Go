package data;

import domein.Prik2GoException;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static domein.BezoekInfo.*;

public class VulDatabase {

    private static Mapper mapper = new Mapper();

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
                        if (!mapper.findKlant(connection, Long.parseLong(csvMap.get("Klant")))) {
                            schrijfKlant(connection, csvMap);
                        }
                        if (!mapper.findBezoek(connection, Long.parseLong(csvMap.get("Bezoeknr")))) {
                            schrijfBezoek(connection, csvMap);
                        }
                        schrijfLandBezoek(connection, csvMap);
                        schrijfVaccinatie(connection, csvMap);
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

    private static void schrijfKlant(Connection connection, Map<String, String> csvMap) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = mapper.insertKlant(connection);
            preparedStatement.setInt(1, Integer.parseInt(csvMap.get("Klant")));
            preparedStatement.setString(2, csvMap.get("Postcode"));
            preparedStatement.executeUpdate();
        } catch (Prik2GoException p) {
            p.printStackTrace();
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private static void schrijfBezoek(Connection connection, Map<String, String> csvMap) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = mapper.insertBezoek(connection);
            preparedStatement.setInt(1, Integer.parseInt(csvMap.get("Bezoeknr")));
            preparedStatement.setDate(2, mapper.fromStringToSQLDate(csvMap.get("Datum")));
            preparedStatement.setString(3, csvMap.get("Vestiging"));
            preparedStatement.setInt(4, Integer.parseInt(csvMap.get("Klant")));
            preparedStatement.executeUpdate();
        } catch (Prik2GoException p) {
            p.printStackTrace();
        } catch (SQLException s) {
            s.printStackTrace();
        }

    }

    private static void schrijfLandBezoek(Connection connection, Map<String, String> csvMap) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = mapper.insertLandBezoek(connection);
            preparedStatement.setString(1, csvMap.get("Land"));
            preparedStatement.setInt(2, Integer.parseInt(csvMap.get("Bezoeknr")));
            preparedStatement.executeUpdate();
        } catch (Prik2GoException p) {
            p.printStackTrace();
        } catch (SQLException s) {
            s.printStackTrace();
        }

    }

    private static void schrijfVaccinatie(Connection connection, Map<String, String> csvMap) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = mapper.insertVaccinatie(connection);
            preparedStatement.setString(1, csvMap.get("Infectie"));
            preparedStatement.setInt(2, Integer.parseInt(csvMap.get("Bezoeknr")));
            preparedStatement.setDate(3, mapper.fromStringToSQLDate(csvMap.get("Datum")));
            preparedStatement.executeUpdate();
        } catch (Prik2GoException p) {
            p.printStackTrace();
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

}
