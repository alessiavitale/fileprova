package gov.nysenate.openleg.api.servlets;

import gov.nysenate.openleg.util.RequestUtils;
import gov.nysenate.openleg.util.RequestUtils.FORMAT;
import gov.nysenate.openleg.util.SessionYear;
import gov.nysenate.services.model.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;

import org.apache.commons.io.F*;
import org.apache.log4j.*;
import org.codehaus.jackson.map.*;  
// Richiede commento

/**
 * PJDCC - Summary for class responsabilities.
 *
 * @author 
 * @since 
 * @version 
 */
@SuppressWarnings("serial")
public class CommitteesServlet extends HttpServlet
{
   

    private static Pattern pathPattern = Pattern.compile("/([0-9]{4}).*");
/** Comments about this class */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        int sessionYear = SessionYear.getSessionYear();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            Matcher pathMatcher = pathPattern.matcher(pathInfo);
            if (pathMatcher.find()) {
                sessionYear = SessionYear.getSessionYear(pathMatcher.group(1));
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        File committeesBase = new File(java.net.URLDecoder.decode(SenatorsServlet.class.getClassLoader().getResource("data/committees/").getPath()));
        File committeesDir = new File(committeesBase, String.valueOf(sessionYear));

        if (!committeesDir.exists()) committeesDir.mkdirs();

        ArrayList<Committee> committees = new ArrayList<Committee>();
        for (File committeeFile : FileUtils.listFiles(committeesDir, new String[]{"json"}, false)) {
            Committee committee = mapper.readValue(committeeFile, Committee.class);
            ArrayList<Member> members = committee.getMembers();
            Collections.sort(members, new Comparator<Member>() {
                public int compare(Member a, Member b) {
                    return a.getShortName().compareToIgnoreCase(b.getShortName());
                }
            });
            committee.setMembers(members);
            committees.add(committee);
        }

        Collections.sort(committees, new Comparator<Committee>() {
            public int compare(Committee a, Committee b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        FORMAT format = RequestUtils.getFormat(request, FORMAT.HTML);

        switch (format) {
        case JSON:
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            mapper.writeValue(response.getOutputStream(), committees);
            break;
        
        case HTML:
            request.setAttribute("committees", committees);
            request.setAttribute("sessionStart", sessionYear);
            request.setAttribute("sessionEnd", sessionYear+1);
            request.getRequestDispatcher("/views/committees.jsp").forward(request, response);
            break;
        
        case XML:
            // There are no plans to add XML support at this time
            break;
        default:
        }
    }
/** Comments about this class */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
/** Comments about this class */
    @Override
    public void init() throws ServletException
    {
        super.init();
    }
}
