package edu.utfpr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class App {

    public static void main(String[] args) throws IOException {
        WebDriver driver = new FirefoxDriver();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        FileWriter writer = new FileWriter(new File("r.csv"));
        driver.get("file:///home/willian/Dropbox/artigos/xbi-css/201302/1.html");
        executor.executeScript("window.elements = document.querySelectorAll('*');");
        writer.write(executor.executeScript(
            "window.styles_t = window.getComputedStyle(window.elements[0], null);" +
            "window.result = [];" +
            "for (var i in window.styles_t) {" +
                "if (!Number.isInteger(i) && typeof window.styles_t[i] !== 'function')" +
                    "window.result.push(i);" +
            "}" +
            "return window.result.join(',')"
        ).toString());
        writer.close();
    }
}
