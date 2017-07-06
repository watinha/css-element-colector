package drivers;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class Driver {
    private WebDriver webdriver;

    protected void collectFeatures () {
        int numberOfElements = ((Integer) this.getJSExecutor().executeScript(
                "window.elements = document.querySelectorAll('*');" +
                "return window.elements.length;", null)).intValue();
    }

    private JavascriptExecutor getJSExecutor () { return (JavascriptExecutor) this.webdriver; }
    public WebDriver getWebDriver (){ return this.webdriver; }
    public void setWebDriver (WebDriver webdriver){ this.webdriver = webdriver; }
}
