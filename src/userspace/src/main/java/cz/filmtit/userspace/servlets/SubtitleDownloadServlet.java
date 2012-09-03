/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.userspace.servlets;

import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import cz.filmtit.share.ChunkStringGenerator;
import cz.filmtit.userspace.USDocument;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;

public class SubtitleDownloadServlet extends HttpServlet {

    FilmTitBackendServer backend;
    
    public SubtitleDownloadServlet(FilmTitBackendServer backend) {
        this.backend = backend;
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // reads the parameters from the http request
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
            writeError(response, "no such way as " + wayString);
            return;
        }

        try {
            // generate thi file name
            USDocument document = backend.getActiveDocument(sessionId, docIdLong);

            String fileName = Normalizer.normalize(document.getTitle(), Normalizer.Form.NFD); // split chars and accents
            fileName = fileName.replaceAll("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+", ""); // removes accents
            fileName = fileName.replaceAll("[^\\x00-\\x7f]", ""); // removes non ASCII characters
            fileName = fileName.replaceAll("[|\\?\\*\\\\<>+/\\[\\]]+", ""); // removes not allowed characters
            fileName = fileName.replaceAll(" ", "_"); // replace spaces by underscores

            // solve the language code
            String languageToFileName = null;
            String language1 = ConfigurationSingleton.getConf().l1().getCode();
            String language2 = ConfigurationSingleton.getConf().l2().getCode();

            if (way == ChunkStringGenerator.SOURCE_SIDE) {
                languageToFileName = document.getLanguage().getCode();
            }
            else {
                if (document.getLanguage().getCode().equals(language1)) {
                    languageToFileName = language2;
                }
                else {
                    languageToFileName = language1;
                }
            }

            fileName += "." + languageToFileName + "." + typeString; // adds the ending

            // generate the actual content of the file
            String fileContent = backend.getSourceSubtitles(sessionId, docIdLong, 25L, type, way);
            response.setContentType(responseType);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

           ServletOutputStream out = response.getOutputStream();
           out.write(fileContent.getBytes("UTF-8"));
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
