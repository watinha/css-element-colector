package edu.utfpr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class App {

    public static void main(String[] args) throws IOException {
        WebDriver driver = new FirefoxDriver();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        FileWriter writer = new FileWriter(new File("data/r.csv"));
        String url = "file:///home/willian/Dropbox/artigos/xbi-css/201302/1.html";
        List <WebElement> all_elements;

        driver.get(url);
        driver.manage().window().maximize();
        all_elements = driver.findElements(By.cssSelector("*"));
        executor.executeScript("window.elements = document.querySelectorAll('*');");
        writer.write(executor.executeScript(
            "window.styles_t = window.getComputedStyle(window.elements[0], null);" +
            "window.result = [];" +
            "for (var i in window.styles_t) {" +
                "if (Number.isNaN(parseInt(i)) && typeof window.styles_t[i] !== 'function')" +
                    "window.result.push(i);" +
            "}" +
            "return window.result.join('|')"
        ).toString());

        for (int i = 0; i < all_elements.size(); i++) {
            writer.write("\n");
            writer.write(executor.executeScript(
                "window.styles_t = window.getComputedStyle(window.elements[" + i + "], null);" +
                "window.result = [];" +
                "for (var i in window.styles_t) {" +
                    "if (Number.isNaN(parseInt(i)) && typeof window.styles_t[i] !== 'function')" +
                        "window.result.push(window.styles_t[i]);" +
                "}" +
                "return window.result.join('|')"
            ).toString());
        }
        writer.close();

        List <WebDriver> lista_drivers = new ArrayList <> ();
        lista_drivers.add(new FirefoxDriver());
        lista_drivers.add(new ChromeDriver());

        for (int driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
            lista_drivers.get(driver_index).get(url);
            lista_drivers.get(driver_index).manage().window().maximize();
            all_elements = lista_drivers.get(driver_index).findElements(By.cssSelector("*"));
            File screenshot = ((TakesScreenshot) lista_drivers.get(driver_index)).getScreenshotAs(OutputType.FILE);
            for (int i = 0; i < all_elements.size(); i++) {
                WebElement target = all_elements.get(i);
                BufferedImage fullimg = ImageIO.read(screenshot);
                Point point = target.getLocation();
                int width = (target.getSize().getWidth() != 0 ?
                                target.getSize().getWidth() : 1),
                    height = (target.getSize().getHeight() != 0 ?
                                target.getSize().getHeight() : 1);
                BufferedImage targetScreenshot = fullimg.getSubimage(
                        0, 0, fullimg.getWidth(), fullimg.getHeight());

                for (int image_x = 0; image_x < width; image_x++) {
                    for (int image_y = 0; image_y < height; image_y++) {
                        targetScreenshot.setRGB(
                                point.getX() + image_x, point.getY() + image_y,
                                    (new Color(255, 255, 255, 255)).hashCode());
                    }
                }

                File targetLocation = new File("data/" + i + "." + driver_index + ".png");
                ImageIO.write(targetScreenshot, "png", targetLocation);
            }
            lista_drivers.get(driver_index).quit();
        }
    }
}
