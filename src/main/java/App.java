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

    public static void crawl_and_capture_screens (String url, FileWriter writer, String folder, String filename,
                                                  List <WebDriver> lista_drivers) throws IOException, InterruptedException {
        List <List<WebElement>> all_elements_browsers = new ArrayList <List<WebElement>> ();
        int driver_index, number_of_elements,
            height = 0, width = 0, top = 0, left = 0,
            relativeTopParent = 0, relativeLeftParent = 0,
            relativeTopPrevSibling = 0, relativeLeftPrevSibling = 0,
            relativeTopNextSibling = 0, relativeLeftNextSibling = 0;

        for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
            lista_drivers.get(driver_index).get(url);
            lista_drivers.get(driver_index).manage().window().maximize();
            ((JavascriptExecutor) lista_drivers.get(driver_index)).executeScript(
                    "window.elements = document.querySelectorAll('*');");
        }
        Thread.sleep(10000);

    }

    public static void save_element_position_screenshot (File screenshot, WebElement target, int element_index,
                                                         String folder, String filename, int driver_index) throws Exception {
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
                if (image_x < point.getX() || image_x > (point.getX() + width) ||
                    image_y < point.getY() || image_y > (point.getY() + height)) {
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

        File targetLocation = new File("data/" + folder + "." + filename + "." + element_index + "." + driver_index + ".png");
        ImageIO.write(targetScreenshot, "png", targetLocation);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        WebDriver driver = new FirefoxDriver();
        List <WebDriver> lista_drivers = new ArrayList <> ();
        lista_drivers.add(new FirefoxDriver());
        lista_drivers.add(new ChromeDriver());
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        FileWriter writer = new FileWriter(new File("data/elements.csv"));
        BufferedReader br_url = new BufferedReader(new FileReader("url_list.txt"));
        List <String> url_list = new ArrayList <> ();
        String u = br_url.readLine();
        while (u != null) {
            url_list.add(u);
            u = br_url.readLine();
        }
        br_url.close();

        writer.write("id\ttagname\theight\twidth\ttop\tleft\trelative top\trelative left");
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
            App.crawl_and_capture_screens(url, writer, folder, filename, lista_drivers);
        }
        writer.close();
        driver.quit();
        for (WebDriver d : lista_drivers)
            d.quit();
    }
}
