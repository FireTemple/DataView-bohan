/**
 * This servlet is used for setting up user access.
 * @author Aravind Mohan.
 *
 */

package usermgmt;
import dataview.models.User;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Servlet implementation class UserLogin
 */

public class UserLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserLoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		
		String emailId = (String)request.getParameter("txtEmailId");
     	String passwrd = (String)request.getParameter("txtPasswd");
		System.out.println(emailId);
		System.out.println(passwrd);

     	Encrypt encrypt = null;
		try {
			encrypt = new Encrypt();
		} catch (Exception e) {
			e.printStackTrace();
		}

		passwrd = encrypt.encrypt(passwrd);
     	String tableLocation = getServletContext().getRealPath(request.getServletPath()).replace("UserLogin", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";
		System.out.println("table location is : "+tableLocation);

     	try{
     		//WebbenchUtility.initializeWebbenchConfig();
     		User user = new User(emailId,passwrd,tableLocation);
     		if(user.login(user.email, user.password))
     		{
     			//succesful logic
				/* 允许跨域的主机地址 */
				response.setHeader("Access-Control-Allow-Origin", "*");
				/* 允许跨域的请求方法GET, POST, HEAD 等 */
				response.setHeader("Access-Control-Allow-Methods", "*");
				/* 重新预检验跨域的缓存时间 (s) */
				response.setHeader("Access-Control-Max-Age", "3600");
				/* 允许跨域的请求头 */
				response.setHeader("Access-Control-Allow-Headers", "*");
				/* 是否携带cookie */
				response.setHeader("Access-Control-Allow-Credentials", "true");

				request.setAttribute("userId",emailId);
     			HttpSession session = request.getSession(true);
     			session.setAttribute("UserID", emailId);

//     			getServletConfig().getServletContext().getRequestDispatcher(
//     			        "/workflow.jsp").forward(request,response);


				// TODO ready to go
				response.setCharacterEncoding("UTF-8");
				response.setContentType("application/json; charset=utf-8");
				String jsonStr = "{\"username\":\""+emailId+"\",\"code\":\"0\"}";

				PrintWriter out = null;

				try {
					out = response.getWriter();
					out.write(jsonStr);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

					if (out != null) {
						out.close();
					}

				}


     		}
     		else
     		{
     			//failure logic
//     			request.setAttribute("statusMsg","Incorrect credentials, please try again.");
//     			getServletConfig().getServletContext().getRequestDispatcher(
//     			        "/login.jsp").forward(request,response);
				response.setCharacterEncoding("UTF-8");
				response.setContentType("application/json; charset=utf-8");
				String jsonStr = "{\"msg\":\"information incorrect\",\"code\":\"100001\"}";
				PrintWriter out = null;
				try {
					out = response.getWriter();
					out.write(jsonStr);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (out != null) {
						out.close();
					}
				}
     		}
     	}
     	catch(Exception e)
     	{
     		System.out.println(e.toString());
     	}

	}

}
