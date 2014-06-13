package appspot;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")

public class ConnectServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String id = req.getParameter("id");
		String pass = req.getParameter("pwd");
		
		UserService userService = UserServiceFactory.getUserService();
		String url = req.getRequestURI();
		
		RequestDispatcher dispatcher = null;
		
		
		// TODO : Brancher par rapport à la connexion
		
		if(req.getUserPrincipal() != null) {
			System.out.println(userService.createLogoutURL(url));
			resp.getWriter().append("<a href='" + userService.createLogoutURL(url) + ">Test</a>");
			//dispatcher = req.getRequestDispatcher("/connect.html");
		}
		else {
			System.out.println(userService.createLoginURL(url));
			resp.getWriter().println("<html><head></head><body><a href='" + userService.createLoginURL(url) + "'>Test</a></body></html>");
			//dispatcher = req.getRequestDispatcher("/disconnect.html");
		}
		
		//dispatcher.forward(req, resp);
	}
}
