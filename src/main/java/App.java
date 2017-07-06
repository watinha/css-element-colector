import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfFloat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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
            size, window_height, window_width, i;
        double [] screenshot_similarity = {-1, -1, -1};
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
                    screenshot = new File(App.folder_path +
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
            }
            screenshot_list = App.resize_images_similarly(screenshot_list, element_index, folder, filename);
            screenshot_similarity = App.chi_squared(screenshot_list);
            for (driver_index = 0; driver_index < screenshot_similarity.length; driver_index++) {
                writer.write(screenshot_similarity[driver_index] + "\t");
            }
            screenshot_similarity = App.image_diff(screenshot_list);
            for (driver_index = 0; driver_index < screenshot_similarity.length; driver_index++) {
                writer.write(screenshot_similarity[driver_index] + "\t");
            }
            writer.write("\n");
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
        if (top < 0) {
            height = height + top;
            top = 0;
        }
        if (left < 0) {
            width = width + left;
            left = 0;
        }
        if (top >= full_image.getHeight())
            top = full_image.getHeight() - 2;
        if (left >= full_image.getWidth())
            left = full_image.getWidth() - 2;
        if (top + height >= full_image.getHeight())
            height = full_image.getHeight() - top - 1;
        if (left + width >= full_image.getWidth())
            width = full_image.getWidth() - left - 1;
        sub_image = full_image.getSubimage(
                left, top,
                (width <= 0 ? 1 : width),
                (height <= 0 ? 1 : height));
        File file = new File(App.folder_path +
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

        File targetLocation = new File(App.folder_path +
                                        folder + "." + filename + "." + element_index + "." + driver_index + ".png");
        ImageIO.write(targetScreenshot, "png", targetLocation);
    }

    public static double [] image_diff (List <File> file_list) throws Exception {
		BufferedImage base = ImageIO.read(file_list.get(0)),
					  target;
		double [] results = new double[file_list.size()];
        int width = base.getWidth(null),
            height = base.getHeight(null);
        results[0] = 0; // comparing base to base

		for (int i = 1; i < file_list.size(); i++) {
            target = ImageIO.read(file_list.get(i));
            results[i] = 0;
            for (int y = 0; y < height; y++) {
              for (int x = 0; x < width; x++) {
                int rgb1 = base.getRGB(x, y);
                int rgb2 = target.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >>  8) & 0xff;
                int b1 = (rgb1      ) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >>  8) & 0xff;
                int b2 = (rgb2      ) & 0xff;
                results[i] += Math.abs(r1 - r2);
                results[i] += Math.abs(g1 - g2);
                results[i] += Math.abs(b1 - b2);
              }
            }
        }
        return results;
    }

    public static double [] chi_squared (List <File> file_list) throws Exception {
        double [] results = new double[file_list.size()];
        Mat base = Highgui.imread(file_list.get(0).getAbsolutePath()),
            target,
            hist_base = new Mat(),
            hist_target = new Mat();
        MatOfInt histSize = new MatOfInt(256, 256, 256),
                 channels = new MatOfInt(0, 1, 2);
        MatOfFloat ranges = new MatOfFloat(0.0f, 256.0f, 0.0f, 256.0f, 0.0f, 256.0f);
        results[0] = 0; // comparing base to base
        Imgproc.calcHist(Arrays.asList(base), channels, new Mat(), hist_base, histSize, ranges);
        for (int i = 1; i < file_list.size(); i++) {
            target = Highgui.imread(file_list.get(i).getAbsolutePath());
            Imgproc.calcHist(Arrays.asList(target), channels, new Mat(), hist_target, histSize, ranges);
            results[i] = Imgproc.compareHist(hist_base, hist_target, Imgproc.CV_COMP_CHISQR);
        }
        return results;
    }

    public static List <File> resize_images_similarly (List <File> files_list, int element_index,
                                                String folder, String filename) throws Exception {
        List <BufferedImage> images_list = new ArrayList <BufferedImage> ();
        List <File> resized_list = new ArrayList <File> ();
        int max_width = -1,
            max_height = -1,
            i;
        Image image;
        BufferedImage buf_image, new_buf_image;
        File file;
        Graphics2D graphics;
        for (i = 0; i < files_list.size(); i++) {
            file = files_list.get(i);
            buf_image = ImageIO.read(file);
            images_list.add(buf_image);
            if (max_width < buf_image.getWidth())
                max_width = buf_image.getWidth();
            if (max_height < buf_image.getHeight())
                max_height = buf_image.getHeight();
        }
        for (i = 0; i < images_list.size(); i++) {
            buf_image = images_list.get(i);
            file = files_list.get(i);
            image = buf_image.getScaledInstance(max_width, max_height, Image.SCALE_DEFAULT);
            new_buf_image = new BufferedImage(
                    image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            graphics = new_buf_image.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            file.delete();
            file = new File(App.folder_path +
                      folder + "." + filename + "." + element_index + "." + i + ".resized.png");
            ImageIO.write(new_buf_image, "png", file);
            resized_list.add(file);
        }
        return resized_list;
    }

    private static String folder_path = "/home/wwatanabe/Dropbox/artigos/xbi/xbi-css/results-mobile/201602/";

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List <WebDriver> lista_drivers = new ArrayList <> ();
        int j;
        lista_drivers.add(new RemoteWebDriver(new URL("http://192.168.0.15:4444/wd/hub"),
                                              DesiredCapabilities.chrome()));
        lista_drivers.add(new RemoteWebDriver(new URL("http://192.168.0.15:4444/wd/hub"),
                                              DesiredCapabilities.firefox()));
        lista_drivers.add(new RemoteWebDriver(new URL("http://192.168.0.15:4444/wd/hub"),
                                              DesiredCapabilities.internetExplorer()));
        FileWriter writer = new FileWriter(new File(App.folder_path + "elements.csv"));
        BufferedReader br_url = new BufferedReader(new FileReader("url_list_mobile.txt"));
        List <String> url_list = new ArrayList <> ();
        String u = br_url.readLine();
        while (u != null) {
            url_list.add(u);
            u = br_url.readLine();
        }
        br_url.close();

        writer.write("id");
        for (j = 0; j < lista_drivers.size(); j++) {
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
        for (j = 0; j < lista_drivers.size(); j++) {
            writer.write("\tchi-squared " + j);
        }
        for (j = 0; j < lista_drivers.size(); j++) {
            writer.write("\tdiff " + j);
        }
        writer.write("\n");

        for (String url : url_list) {
            System.out.println(url);
            String filename = url.substring(url.lastIndexOf("/") + 1),
                   folder = url.substring(url.lastIndexOf("/", url.lastIndexOf("/") - 1) + 1, url.lastIndexOf("/"));
            App.crawl_and_capture_screens(url, writer, folder, filename, lista_drivers);
        }
        writer.close();
        for (WebDriver d : lista_drivers)
            d.quit();
    }
}
