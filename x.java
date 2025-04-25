import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {

    public static List<String> scrapeTitleAndHeadings(String url) {
        List<String> results = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            results.add("Title: " + title);
            Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
            for (Element heading : headings) {
                results.add("Heading: " + heading.text());
            }
        } catch (IOException e) {
            results.add("Error scraping URL: " + e.getMessage());
        }
        return results;
    }

    public static List<String> scrapeLinks(String url) {
        List<String> links = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements linkElements = doc.select("a[href]");
            for (Element link : linkElements) {
                links.add(link.attr("abs:href")); // Get absolute URLs
            }
        } catch (IOException e) {
            links.add("Error scraping URL: " + e.getMessage());
        }
        return links;
    }

    // Modification for news headlines, dates, and authors (example for BBC)
    public static List<NewsArticle> scrapeNewsData(String url) {
        List<NewsArticle> newsArticles = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();

            // Example selector for BBC News headlines (adjust as needed)
            Elements headlines = doc.select(".media__content h3 a");
            // Example selector for publication dates (adjust as needed)
            Elements dates = doc.select(".date--v2");
            // Example selector for author names (adjust as needed - BBC often doesn't show explicit authors)
            Elements authors = doc.select(".byline"); // This might need refinement

            int minSize = Math.min(headlines.size(), Math.min(dates.size(), authors.size()));

            for (int i = 0; i < minSize; i++) {
                String headline = headlines.get(i).text();
                String date = dates.size() > i ? dates.get(i).text() : "N/A";
                String author = authors.size() > i ? authors.get(i).text() : "N/A";
                newsArticles.add(new NewsArticle(headline, date, author));
            }

        } catch (IOException e) {
            System.err.println("Error scraping news data from " + url + ": " + e.getMessage());
        }
        return newsArticles;
    }

    public static class NewsArticle {
        private String headline;
        private String publicationDate;
        private String author;

        public NewsArticle(String headline, String publicationDate, String author) {
            this.headline = headline;
            this.publicationDate = publicationDate;
            this.author = author;
        }

        // Getters for JSON serialization
        public String getHeadline() {
            return headline;
        }

        public String getPublicationDate() {
            return publicationDate;
        }

        public String getAuthor() {
            return author;
        }
    }

    public static void main(String[] args) {
        String url = "https://bbc.com";

        System.out.println("--- Title and Headings ---");
        scrapeTitleAndHeadings(url).forEach(System.out::println);

        System.out.println("\n--- Links ---");
        scrapeLinks(url).forEach(System.out::println);

        System.out.println("\n--- News Data ---");
        scrapeNewsData(url).forEach(article ->
                System.out.println("Headline: " + article.getHeadline() +
                                   ", Date: " + article.getPublicationDate() +
                                   ", Author: " + article.getAuthor())
        );
    }
}