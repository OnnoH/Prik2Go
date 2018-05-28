package domein;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@DisplayName("Testen van postcodes")
class TestPostcode {

    @Test
    @DisplayName("Herstel postcodes")
    void herstelPostcodeTest() {
        Postcode postcode = new Postcode(); // unitUnderTest.methodUnderTest();

        assertAll("Goede postcode",
                () -> assertEquals("1234AA", postcode.herstelPostcode("1234AA"))
        );

        assertAll("Goede herstelde postcodes",
                () -> assertEquals("1234AA", postcode.herstelPostcode("1234 AA")),
                () -> assertEquals("1234AA", postcode.herstelPostcode(" 1234 AA ")),
                () -> assertEquals("1234AA", postcode.herstelPostcode("1234aa")),
                () -> assertEquals("1234AA", postcode.herstelPostcode("1234 aa"))
        );
        assertAll("Foutieve niet te herstellen postcodes",
                () -> assertNull(postcode.herstelPostcode("0123AA")),  // Mag niet met een 0 beginnen
                () -> assertNull(postcode.herstelPostcode("AA1234")),  // Mag niet met een letter beginnen
                () -> assertNull(postcode.herstelPostcode("123AA")),   // Te kort
                () -> assertNull(postcode.herstelPostcode("1234AAA")), // Te lang
                () -> assertNull(postcode.herstelPostcode("123456")),  // Alleen cijfers
                () -> assertNull(postcode.herstelPostcode("AAAAAA")),  // Alleen letters
                () -> assertNull(postcode.herstelPostcode("123AAA")),  // Te weinig cijfers, teveel letters
                () -> assertNull(postcode.herstelPostcode("12345A")),  // Te veel cijfers, te weinig letters
                () -> assertNull(postcode.herstelPostcode("1234SA")),  // Ongeldige lettercombinatie
                () -> assertNull(postcode.herstelPostcode("1234SD")),  // Ongeldige lettercombinatie
                () -> assertNull(postcode.herstelPostcode("1234SS"))   // Ongeldige lettercombinatie
        );
    }
}
