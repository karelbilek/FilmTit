package cz.filmtit.userspace.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.*;
import cz.filmtit.userspace.*;

public class SubtitleDownloadServlet extends HttpServlet {

    FilmTitBackendServer backend;
    
    public SubtitleDownloadServlet(FilmTitBackendServer backend) {
        this.backend = backend;
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        
        String docId = request.getParameter("docId");
        String sessionId = request.getParameter("sessionId");
        String typeString = request.getParameter("type");
        String wayString = request.getParameter("way");
        
        if (docId==null || sessionId==null||typeString==null||wayString==null) {
            writeError(response, "no parameter");
            return;
        }
        
        Long docIdLong;
        try {
            docIdLong = new Long(docId);
        } catch (NumberFormatException e) {
            writeError(response, "wrong documentId");
            return;
        }

        if (!backend.canReadDocument(sessionId, docIdLong)) {
            writeError(response, "no rights to read document");
            return;
        }

        TimedChunk.FileType type;

        String responseType;
        if (typeString.equals("srt")) {
            type=TimedChunk.FileType.SRT;
            responseType = "application/x-subrip";
        } else if (typeString.equals("sub")) {
            type=TimedChunk.FileType.SUB;
            responseType = "text/plain";
        } else if (typeString.equals("txt")) {
            type=TimedChunk.FileType.TXT;
            responseType = "text/plain";
        } else {
            writeError(response, "wrong format "+typeString);
            return;
        }

        ChunkStringGenerator.ResultToChunkConverter way;
        if (wayString.equals("source")) {
            way = ChunkStringGenerator.SOURCE_SIDE;
        } else if (wayString.equals("target")) {
            way = ChunkStringGenerator.TARGET_SIDE;
        } else if (wayString.equals("targetthrowback")) {
            way = ChunkStringGenerator.TARGET_SIDE_WITH_THROWBACK;
        } else {
            writeError(response, "no such way as "+wayString);
            return;
        }

        try {  
            String s = backend.getSourceSubtitles(sessionId, docIdLong, 25L, type, way);
            response.setContentType(responseType);
            response.setContentLength(s.length());
            ServletOutputStream output = response.getOutputStream();
            output.print(s);
            output.flush();
            output.close();
        } catch (InvalidSessionIdException e) {
            writeError(response, "Invalid session id exception");
            return;
        } catch (InvalidDocumentIdException e) {
            writeError(response, "Invalid document ID exception");
            return;
        } catch (IOException e) {
            writeError(response, "IOexception");
            return;
        }
   }

    public void writeError(HttpServletResponse response, String error) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("There was some error along the way :(" + error);
     }

}
