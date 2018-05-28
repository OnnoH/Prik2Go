package data;

import domein.Prik2GoException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Mapper {
    private static final String DATABASE_URL = "jdbc:firebirdsql://10.0.149.72:3050/Prik2Go";
    private static final String DATABASE_USER = "sysdba";
    private static final String DATABASE_PASSWORD = "masterkey";
//    private static final String DRIVERNAME = "org.firebirdsql.jdbc.FBDriver";
    private Connection connection = null;

    public static final String DATUMFORMAT = "yyyy-MM-dd";

    public static Date fromStringToSQLDate(String datum) {
        Date sqlDate = null;
        SimpleDateFormat format = new SimpleDateFormat(DATUMFORMAT);
        java.util.Date utilDate = null;
        try {
            utilDate = format.parse(datum);
            sqlDate = new Date(utilDate.getTime());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        } finally {
            return sqlDate;
        }
    }

    public Mapper() {
        try {
            maakVerbinding();
        } catch (Prik2GoException p) {
            p.printStackTrace();
        }
    }

    private void maakVerbinding() throws Prik2GoException {
        Properties properties = new Properties();
        properties.setProperty("user", DATABASE_USER);
        properties.setProperty("password", DATABASE_PASSWORD);
        properties.setProperty("encoding", "UTF8");

        try {
            connection = DriverManager.getConnection(DATABASE_URL, properties);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het maken van verbinding met het database.\n" + e.getMessage());
        }
    }

    public Boolean findKlant(Connection connection, Long klantNr) throws Prik2GoException {

        Boolean klantFound = false;

        try {
            String sql = "SELECT COUNT(NR) AS ROWCOUNT FROM KLANT WHERE NR=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, klantNr);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            klantFound = (resultSet.getInt("ROWCOUNT") == 1);

        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar KLANT.\n" + e.getMessage());
        }

        return klantFound;
    }

    public PreparedStatement insertKlant(Connection connection) throws Prik2GoException {

        PreparedStatement preparedStatement;

        try {
            String sql = "INSERT INTO KLANT(NR, POSTCODE) VALUES (?,?)";
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor KLANT.\n" + e.getMessage());
        }

        return preparedStatement;

    }

    public Boolean findBezoek(Connection connection, Long bezoekNr) throws Prik2GoException {

        Boolean bezoekFound = false;

        try {
            String sql = "SELECT COUNT(NR) AS ROWCOUNT FROM BEZOEK WHERE NR=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, bezoekNr);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            bezoekFound = (resultSet.getInt("ROWCOUNT") == 1);

        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar BEZOEK.\n" + e.getMessage());
        }

        return bezoekFound;
    }

    public PreparedStatement insertBezoek(Connection connection) throws Prik2GoException {

        PreparedStatement preparedStatement = null;

        try {
            String sql = "INSERT INTO BEZOEK(NR, DATUM, VESTIGING, KLANT) VALUES (?,?,?,?)";
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor BEZOEK.\n" + e.getMessage());
        }

        return preparedStatement;

    }

    public PreparedStatement insertLandBezoek(Connection connection) throws Prik2GoException {

        PreparedStatement preparedStatement = null;

        try {
            String sql = "INSERT INTO LANDBEZOEK(ID, LAND, BEZOEK) VALUES (NEXT VALUE FOR GLANDBEZOEK, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor LANDBEZOEK.\n" + e.getMessage());
        }

        return preparedStatement;

    }

    public PreparedStatement insertVaccinatie(Connection connection) throws Prik2GoException {

        PreparedStatement preparedStatement = null;

        try {
            String sql = "INSERT INTO VACCINATIE(ID, VACCINATIESCHEMA, BEZOEK, DATUM) VALUES (NEXT VALUE FOR GVACCINATIE,?,?,?)";
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor VACCINATIE.\n" + e.getMessage());
        }

        return preparedStatement;

    }

    public Connection getConnection() {
        return connection;
    }
}