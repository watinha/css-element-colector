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
                                                  List <WebDriver> lista_drivers) throws Exception {
        List <List<WebElement>> all_elements_browsers = new ArrayList <List<WebElement>> ();
        int driver_index, number_of_elements = 0,
            height = -1, width = -1, top = -1, left = -1,
            relativeTopParent = -1, relativeLeftParent = -1,
            relativeTopPrevSibling = -1, relativeLeftPrevSibling = -1,
            relativeTopNextSibling = -1, relativeLeftNextSibling = -1;
        String tagName;
        WebElement target, target_parent, target_next_sibling, target_previous_sibling;
        WebDriver target_driver;
        File screenshot;

        for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
            int size;
            lista_drivers.get(driver_index).get(url);
            lista_drivers.get(driver_index).manage().window().maximize();
            size = ((Long) ((JavascriptExecutor) lista_drivers.get(driver_index)).executeScript(
                    "window.elements = document.querySelectorAll('*');" +
                    "return window.elements.length;")).intValue();
            if (number_of_elements == 0 || number_of_elements > size)
                number_of_elements = size;
        }
        Thread.sleep(10000);

        for (int element_index = (number_of_elements - 1); element_index >= 0; element_index--) {
            for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
                screenshot = ((TakesScreenshot) lista_drivers.get(driver_index)).getScreenshotAs(OutputType.FILE);
                target_driver = lista_drivers.get(driver_index);
                target = (WebElement) ((JavascriptExecutor) target_driver).executeScript(
                        "return window.elements[" + element_index + "];");
                target_parent = (WebElement) ((JavascriptExecutor) target_driver).executeScript(
                        "return window.elements[" + element_index + "].parentElement;");
                target_previous_sibling = (WebElement) ((JavascriptExecutor) target_driver).executeScript(
						"var target = window.elements[" + element_index + "], p;" +
                        "while (target.parentElement != null && target.previousElementSibling == null)" +
                        "   target = target.parentElement;" +
                        "return target.previousElementSibling;");
                target_next_sibling = (WebElement) ((JavascriptExecutor) target_driver).executeScript(
						"var target = window.elements[" + element_index + "], p;" +
                        "while (target.parentElement != null && target.nextElementSibling == null)" +
                        "   target = target.parentElement;" +
                        "return target.nextElementSibling;");

                tagName = target.getTagName();
                height = target.getSize().getHeight();
                width = target.getSize().getWidth();
                top = target.getLocation().getY();
                left = target.getLocation().getX();
                if (target_parent != null) {
                    relativeTopParent = target_parent.getLocation().getY();
                    relativeLeftParent = target_parent.getLocation().getX();
                }
                if (target_previous_sibling != null) {
                    relativeTopPrevSibling = target_previous_sibling.getLocation().getY();
                    relativeLeftPrevSibling = target_previous_sibling.getLocation().getX();
                }
                if (target_next_sibling != null) {
                    relativeTopNextSibling = target_next_sibling.getLocation().getY();
                    relativeLeftNextSibling = target_next_sibling.getLocation().getX();
                }

                writer.write(folder + "." + filename + "." + element_index + "\t");
                writer.write(tagName + "\t");
                writer.write("\t" + height + "\t" + width + "\t" + top + "\t" + left +
                             "\t" + relativeTopParent + "\t" + relativeLeftParent +
                             "\t" + relativeTopPrevSibling + "\t" + relativeLeftPrevSibling +
                             "\t" + relativeTopNextSibling + "\t" + relativeLeftNextSibling);

                App.save_element_position_screenshot(screenshot, target, element_index, folder, filename, driver_index);

                ((JavascriptExecutor) target_driver).executeScript(
                        "window.elements[" + element_index + "].style.opacity = 0;");
            }
            writer.write("\n");
        }
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

    public static void main(String[] args) throws Exception {
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

        writer.write("id");
        for (int j = 0; j < lista_drivers.size(); j++) {
            writer.write("\ttagname" + j +
						 "\theight " + j +
                         "\twidth " + j +
                         "\ttop " + j +
                         "\tleft " + j +
                         "\tparent top " + j +
                         "\tparent left " + j +
                         "\tprevious sibling top " + j +
                         "\tprevious sibling left " + j +
                         "\tnext sibling top " + j +
                         "\tnext sibling left " + j);
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
