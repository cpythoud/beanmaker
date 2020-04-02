package org.beanmaker.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class OperationsBaseServlet extends BeanMakerBaseServlet {

    @Override
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter out = response.getWriter();

        switch (getOperationIndex(request)) {
            case 1:
                response.setContentType("text/html; charset=UTF-8");
                out.println(getForm(request));
                break;
            case 2:
                response.setContentType("text/json; charset=UTF-8");
                out.println(submitForm(request));
                break;
            case 3:
                response.setContentType("text/json; charset=UTF-8");
                out.println(deleteBean(request));
                break;
            default:
                throw new AssertionError("Unidentified operation index: " + getOperationIndex(request));
        }
    }

    protected String getForm(final HttpServletRequest request) {
        return getHTMLView(getBeanId(request), getLanguage(request.getSession())).getHtmlForm();
    }

    protected abstract DbBeanHTMLViewInterface getHTMLView(final long id, final DbBeanLanguage language);

    protected long getBeanId(final HttpServletRequest request) {
        return getBeanId(request, "id");
    }

    protected abstract DbBeanLanguage getLanguage(final HttpSession session);

    protected String submitForm(HttpServletRequest request) {
        return processBean(
                new HttpRequestParameters(request),
                getHTMLView(getBeanId(request), getLanguage(request.getSession()))
        );
    }

    protected String processBean(final HttpRequestParameters parameters, final DbBeanHTMLViewInterface htmlView) {
        htmlView.setAllFields(parameters);

        if (htmlView.isDataOK()) {
            htmlView.updateDB();
            return getJsonOk();
        }

        return getStartJsonErrors() + ErrorMessage.toJson(htmlView.getErrorMessages()) + " }";
    }

    protected String deleteBean(final HttpServletRequest request) {
        return deleteBean(getInstance(getBeanId(request)));
    }

    protected abstract DbBeanInterface getInstance(final long id);
}
