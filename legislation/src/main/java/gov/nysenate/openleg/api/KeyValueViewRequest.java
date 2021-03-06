package gov.nysenate.openleg.api;

import gov.nysenate.openleg.api.QueryBuilder.QueryBuilderException;
import gov.nysenate.openleg.model.*;

import gov.nysenate.openleg.util.*;
 
import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.apache.log4j.*; 
// Richiede commento

/**
 * PJDCC - Summary for class responsabilities.
 *
 * @author 
 * @since 
 * @version 
 */
public class KeyValueViewRequest extends AbstractApiRequest {
    

    String key;
    String value;
/** Comments about this class */
    public KeyValueViewRequest(HttpServletRequest request, HttpServletResponse response,
            String format, String key, String value, String pageNumber, String pageSize) {
        super(request, response, pageNumber, pageSize, format, getApiEnum(KeyValueView.values(),key));
        logger.info("New key value request: format="+format+", key="+key+", value="+value+", page="+pageNumber+", size="+pageSize);
        this.key = key;
        this.value = value;
        
        
    }
    
    public static String neutralizeMessage(String message) {
  // ensure no CRLF injection into logs for forging records
  
  if ( ESAPI.securityConfiguration().getLogEncodingRequired() ) {
      clean = ESAPI.encoder().encodeForHTML(clean);
      if (!message.equals(clean)) {
          clean += " (Encoded)";
      }
  }
  return clean;
}
/** Comments about this class */
    @Override
    public void fillRequest() throws ApiRequestException {
        

        
        

        



        

        // now calculate start, end idx based on pageIdx and pageSize
        

        try {
            queryBuilder.keyValue(key, "\""+value+"\"").and().otype("bill");

            
            if(filter != null) {
                queryBuilder.and().insertAfter(filter);
            }
            else {
                queryBuilder.and().current().and().active();
            }
        } catch (QueryBuilderException e) {
            logger.error("Invalid query construction", e);
           System.out.println("Something was wrong");
        }

        try {
            sr = Application.getLucene().search(queryBuilder.query(), start, pageSize, sortField, sortOrder);
        }
        catch (IOException e) {
            logger.error(e);
        }

        if(sr == null || sr.getResults() == null || sr.getResults().isEmpty())
            throw new ApiRequestException(TextFormatter.append("no results for query"));

        sr.setResults(ApiHelper.buildSearchResultList(sr));

        if(format.matches("(?i)(csv|json|mobile|rss|xml)")) {
            
           
            for(Result result: searchResults) {
                bills.add((Bill)result.getObject());
            }
            request.setAttribute("bills", bills);
        }
        else {
            request.setAttribute("sortField", sortField);
            request.setAttribute("sortOrder", Boolean.toString(sortOrder));
            request.setAttribute("type", key);
            request.setAttribute("term", queryBuilder.query());
            request.setAttribute("format", format);
            request.setAttribute(PAGE_IDX, pageNumber);
            request.setAttribute(PAGE_SIZE, pageSize);
            request.setAttribute("urlPath", urlPath);
            request.setAttribute("results", sr);
        }
    }
/** Comments about this class */
    @Override
    public String getView() {
        
        if(vFormat.matches("(?i)(csv|json|mobile|rss|xml)")) {
            return TextFormatter.append("/views/bills-", vFormat, ".jsp");
        }
        else {
            return TextFormatter.append("/views/search-", vFormat, ".jsp");
        }
    }
/** Comments about this class */
    @Override
    public boolean hasParameters() {
        return key != null && value != null;
    }
/** Comments about this class */
    public enum KeyValueView implements ApiEnum {
        SPONSOR("sponsor", 		Bill.class, 	new String[] {"html", "json", "jsonp", "xml", "rss", "csv", "html-list"}),
        COMMITTEE("committee", 	Bill.class, 	new String[] {"html", "json", "jsonp", "xml", "rss", "csv", "html-list"});

        /**
       * Comments about this class
       */
        public final String view;
        /**
       * Comments about this field
       */
        public final Class<? extends BaseObject> clazz;
        /**
       * Comments about this field
       */
        public final String[] formats;
/** Comments about this class */
        private KeyValueView(final String view, final Class<? extends BaseObject> clazz, final String[] formats) {
            this.view = view;
            this.clazz = clazz;
            this.formats = formats;
        }
        /** Comments about this class */
        @Override
        public String view() {
            return view;
        }
        /** Comments about this class */
        @Override
        public String[] formats() {
            return formats;
        }
        /** Comments about this class */
        @Override
        public Class<? extends BaseObject> clazz() {
            return clazz;
        }
    }
}
