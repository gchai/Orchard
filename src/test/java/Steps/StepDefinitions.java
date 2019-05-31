package Steps;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


public class StepDefinitions {
    private static Configurations configurations = new Configurations();
    private static Configuration config;
    private static WebDriver driver;
    private static WebDriverWait standardWait;

    private static Configuration importProperties() {
        Configuration config = null;
        try {
            Path path = Paths.get("");
            config = configurations.properties(path.toAbsolutePath() + "/src/test/resources/app.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static void initDriver() {
        Path path = Paths.get("");
        String os = System.getProperty("os.name");
        if (os.contains("Win")) {
            System.setProperty("webdriver.chrome.driver", path.toAbsolutePath() + "/src/test/resources/chromedriver.exe");
        } else {
            System.setProperty("webdriver.chrome.driver", path.toAbsolutePath() + "/src/test/resources/chromedriver");
        }
        driver = new ChromeDriver();
        standardWait = new WebDriverWait(driver, 5L);
    }

    private static void closeDriver() {
        driver.close();
    }


    private static void goToTestPage() {
        driver.get(config.getString("amazon.url"));
    }

    private static void locateSearchField() {
        standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(config.getString("amazon.searchField"))));

    }

    private static void enterSearchField(String searchField) {
        WebElement searchFieldInput = standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(config.getString("amazon.searchField"))));
        searchFieldInput.sendKeys(searchField);
        //We can press the button, but we're going to just press the enter key for simplicity's sake.
        searchFieldInput.sendKeys(Keys.RETURN);
    }

    private static void clickSearch() {
        WebElement search = standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(config.getString("amazon.searchButton"))));
        search.click();
    }

    private static void findAlbum() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("System was interrupted while waiting");
        }
        WebElement search = standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(config.getString("amazon.albumName"))));
        search.click();

    }

    private static void assertAudioCdPrice(){
        List<WebElement> searchList = driver.findElements(By.xpath("//*[@class='a-button-text']"));
        String search = null;
        for (int i=0; i<searchList.size();i++){
            if (searchList.get(i).getText().contains("Audio CD")){
                search = searchList.get(i).getText();
                //Gets rid of the Audio CD line
                search = search.replaceAll("[a-z]|[A-Z]| |$|\\n|\\$","");
                break;
            }
        }
        try {
            assertEquals("15.98",search);
            System.out.println("Passes the 'Price' test");
        } catch (AssertionError e) {
            throw e;
        }
    }

    private static void assertStars(){
        WebElement search = standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-hook='rating-out-of-text']")));
//        System.out.println(search.getText());
        try {
            assertEquals("4.5 out of 5 stars", search.getText());
            System.out.println("Passes the 'Stars' test");
        } catch (AssertionError e) {
            throw e;
        }
    }

    private static void assertRecommended(){


        //for some reason, every now and then it'll read this as "Dap-Dippin, even though Miss Sharon Jones is in posinset=1.
        //Dap-dippin loads first when the page gets pulled up. This may be an issue with a slower computer or someone with slow internet.
        //If we have a known good computer that doesn't lag, we can get rid of the sleep timers.

        //It seems like scrolling forces it to show
        ((JavascriptExecutor)driver).executeScript("scroll(0,1000)");


        WebElement search = standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='a-carousel-card' and @aria-posinset='1' and @aria-setsize='30']")));

//        System.out.println(search.getText());


        try {
            assertTrue(search.getText().contains("Miss Sharon Jones"));
            System.out.println("Passes the 'Customer also bought these albums' test");
        } catch (AssertionError e) {
            throw e;
        }
    }

    private static void assertRecommendedWrong(){
        WebElement search = standardWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='a-carousel-card' and @aria-posinset='1' and @aria-setsize='30']")));
        //Checks to see if the first album in the "Customers who bougth this item..." is "The Reistance" by Muse.
        //If you want this to pass, change "assertTrue" to "assertFalse"
        try {
            assertTrue(search.getText().contains("The Resistance"));
        } catch (AssertionError e) {
            throw e;
        }
    }


    @Test
    private static void test() {
        try {
            config = importProperties();
            initDriver();
            goToTestPage();
            locateSearchField();
            enterSearchField("Sharon Jones");
//            clickSearch();
            findAlbum();
            //Letting the page load before we assert
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("System was interrupted while waiting");
            }
            assertAudioCdPrice();
            assertStars();
            assertRecommended();
            assertRecommendedWrong();
            closeDriver();
            System.out.println("Pass!");
        } catch (Exception e) {
            closeDriver();
            System.out.println("Fail!");
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        test();
    }
}
