package ca.eqv.maven.plugins.tsc.demo;

import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/greet")
public class GreetingServlet extends HttpServlet {

	@Override
	@SuppressWarnings("unchecked")
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final String name = req.getParameter("name");
		final String greeting = req.getParameter("greeting");
		final String text = "In that case: " + greeting + ", " + name + "!";

		resp.setContentType("application/json");
		final JSONObject result = new JSONObject();
		result.put("text", text);
		result.writeJSONString(resp.getWriter());
	}

}
