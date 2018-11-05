import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@WebServlet(urlPatterns = "/*", loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {


    private Map<String, Method> uriMappings = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Getting request for " + req.getRequestURI());
        var uri = req.getRequestURI();
        if (!this.uriMappings.containsKey(uri))
            resp.sendError(404, "no mapping found for request uri "+uri);
        else {
            Method maMethodEvoq = this.uriMappings.get(req.getRequestURI());

            Class cl = maMethodEvoq.getDeclaringClass();

            try {
                Object ob = cl.newInstance();
                Object res =  maMethodEvoq.invoke(ob, null);
                PrintWriter writer = resp.getWriter();
                writer.print(res.toString());
            } catch (RuntimeException | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.registerController(HelloController.class);
    }

    protected void registerController(Class controllerClass) throws IllegalArgumentException {
        System.out.println("Analysing class " + controllerClass.getName());

        if (controllerClass.getAnnotation(Controller.class) == null)
            throw new IllegalArgumentException("Class is not Controler class");


        Method[] mesMethod = controllerClass.getDeclaredMethods();
        if (mesMethod.length == 0)
            throw new IllegalArgumentException("No method declared");

        for (Method method : mesMethod) {
            if (!method.getReturnType().equals(Void.TYPE)) {
                if (method.getDeclaredAnnotations().length != 0)
                    registerMethod(controllerClass, method);
            }

        }
    }

    protected void registerMethod(Object controller, Method method) throws IllegalArgumentException {
        System.out.println("Registering method " + method.getName());
        var annotation = method.getAnnotation(RequestMapping.class);
        if (annotation != null)
            uriMappings.put(annotation.uri(), method);

    }

    protected Map<String, Method> getMappings() {
        return this.uriMappings;
    }

    protected Method getMappingForUri(String uri) {
        return this.uriMappings.get(uri);
    }
}


