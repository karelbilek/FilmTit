package cz.filmtit.userspace.servlets;


public class SubtitleDownloadServlet extends HttpServlet {

    public SubtitleDownloadServlet(FilmTitBackendServer backend) {
    
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("Hello World");
    }

}
