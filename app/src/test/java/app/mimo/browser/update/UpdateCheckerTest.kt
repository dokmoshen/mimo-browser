package app.mimo.browser.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun detectsNewerVersions() {
        assertTrue(UpdateChecker.isNewer("0.1.6", "0.1.5"))
        assertTrue(UpdateChecker.isNewer("1.0.0", "0.9.9"))
        assertTrue(UpdateChecker.isNewer("0.2", "0.1.9"))
    }

    @Test
    fun ignoresSameAndOlderVersions() {
        assertFalse(UpdateChecker.isNewer("0.1.6", "0.1.6"))
        assertFalse(UpdateChecker.isNewer("0.1.5", "0.1.6"))
        assertFalse(UpdateChecker.isNewer("v", "0.1.6"))
    }
}
