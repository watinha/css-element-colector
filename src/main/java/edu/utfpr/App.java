package edu.utfpr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
        FileWriter writer = new FileWriter(new File("data/elements.csv"));
        BufferedReader br = new BufferedReader(new FileReader("css-attributes-selection.txt"));
        String url = "file:///home/willian/Dropbox/artigos/xbi-css/201302/1.html";
        String css_attributes = "[",
               attr = br.readLine();

        while (attr != null) {
            css_attributes += "\"" + attr + "\"";
            attr = br.readLine();
            if (attr == null) {
                css_attributes += "]";
                break;
            }
            css_attributes += ",";
        }
        br.close();


        List <WebElement> all_elements;
        driver.get(url);
        driver.manage().window().maximize();
        all_elements = driver.findElements(By.cssSelector("*"));
        executor.executeScript("window.elements = document.querySelectorAll('*');");
        writer.write(executor.executeScript(
            // "window.styles_t = window.getComputedStyle(window.elements[0], null);" +
            "window.css_attributes = " + css_attributes + ";" +
            "return window.css_attributes.join('\t')"
        ).toString());

        for (int i = 0; i < all_elements.size(); i++) {
            writer.write("\n");
            writer.write(executor.executeScript(
                "window.styles_t = window.getComputedStyle(window.elements[" + i + "], null);" +
                "window.result = [];" +
                "for (var i in window.css_attributes) {" +
                    "if (Number.isNaN(parseInt(window.css_attributes[i])) && typeof window.styles_t[window.css_attributes[i]] !== 'function')" +
                        "window.result.push(window.styles_t[window.css_attributes[i]]);" +
                "}" +
                "return window.result.join('\t')"
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

                for (int image_x = 0; image_x < fullimg.getWidth(); image_x++) {
                    for (int image_y = 0; image_y < fullimg.getHeight(); image_y++) {
                        if (image_x <= point.getX() || image_x >= (point.getX() + width) ||
                            image_y <= point.getY() || image_y >= (point.getY() + height)) {
                            Color c = new Color(targetScreenshot.getRGB(image_x, image_y));
                            targetScreenshot.setRGB(image_x, image_y,
                                (new Color(
                                    (int) Math.floor(0.1 * c.getRed()),
                                    (int) Math.floor(0.1 * c.getGreen()),
                                    (int) Math.floor(0.1 * c.getBlue()),
                                    255
                                )).hashCode());
                        }
                    }
                }

                File targetLocation = new File("data/" + i + "." + driver_index + ".png");
                ImageIO.write(targetScreenshot, "png", targetLocation);
            }
            lista_drivers.get(driver_index).quit();
        }
    }
}
