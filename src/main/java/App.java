import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class App {

    public static void crawl_and_capture_screens (String url, FileWriter writer, String folder, String filename,
                                                  List <WebDriver> lista_drivers) throws Exception {
        List <List<WebElement>> all_elements_browsers = new ArrayList <List<WebElement>> ();
        int driver_index, number_of_elements = 0,
            height = -1, width = -1, top = -1, left = -1,
            relativeTopParent = -1, relativeLeftParent = -1,
            relativeTopPrevSibling = -1, relativeLeftPrevSibling = -1,
            relativeTopNextSibling = -1, relativeLeftNextSibling = -1,
            size, window_height, window_width;
        String tagName;
        WebElement target, target_parent, target_next_sibling, target_previous_sibling;
        WebDriver target_driver;
        File screenshot;
        List <File> screenshot_list = new ArrayList <File> ();

        for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
            target_driver = lista_drivers.get(driver_index);
            target_driver.get(url);
            target_driver.manage().window().maximize();
            if (((RemoteWebDriver) target_driver).getCapabilities().getBrowserName().equals("internet explorer")) {
                ((JavascriptExecutor) target_driver).executeScript(
                    "document.querySelector('html').style.width = document.querySelector('html').scrollWidth + 'px';" +
                    "document.querySelector('html').style.height = document.querySelector('html').scrollHeight + 'px';");
            }
            size = ((Long) ((JavascriptExecutor) target_driver).executeScript(
                    "window.elements = document.querySelectorAll('*');" +
                    "return window.elements.length;")).intValue();
            if (number_of_elements == 0 || number_of_elements > size)
                number_of_elements = size;
        }
        Thread.sleep(10000);

        for (int element_index = (number_of_elements - 1); element_index >= 0; element_index--) {
            screenshot_list.clear();
            for (driver_index = 0; driver_index < lista_drivers.size(); driver_index++) {
                target_driver = lista_drivers.get(driver_index);
                if (((RemoteWebDriver) target_driver).getCapabilities().getBrowserName().equals("chrome")) {
                    screenshot = new File("/media/willian/Seagate Expansion Drive/xbi-data-07-2016/" +
                              element_index + "." + driver_index + ".png");
                    Screenshot ashot_screenshot = new AShot().shootingStrategy(
                            ShootingStrategies.viewportPasting(500)).takeScreenshot(target_driver);
                    BufferedImage ashot_image = ashot_screenshot.getImage();
                    ImageIO.write(ashot_image, "PNG", screenshot);
                } else {
                    screenshot = ((TakesScreenshot) target_driver).getScreenshotAs(OutputType.FILE);
                }
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

                if (driver_index == 0) {
                    writer.write(folder + "." + filename + "." + element_index + "\t");
                }
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

                writer.write(tagName);
                writer.write("\t" + height + "\t" + width + "\t" + top + "\t" + left +
                             "\t" + relativeTopParent + "\t" + relativeLeftParent +
                             "\t" + relativeTopPrevSibling + "\t" + relativeLeftPrevSibling +
                             "\t" + relativeTopNextSibling + "\t" + relativeLeftNextSibling + "\t");

                App.save_element_position_screenshot(screenshot, target, element_index, folder, filename, driver_index);
                screenshot_list.add(App.save_target_screenshot(screenshot, target, element_index, folder, filename, driver_index));

                ((JavascriptExecutor) target_driver).executeScript(
                        "window.elements[" + element_index + "].style.opacity = 0;");
                screenshot.delete();
                if (driver_index == (lista_drivers.size() - 1)) {
                    writer.write("\n");
                }
            }
            App.resize_images_similarly(screenshot_list);
        }
    }

    public static File save_target_screenshot (File screenshot, WebElement target, int element_index,
                                               String folder, String filename, int driver_index) throws Exception {
        BufferedImage full_image = ImageIO.read(screenshot),
                      sub_image = null;
        int left = target.getLocation().getX(),
            top = target.getLocation().getY(),
            height = target.getSize().getHeight(),
            width = target.getSize().getWidth();
        if (top + height > full_image.getHeight())
            height = full_image.getHeight() - top;
        if (left + width > full_image.getWidth())
            width = full_image.getWidth() - left;
        sub_image = full_image.getSubimage(
                left, top,
                (width <= 0 ? 1 : width),
                (height <= 0 ? 1 : height));
        File file = new File("/media/willian/Seagate Expansion Drive/xbi-data-07-2016/" +
                              folder + "." + filename + "." + element_index + "." + driver_index + ".element.png");
        ImageIO.write(sub_image, "png", file);
        return file;
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

        File targetLocation = new File("/media/willian/Seagate Expansion Drive/xbi-data-07-2016/" +
                                        folder + "." + filename + "." + element_index + "." + driver_index + ".png");
        ImageIO.write(targetScreenshot, "png", targetLocation);
    }

    public static void resize_images_similarly (List <File> files_list) throws Exception {
        List <BufferedImage> images_list = new ArrayList <BufferedImage> ();
        int min_width = 9999,
            min_height = 9999,
            i;
        Image image;
        BufferedImage buf_image, new_buf_image;
        File file;
        Graphics2D graphics;
        for (i = 0; i < files_list.size(); i++) {
            file = files_list.get(i);
            buf_image = ImageIO.read(file);
            images_list.add(buf_image);
            if (min_width > buf_image.getWidth())
                min_width = buf_image.getWidth();
            if (min_height > buf_image.getHeight())
                min_height = buf_image.getHeight();
        }
        for (i = 0; i < images_list.size(); i++) {
            buf_image = images_list.get(0);
            file = files_list.get(0);
            file.delete();
            image = buf_image.getScaledInstance(min_width, min_height, Image.SCALE_DEFAULT);
            new_buf_image = new BufferedImage(
                    image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            graphics = new_buf_image.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            file.delete();
            file = new File("/media/willian/Seagate Expansion Drive/xbi-data-07-2016/" +
                      folder + "." + filename + "." + element_index + "." + driver_index + ".resized.png");
            ImageIO.write(new_buf_image, "png", file);
        }
    }

    public static void main(String[] args) throws Exception {
        List <WebDriver> lista_drivers = new ArrayList <> ();
        lista_drivers.add(new RemoteWebDriver(new URL("http://192.168.122.103:4444/wd/hub"),
                                              DesiredCapabilities.chrome()));
        lista_drivers.add(new RemoteWebDriver(new URL("http://192.168.122.103:4444/wd/hub"),
                                              DesiredCapabilities.firefox()));
        lista_drivers.add(new RemoteWebDriver(new URL("http://192.168.122.103:4444/wd/hub"),
                                              DesiredCapabilities.internetExplorer()));
        FileWriter writer = new FileWriter(new File("/media/willian/Seagate Expansion Drive/xbi-data-07-2016/elements.csv"));
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
                         "\tprev sibling top " + j +
                         "\tprev sibling left " + j +
                         "\tnext sibling top " + j +
                         "\tnext sibling left " + j);
        }
        writer.write("\n");

        for (String url : url_list) {
            String filename = url.substring(url.lastIndexOf("/") + 1),
                   folder = url.substring(url.lastIndexOf("/", url.lastIndexOf("/") - 1) + 1, url.lastIndexOf("/"));
            App.crawl_and_capture_screens(url, writer, folder, filename, lista_drivers);
        }
        writer.close();
        for (WebDriver d : lista_drivers)
            d.quit();
    }
}
