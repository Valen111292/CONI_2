package filtro;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Intercepta todas las URLs
// DESHABILITADO: Comentar la anotación WebFilter para evitar conflictos.
// @WebFilter("/*") 
public class CORSFilter implements Filter {

    // Origen permitido. Se usa la URL del frontend. 
    // Si sigue fallando, prueba cambiar esto a "*" (comentando la línea original)
    // Pero por ahora, mantenemos la URL específica para seguridad.
    private final String allowedOrigin = "https://coni-frontend.onrender.com"; 

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Añadir headers CORS comunes a TODAS las respuestas
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", 
                           "Origin, Content-Type, Accept, Authorization");
        response.setHeader("Access-Control-Allow-Methods", 
                           "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Vary", "Origin");
        response.setHeader("Access-Control-Max-Age", "3600"); 

        // ----------------------------------------------------
        // FIX CLAVE: Responder directamente OPTIONS (preflight)
        // ----------------------------------------------------
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // Establece el estado 200 y finaliza la respuesta de inmediato.
            response.setStatus(HttpServletResponse.SC_OK);
            // No llamar a chain.doFilter(req, res);
            return; 
        }

        // Para los demás métodos (GET, POST, etc.), continuar con la cadena de filtros/servlets
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}