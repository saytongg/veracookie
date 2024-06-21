package com.saytongg.veracookie.service;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.saytongg.veracookie.entity.CookieBannerRecord;
import com.saytongg.veracookie.repository.CookieBannerRecordRepository;

@Service
public final class CookieBannerService {
    @Value( "${classifier.url}" )
    private String classifier_url;

    @Value( "${driver.path}" )
    private String driver_path;

    @Autowired
	private CookieBannerRecordRepository cache;

    private static final Logger logger = LoggerFactory.getLogger(CookieBannerService.class);

    private record CookieBanner(String image, String text) {}

    private record CookieBannerRating(String textRating, String imageRating) {}

    public boolean validateLink(String link){
        final Pattern pattern = Pattern.compile("^(http(s)?://)?[\\w][\\w\\.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.]+$");
        final Matcher m = pattern.matcher(link);
        return m.matches();
    }

    public CookieBannerRecord getRatings(String link) throws Exception{
        final String baseLink = getBaseLink(link);

        CookieBannerRecord record = getCachedRecord(baseLink);
        if(record != null){
            logger.info(String.format("Result for link %s is obtained from cache.", link));
            return record;
        }
        
        // Link is not in cache
        record = new CookieBannerRecord();
        record.setLink(baseLink);

        final WebDriver driver = getDriver();
        logger.info(String.format("Driver object created for %s.", link));

        try{
            logger.info(String.format("Finding the cookie banner for %s.", link));
            WebElement cookieBanner = getCookieBanner(driver, baseLink);

            if(cookieBanner == null){
                logger.info(String.format("No cookie banner found on %s.", link));
                saveToCache(record);
                return record;
            }

            logger.info(String.format("A cookie banner was found on %s.", link));
            final String text = preprocessText(cookieBanner.getText());
            final String image = adjustImage(driver, cookieBanner);
            logger.info(String.format("Done resizing the cookie banner for %s.", link));

            // Send to classifiers
            final CookieBannerRating rating = rateCookieBanner(new CookieBanner(image, text));
            logger.info(String.format("Classification ratings for %s are available.", link));

            // Create a record for the cookie banner found
            record.setImage(image);
            record.setImageRating(rating.imageRating());
            record.setTextRating(rating.textRating());

            saveToCache(record);

            return record;
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            driver.quit();
        }
    }



    /*
     * Returns the domain name of a URL.
     * For example, transform "www.google.com/search?q=nice" to "google.com"
     */
    private String getBaseLink(String link) {
        final Pattern pattern = Pattern.compile("^(https?://)?[^/]*/?");
        final Matcher match = pattern.matcher(link);
        match.find();
        return match.group().replaceAll("^(https?://)?(www\\.)?", "").replace("/", "");
    }

    private CookieBannerRecord getCachedRecord(String link){
        final List<CookieBannerRecord> records = cache.findByLink(link);
        return records.isEmpty() ? null : records.get(0);
    }

    private WebDriver getDriver() throws Exception{
        // Set path to web driver
        System.setProperty("webdriver.gecko.driver", driver_path);

        // Specify options for Firefox
        final FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-application-cache");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");

        options.addPreference("permissions.default.image", 2);
        options.addPreference("media.autoplay.default", 5);
        options.addPreference("media.autoplay.enabled.user-gestures-needed", false);
        options.addPreference("media.autoplay.allow-extension-background-pages", false);
        options.addPreference("media.autoplay.block-event.enabled", true);
        options.addPreference("media.autoplay.blocking_policy", 2);
        options.addPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);

        // Force to use English
        options.addPreference("intl.accept_languages","en-US, en");

        // Set timeouts
        final Duration pageTimeout = Duration.ofMinutes(2);
        final Duration implicitWaitTimeout = Duration.ofSeconds(10);
        options.setPageLoadTimeout(pageTimeout);
        options.setImplicitWaitTimeout(implicitWaitTimeout);

        // Create Firefox driver object
        final WebDriver driver = new FirefoxDriver(options);
        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();

        return driver;
    }

    private WebElement getCookieBanner(WebDriver driver, String link) throws Exception{
        // Visit link
        final String browseLink = new StringBuffer(link).insert(0, "https://").toString();
        driver.get(browseLink);

        Thread.sleep(3000);

        // Find cookie banner
        final By by = By.cssSelector("div:not(div[style*=\"z-index: 0\"], div[style*=\"z-index: auto\"], div[style*=\"z-index:0\"])");
        final WebElement cookieBanner = driver.findElements(by)
                .stream()
                .filter(x -> {
                    try{
                        return !x.getCssValue("z-index").equals("auto");
                    }
                    catch(Exception e){
                        return false;
                    }
                })
                .filter(x -> {
                    try {
                        final String text = x.getText().toLowerCase();
                        return text.contains("cookies") || text.contains("consent") || text.contains("trackers");
                    } 
                    catch (Exception e) {
                        return false;
                    }
                })
                .reduce((x, y) -> x)
                .orElse(null);
        
        return cookieBanner;
    }

    private String preprocessText(String text){
        return text.replaceAll("\n", " ").replaceAll("\"", "\"\"");
    }

    /*
     * Minimize the difference between height and width of the banner (nearly
     * perfect square)
     */
    private String adjustImage(WebDriver driver, WebElement banner) throws Exception{
        final int maxWindowHeight = driver.manage().window().getSize().getHeight();
        final int minWindowWidth = 450;

        // Get current height and width of banner
        Dimension bannerDimension = banner.getSize();
        int bannerHeight = bannerDimension.getHeight();
        int bannerWidth = bannerDimension.getWidth();

        int perfectWindowWidth = 0;
        if (bannerWidth / bannerHeight >= 2) {
            perfectWindowWidth = minWindowWidth;
        } 
        else {
            int minDifference = Integer.MAX_VALUE;
            int currentWindowWidth = driver.manage().window().getSize().getWidth();

            while (currentWindowWidth > minWindowWidth) {
                // Get current difference of banner's width and height
                final int currentDifference = Math.abs(bannerHeight - bannerWidth);
                if (currentDifference <= minDifference) {
                    minDifference = currentDifference;
                    perfectWindowWidth = currentWindowWidth;
                }

                // Reduce window width by 5
                final int stepSize = 5;
                currentWindowWidth = currentWindowWidth - stepSize;

                // Update window dimensions
                final Dimension newWindowSize = new Dimension(currentWindowWidth, maxWindowHeight);
                driver.manage().window().setSize(newWindowSize);

                // Update height and width of banner
                bannerDimension = banner.getSize();
                bannerHeight = bannerDimension.getHeight();
                bannerWidth = bannerDimension.getWidth();
            }
        }

        // Set window to ideal width
        driver.manage().window().setSize(new Dimension(perfectWindowWidth, maxWindowHeight));

        // Save banner as string
        return banner.getScreenshotAs(OutputType.BASE64);
    }

    // Forward the extracted cookie banner image and text to the classifiers via an API call.
    private CookieBannerRating rateCookieBanner(CookieBanner banner) throws Exception{
        final RestTemplate restTemplate = new RestTemplate();
        final HttpEntity<CookieBanner> request = new HttpEntity<>(banner);

        try{
            final ResponseEntity<CookieBannerRating> response = restTemplate.exchange(classifier_url, HttpMethod.POST, request,CookieBannerRating.class);
            return response.getBody();
        }
        catch(Exception e){
            throw e;
        }
    }

    private void saveToCache(CookieBannerRecord record){
        cache.save(record);
    }
}
