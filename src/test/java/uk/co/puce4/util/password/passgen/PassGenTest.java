package uk.co.puce4.util.password.passgen;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class PassGenTest {

    @BeforeAll
    static void setup() throws Exception {
        // Only load the data needed for logic tests
        // Do NOT call PassGen.main()
        PassGen.loadDictionary();
    }

    /* @BeforeAll
    static void setup() {
        // Ensure the dictionary is loaded before any tests run
        // This validates that your words.txt is actually in the resources folder
        PassGen.main(new String[]{"--test-mode"});
    } */

    @Test
    @DisplayName("Verify generated password structure (3 words, 2 hyphens)")
    void testPasswordStructure() {
        String password = PassGen.generatePassword(3);
        String[] parts = password.split("-");

        assertEquals(3, parts.length, "Should have exactly 3 words");
        assertTrue(password.contains("-"), "Should contain hyphen separators");
    }

    @Test
    @DisplayName("Verify all words in password meet 4-6 character requirement")
    void testWordLengthConstraints() {
        // Run it 100 times to get a good statistical sample of the dictionary
        for (int i = 0; i < 100; i++) {
            String password = PassGen.generatePassword(3);
            for (String word : password.split("-")) {
                int len = word.length();
                assertTrue(len >= 4 && len <= 6,
                        "Word '" + word + "' has invalid length: " + len);
            }
        }
    }

    @Test
    @DisplayName("Test if passwords strictly alphabetic")
    void testDictionaryIntegrity() {
        // Assuming you make cachedWords package-private for the test
        for (String word : PassGen.cachedWords) {
            assertTrue(word.matches("^[a-z|A-Z]+$"),
                    "Dictionary contains non-alpha word: " + word);
        }
    }

}