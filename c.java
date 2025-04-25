import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/scrape")
public class ScrapeServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getParameter("url");
        String[] options = request.getParameterValues("options");
        List<Object> scrapedDataList = new ArrayList<>(); // Use List<Object> to hold different data types

        if (url != null && !url.isEmpty() && options != null && options.length > 0) {
            WebScraper scraper = new WebScraper();
            for (String option : options) {
                switch (option) {
                    case "titleHeadings":
                        scrapedDataList.addAll(scraper.scrapeTitleAndHeadings(url));
                        break;
                    case "links":
                        scrapedDataList.addAll(scraper.scrapeLinks(url));
                        break;
                    case "news":
                        List<WebScraper.NewsArticle> newsData = scraper.scrapeNewsData(url);
                        scrapedDataList.addAll(newsData);
                        break;
                    // Add handling for "images" if you implement image scraping
                }
            }
        }

        // Convert scraped data to JSON
        Gson gson = new Gson();
        String json = gson.toJson(scrapedDataList);

        // Store JSON in session for potential retrieval on page reload
        HttpSession session = request.getSession();
        session.setAttribute("scrapedDataJson", json);

        // Session Tracking
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 1;
        } else {
            visitCount++;
        }
        session.setAttribute("visitCount", visitCount);

        // Send JSON response back to the client-side JavaScript
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(new ResponseData(json, visitCount)));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // For initial page load or fetching visit count and previous data via AJAX
        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;
        }
        String scrapedDataJson = (String) session.getAttribute("scrapedDataJson");

        response.setContentType("application/json");
        String jsonResponse = new Gson().toJson(new ResponseData(scrapedDataJson, visitCount));
        response.getWriter().write(jsonResponse);
    }

    private static class ResponseData {
        private String scrapedData;
        private Integer visitCount;

        public ResponseData(String scrapedData, Integer visitCount) {
            this.scrapedData = scrapedData;
            this.visitCount = visitCount;
        }
    }
}