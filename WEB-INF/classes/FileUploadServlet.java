import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import java.sql.*;

@WebServlet("/upload")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		InputStream filecontent = null;
		PrintWriter writer = response.getWriter();
		try {
			// This class represents a part or form item that was received
			// within a multipart/form-data POST request.
			Part part = request.getPart("file"); // interface in
													// javax.servlet.http
			String fileName = getFileName(part);// user method
			String type = part.getContentType();
			final int size = (int) part.getSize();

			filecontent = part.getInputStream(); // Gets the content of this
													// part as an InputStream

			saveIntoDataBase(fileName, type, size, filecontent, writer);

			if (filecontent != null) {
				filecontent.close();
			}
			if (writer != null) {
				writer.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			writer.println("ERROR: " + e.getMessage());
		}
	}

	private void saveIntoDataBase(String fileName, String type, int size,
			InputStream filecontent, PrintWriter writer) throws SQLException {
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/fileupload", "root", "");
		String sql = "insert into uploadfile(fname, type, size, filedata) values"
				+ "(?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, fileName);
		pstmt.setString(2, type);
		pstmt.setLong(3, size);

		/*
		 * this method works with older version of mysql connector
		 * setBinaryStream(int, InputStream) && setBinaryStream(int,
		 * InputStream, long) does not work here coz abstract
		 * setBinaryStream(int, InputStream, int only works here)
		 */
		pstmt.setBinaryStream(4, filecontent, size); // only this works with
														// older version
		int id = pstmt.executeUpdate();
		if (id > 0)
			writer.println("New file " + fileName + " uploaded successfully");
	}

	private String getFileName(final Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}
}
