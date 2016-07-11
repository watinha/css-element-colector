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
import org.openqa.selenium.ie.InternetExplorerDriver;

public class App {

    public static void crawl_and_capture_screens (String url, String css_attributes,
                                                  FileWriter writer, String folder, String filename,
                                                  List <WebDriver> lista_drivers) throws IOException, InterruptedException {
        List <List<WebElement>> all_elements_browsers = new ArrayList <List<WebElement>> ();
        int driver_index, size_elements;
        JavascriptExecutor executor2;

        for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
            lista_drivers.get(driver_index).get(url);
            lista_drivers.get(driver_index).manage().window().maximize();
            all_elements_browsers.add(lista_drivers.get(driver_index).findElements(By.cssSelector("*")));
        }
        Thread.sleep(10000);

        for (int i = 0; i < all_elements_browsers.get(0).size(); i++) {
            int offsetHeight = 0,
                offsetWidth = 0,
                offsetTop = 0,
                offsetLeft = 0,
                relativeOffsetTop = 0,
                relativeOffsetLeft = 0,
                relativeTopPrevSibling = 0,
                relativeLeftPrevSibling = 0,
                relativeTopNextSibling = 0,
                relativeLeftNextSibling = 0;
            for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
                executor2 = (JavascriptExecutor) lista_drivers.get(driver_index);
                File screenshot = ((TakesScreenshot) lista_drivers.get(driver_index)).getScreenshotAs(OutputType.FILE);
                if (driver_index == 0) {
                    writer.write("\n");
                    writer.write(folder + "." + filename + "." + i + "\t" + executor2.executeScript(
                        "window.styles_t = window.getComputedStyle(window.elements[" + i + "], null);" +
                        "window.result = [];" +
                        "for (var i in window.css_attributes) {" +
                            "if (Number.isNaN(parseInt(window.css_attributes[i])) && typeof window.styles_t[window.css_attributes[i]] !== 'function')" +
                                "window.result.push(window.styles_t[window.css_attributes[i]]);" +
                        "}" +
                        "return window.result.join('\t')"
                    ).toString());
                    offsetHeight = Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetHeight").toString());
                    offsetWidth  = Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetWidth").toString());
                    offsetTop    = Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetTop").toString());
                    offsetLeft   = Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetLeft").toString());
                    relativeOffsetTop = Integer.parseInt(executor2.executeScript(
                        "if (window.elements[" + i + "].parentElement)" +
                        "return window.elements[" + i + "].offsetTop - window.elements[" + i + "].parentElement.offsetTop;" +
                        "return window.elements[" + i + "].offsetTop;").toString());
                    relativeOffsetLeft = Integer.parseInt(executor2.executeScript(
                        "if (window.elements[" + i + "].parentElement)" +
                        "return window.elements[" + i + "].offsetLeft - window.elements[" + i + "].parentElement.offsetLeft;" +
                        "return window.elements[" + i + "].offsetLeft;").toString());

                    relativeTopPrevSibling = Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.previousElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.previousElementSibling)" +
                        "return window.elements[" + i + "].offsetTop - target.previousElementSibling.offsetTop;" +
                        "return 0;").toString());
                    relativeLeftPrevSibling = Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.previousElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.previousElementSibling)" +
                        "return window.elements[" + i + "].offsetLeft - target.previousElementSibling.offsetLeft;" +
                        "return 0;").toString());
                    relativeTopNextSibling = Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.nextElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.nextElementSibling)" +
                        "return window.elements[" + i + "].offsetTop - target.nextElementSibling.offsetTop;" +
                        "return 0;").toString());
                    relativeLeftNextSibling = Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.nextElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.nextElementSibling)" +
                        "return window.elements[" + i + "].offsetLeft - target.nextElementSibling.offsetLeft;" +
                        "return 0;").toString());

                    writer.write("\t" + offsetHeight);
                    writer.write("\t" + offsetWidth);
                    writer.write("\t" + offsetTop);
                    writer.write("\t" + offsetLeft);
                    writer.write("\t" + relativeOffsetTop);
                    writer.write("\t" + relativeOffsetLeft);

                    writer.write("\t" + relativeTopPrevSibling);
                    writer.write("\t" + relativeLeftPrevSibling);
                    writer.write("\t" + relativeTopNextSibling);
                    writer.write("\t" + relativeLeftNextSibling);
                } else {
                    writer.write("\t" +
                            Math.abs(offsetHeight - Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetHeight").toString())));
                    writer.write("\t" +
                            Math.abs(offsetWidth - Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetWidth").toString())));
                    writer.write("\t" +
                            Math.abs(offsetTop - Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetTop").toString())));
                    writer.write("\t" +
                            Math.abs(offsetLeft - Integer.parseInt(executor2.executeScript(
                                    "return window.elements[" + i + "].offsetLeft").toString())));
                    writer.write("\t" + Math.abs(relativeOffsetTop - Integer.parseInt(executor2.executeScript(
                        "if (window.elements[" + i + "].parentElement)" +
                        "return window.elements[" + i + "].offsetTop - window.elements[" + i + "].parentElement.offsetTop;" +
                        "return 0;").toString())));
                    writer.write("\t" + Math.abs(relativeOffsetLeft- Integer.parseInt(executor2.executeScript(
                        "if (window.elements[" + i + "].parentElement)" +
                        "return window.elements[" + i + "].offsetLeft - window.elements[" + i + "].parentElement.offsetLeft;" +
                        "return 0;").toString())));

                    writer.write("\t" + Math.abs(relativeTopPrevSibling - Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.previousElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.previousElementSibling)" +
                        "return window.elements[" + i + "].offsetTop - target.previousElementSibling.offsetTop;" +
                        "return 0;").toString())));
                    writer.write("\t" + Math.abs(relativeLeftPrevSibling - Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.previousElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.previousElementSibling)" +
                        "return window.elements[" + i + "].offsetLeft - target.previousElementSibling.offsetLeft;" +
                        "return 0;").toString())));
                    writer.write("\t" + Math.abs(relativeTopNextSibling - Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.nextElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.nextElementSibling)" +
                        "return window.elements[" + i + "].offsetTop - target.nextElementSibling.offsetTop;" +
                        "return 0;").toString())));
                    writer.write("\t" + Math.abs(relativeLeftNextSibling - Integer.parseInt(executor2.executeScript(
                        "var target = window.elements[" + i + "], p;" +
                        "while (target.parentElement != null && target.nextElementSibling === null)" +
                        "   target = target.parentElement;" +
                        "if (target.nextElementSibling)" +
                        "return window.elements[" + i + "].offsetLeft - target.nextElementSibling.offsetLeft;" +
                        "return 0;").toString())));
                }

                WebElement target = all_elements_browsers.get(driver_index).get(i);
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

                File targetLocation = new File("data/" + folder + "." + filename + "." + i + "." + driver_index + ".png");
                ImageIO.write(targetScreenshot, "png", targetLocation);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        WebDriver driver = new FirefoxDriver();
        List <WebDriver> lista_drivers = new ArrayList <> ();
        lista_drivers.add(new FirefoxDriver());
        lista_drivers.add(new ChromeDriver());
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        FileWriter writer = new FileWriter(new File("data/elements.csv"));
        BufferedReader br = new BufferedReader(new FileReader("css-attributes-selection.txt"));
        BufferedReader br_url = new BufferedReader(new FileReader("url_list.txt"));
        List <String> url_list = new ArrayList <> ();
        String u = br_url.readLine();
        while (u != null) {
            url_list.add(u);
            u = br_url.readLine();
        }
        br_url.close();

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

        driver.get(url_list.get(0));
        driver.manage().window().maximize();
        writer.write("id\t" + executor.executeScript(
            "window.css_attributes = " + css_attributes + ";" +
            "return window.css_attributes.join('\t')"
        ).toString());
        writer.write("\theight\twidth\ttop\tleft\trelative top\trelative left");
        writer.write("\trelative prev top\trelative prev left\trelative next top\trelative next left");
        for (int j = 1; j < lista_drivers.size(); j++) {
            writer.write("\theight diff " + j +
                         "\twidth diff " + j +
                         "\ttop diff " + j +
                         "\tleft diff " + j +
                         "\tparent top diff " + j +
                         "\tparent left diff " + j +
                         "\tprevious sibling top diff " + j +
                         "\tprevious sibling left diff " + j +
                         "\tnext sibling top diff " + j +
                         "\tnext sibling left diff " + j);
        }

        for (String url : url_list) {
            String filename = url.substring(url.lastIndexOf("/") + 1),
                   folder = url.substring(url.lastIndexOf("/", url.lastIndexOf("/") - 1) + 1, url.lastIndexOf("/"));
            App.crawl_and_capture_screens(url, css_attributes, writer, folder, filename, lista_drivers);
        }
        writer.close();
        driver.quit();
        for (WebDriver d : lista_drivers)
            d.quit();
    }
}
