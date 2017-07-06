package drivers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.WebDriver;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DriverTest {

    private Driver driver;

    @Before
    public void setUp () {
        driver = new Driver () {};
    }

    @Test
    public void testGetSetterWebDriver () {
        WebDriver webdriverMock = mock(WebDriver.class);
        this.driver.setWebDriver(webdriverMock);
        assertEquals(webdriverMock, this.driver.getWebDriver());
    }

}
