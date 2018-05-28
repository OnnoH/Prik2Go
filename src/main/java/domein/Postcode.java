package domein;

import java.util.Arrays;

/**
 * Klasse Postcode, gebruikt om de locatie van klanten en vestigingen vast te leggen.
 */
public class Postcode {

    private static String[] ongeldigeLetterCombinaties = new String[] { "SA", "SD", "SS" };
    private String postcode;
    private Double lat;
    private Double lng;
    private Plaats plaats;

    /**
     * Herstelt zo mogelijk een postcode
     * @param postcode de postcode
     * @return de correcte of correct gemaakte postcode,
     * null indien de postcode niet gecorrigeerd kan worden
     */
    public static String herstelPostcode(String postcode) {

        String hersteldePostcode;

        // Converteer naar bovenkast en verwijder de witspaties
        hersteldePostcode = postcode.toUpperCase().trim().replaceAll("\\s+", "");
        if (!controleerPostcode(hersteldePostcode)) {
            hersteldePostcode = null;
        }

        return hersteldePostcode;
    }

    /**
     * Controleert een postcode
     * @param postcode de postcode
     * @return true als de postcode voldoet aan de regels, false indien dit niet het geval is
     */
    private static Boolean controleerPostcode(String postcode) {

        Boolean controleerPostcode = false;
        String controleRegel = "^([1-9][0-9]{3})([A-Z]{2})$";

        if (postcode.matches(controleRegel)) {
            controleerPostcode = !Arrays.asList(ongeldigeLetterCombinaties).contains(postcode.substring(4, 6));
        }

        return controleerPostcode;
    }

    public static String[] getOngeldigeLetterCombinaties() {
        return ongeldigeLetterCombinaties;
    }

    public static void setOngeldigeLetterCombinaties(String[] ongeldigeLetterCombinaties) {
        Postcode.ongeldigeLetterCombinaties = ongeldigeLetterCombinaties;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Plaats getPlaats() {
        return plaats;
    }

    public void setPlaats(Plaats plaats) {
        this.plaats = plaats;
    }
}
