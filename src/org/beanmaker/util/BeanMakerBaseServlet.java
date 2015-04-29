package org.beanmaker.util;

import org.dbbeans.util.Pair;
import org.dbbeans.util.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.Enumeration;
import java.util.List;

public abstract class BeanMakerBaseServlet extends HttpServlet {

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    abstract protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException;

    protected String getRootPath() {
        return getServletContext().getRealPath("/");
    }

    protected String getErrorsInJson(final List<ErrorMessage> errorMessages) {
        final StringBuilder buf = new StringBuilder();

        buf.append("{ \"ok\": false, \"errors\": [ ");

        for (ErrorMessage errorMessage: errorMessages)
            buf.append(errorMessage.toJson()).append(", ");

        buf.delete(buf.length() - 2, buf.length());
        buf.append(" ] }");

        return buf.toString();
    }

    protected long getBeanId(final HttpServletRequest request, final String parameterName) {
        return Strings.getLongVal(request.getParameter(parameterName));
    }

    protected String getJsonSimpleStatus(final String status) {
        return "{ \"status\": \"" + status + "\" }";
    }

    protected String getJsonOk() {
        return getJsonSimpleStatus("ok");
    }

    protected String getStartJsonErrors() {
        return "{ \"status\": \"errors\", ";
    }

    protected Pair<String, Long> getSubmittedFormAndId(final HttpServletRequest request) throws ServletException {
        String form = null;
        long id = 0;

        int count = 0;
        final Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            final String param = params.nextElement();
            if (param.startsWith("submitted")) {
                ++count;
                form = param.substring(9, param.length());
                id = Strings.getLongVal(request.getParameter(param));
            }
        }

        if (count > 1)
            throw new ServletException("More than one submittedXXX parameter.");

        return new Pair<String, Long>(form, id);
    }

    protected String processBean(final HttpServletRequest request, final DbBeanHTMLViewInterface htmlView) {
        htmlView.setAllFields(request);

        if (htmlView.isDataOK()) {
            htmlView.updateDB();
            return getJsonOk();
        }

        return getStartJsonErrors() + ErrorMessage.toJson(htmlView.getErrorMessages()) + " }";
    }
}
