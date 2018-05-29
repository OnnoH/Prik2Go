package data;

import domein.Prik2GoException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Mapper {
    private static String DATABASE_URL = "jdbc:firebirdsql://10.0.149.72:3050/Prik2Go";
    private static final String DATABASE_USER = "sysdba";
    private static final String DATABASE_PASSWORD = "masterkey";
    private static final String DATUMFORMAT = "yyyy-MM-dd";
    // DRIVERNAME is niet langer vereist voor recente Java/JDBC versies
    // private static final String DRIVERNAME = "org.firebirdsql.jdbc.FBDriver";
    private Connection connection = null;
    private PreparedStatement findKlantStatement;
    private PreparedStatement insertKlantStatement;
    private PreparedStatement findBezoekStatement;
    private PreparedStatement insertBezoekStatement;
    private PreparedStatement findLandStatement;
    private PreparedStatement findLandBezoekStatement;
    private PreparedStatement insertLandBezoekStatement;
    private PreparedStatement findVaccinatieStatement;
    private PreparedStatement insertVaccinatieStatement;
    private PreparedStatement controleerVaccinatie;

    /**
     * Convenience method om een datum string om te zetten naar een SQL datum
     * @param datum
     * @return de datum in SQLformaat, null indien ongeldig.
     */
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

    /**
     * Constructor. Maakt verbinding met de database in initialiseert de PreparedStatements
     */
    public Mapper() {
        try {
            maakVerbinding();
            findKlantStatement();
            findBezoekStatement();
            findLandStatement();
            findLandBezoekStatement();
            findVaccinatieStatement();
            insertKlantStatement();
            insertBezoekStatement();
            insertLandBezoekStatement();
            insertVaccinatieStatement();
            leesVaccinatieSchemaStatement();
        } catch (Prik2GoException p) {
            p.printStackTrace();
        }
    }

    /**
     * Maak een verbinding met de opgegeven database. De connection is beschikbaar via een getter.
     * @throws Prik2GoException
     */
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

    /**
     * PreparedStatement voor leesLand bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void findLandStatement() throws Prik2GoException {

        try {
            String sql = "SELECT CODE FROM LAND WHERE NAAM=?";
            findLandStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar LAND.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor leesKlant bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void findKlantStatement() throws Prik2GoException {

        try {
            String sql = "SELECT COUNT(NR) AS ROWCOUNT FROM KLANT WHERE NR=?";
            findKlantStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar KLANT.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor schrijfKlant bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void insertKlantStatement() throws Prik2GoException {
        try {
            String sql = "INSERT INTO KLANT(NR, POSTCODE) VALUES (?,?)";
            insertKlantStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor KLANT.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor schrijfBezoek bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void findBezoekStatement() throws Prik2GoException {

        try {
            String sql = "SELECT COUNT(NR) AS ROWCOUNT FROM BEZOEK WHERE NR=?";
            findBezoekStatement = connection.prepareStatement(sql);

        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar BEZOEK.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor schrijfBezoek bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void insertBezoekStatement() throws Prik2GoException {

        try {
            String sql = "INSERT INTO BEZOEK(NR, DATUM, VESTIGING, KLANT) VALUES (?,?,?,?)";
            insertBezoekStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor BEZOEK.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor leesLandBezoek bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void findLandBezoekStatement() throws Prik2GoException {

        try {
            String sql = "SELECT COUNT(ID) AS ROWCOUNT FROM LANDBEZOEK WHERE LAND=? AND BEZOEK=?";
            findLandBezoekStatement = connection.prepareStatement(sql);

        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar LANDBEZOEK.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor schrijfLandBezoek bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void insertLandBezoekStatement() throws Prik2GoException {

        try {
            String sql = "INSERT INTO LANDBEZOEK(ID, LAND, BEZOEK) VALUES (NEXT VALUE FOR GLANDBEZOEK, ?, ?)";
            insertLandBezoekStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor LANDBEZOEK.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor leesVaccinatie bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void findVaccinatieStatement() throws Prik2GoException {

        try {
            String sql = "SELECT ID FROM VACCINATIESCHEMA WHERE INFECTIE = ? AND VOLGNR = ?";
            findVaccinatieStatement = connection.prepareStatement(sql);

        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het zoeken naar VACCINATIESCHEMA.\n" + e.getMessage());
        }
    }

    /**
     * PreparedStatement voor schrijfVaccinatie bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void insertVaccinatieStatement() throws Prik2GoException {

        try {
            String sql = "INSERT INTO VACCINATIE(ID, VACCINATIESCHEMA, BEZOEK, DATUM) VALUES (NEXT VALUE FOR GVACCINATIE,?,?,?)";
            insertVaccinatieStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het initialiseren van de prepared statements voor VACCINATIE.\n" + e.getMessage());
        }

    }

    /**
     * PreparedStatement voor leesVaccinatieSchema bereikbaar via getter.
     * @throws Prik2GoException
     */
    private void leesVaccinatieSchemaStatement() throws Prik2GoException {

        try {
            String sql = "SELECT COUNT(ID) AS ROWCOUNT FROM VACCINATIE WHERE ID = ?";
            controleerVaccinatie = connection.prepareStatement(sql);

        } catch (SQLException e) {
            throw new Prik2GoException("Fout bij het controleren van VACCINATIESCHEMA.\n" + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement getFindKlantStatement() {
        return findKlantStatement;
    }

    public PreparedStatement getInsertKlantStatement() {
        return insertKlantStatement;
    }

    public PreparedStatement getFindBezoekStatement() {
        return findBezoekStatement;
    }

    public PreparedStatement getInsertBezoekStatement() {
        return insertBezoekStatement;
    }

    public PreparedStatement getInsertLandBezoekStatement() {
        return insertLandBezoekStatement;
    }

    public PreparedStatement getInsertVaccinatieStatement() {
        return insertVaccinatieStatement;
    }

    public PreparedStatement getFindVaccinatieStatement() {
        return findVaccinatieStatement;
    }

    public PreparedStatement getFindLandStatement() {
        return findLandStatement;
    }

    public PreparedStatement getControleerVaccinatie() {
        return controleerVaccinatie;
    }

    public PreparedStatement getFindLandBezoekStatement() {
        return findLandBezoekStatement;
    }

    public static void setDatabaseUrl(String databaseUrl) {
        DATABASE_URL = databaseUrl;
    }
}