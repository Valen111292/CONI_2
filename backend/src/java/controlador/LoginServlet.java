package controlador;
 
import dao.UsuarioDAO;
import modelo.Usuario;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;

// El @WebServlet no se cambia.
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    
    // --- NUEVO MÉTODO: Añade las cabeceras CORS ---
    private void addCorsHeaders(HttpServletResponse response) {
        // Esto fuerza a que Render envíe el Access-Control-Allow-Origin al Frontend
        response.setHeader("Access-Control-Allow-Origin", "https://coni-frontend.onrender.com");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600"); // Cachea la respuesta del pre-vuelo por 1 hora
    }

    // --- NUEVO MÉTODO: Maneja la petición OPTIONS (Pre-flight) ---
    // Este es el FIX CRÍTICO: El navegador envía OPTIONS antes del POST.
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Aplicamos las cabeceras CORS
        addCorsHeaders(response);
        // Indicamos que el pre-vuelo fue exitoso
        response.setStatus(HttpServletResponse.SC_OK);
    }
 
    @Override 
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. APLICAR CABECERAS CORS (primero en doPost)
        addCorsHeaders(response); 
 
        // Configuramos la respuesta para que sea en formato JSON y use la codificación UTF-8.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
 
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
 
        try {
            // Leemos el cuerpo de la solicitud (Request Body)
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            JSONObject jsonRequest = new JSONObject(jsonString);
 
            // --- LÓGICA DE NEGOCIO ---
            String username = jsonRequest.getString("username");
            String password = jsonRequest.getString("password");
 
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.validar(username, password);
 
            if (usuario != null) {
                // Login exitoso
                HttpSession session = request.getSession(true);
                session.setAttribute("idUsuario", usuario.getId());
                session.setAttribute("rolAutenticacion", usuario.getRol());
                session.setAttribute("cargoEmpleado", usuario.getCargoEmpleado());
                session.setAttribute("username", usuario.getUsername());
 
                // Preparamos la respuesta JSON para el frontend
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Login exitoso");
 
                JSONObject userData = new JSONObject();
                userData.put("id", usuario.getId());
                userData.put("rolAutenticacion", usuario.getRol());
                userData.put("cargoEmpleado", usuario.getCargoEmpleado());
                userData.put("username", usuario.getUsername());
                jsonResponse.put("user", userData);
 
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // Credenciales incorrectas
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Usuario, contraseña o rol incorrectos.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
 
        } catch (org.json.JSONException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Formato de solicitud JSON inválido o campos faltantes.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
 
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error interno del servidor: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
 
        } finally {
            // Finalmente, enviamos la respuesta JSON al frontend y cerramos el flujo de salida.
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
}