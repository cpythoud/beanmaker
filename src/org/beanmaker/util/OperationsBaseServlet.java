package org.beanmaker.util;

import org.dbbeans.util.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

public abstract class OperationsBaseServlet extends BeanMakerBaseServlet {

    @Override
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        switch (getOperation(request)) {
            case GET_FORM:
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println(getForm(request));
                break;
            case SUBMIT_FORM:
                response.setContentType("text/json; charset=UTF-8");
                response.getWriter().println(submitForm(request));
                break;
            case DELETE_BEAN:
                response.setContentType("text/json; charset=UTF-8");
                response.getWriter().println(deleteBean(request));
                break;
            case CHANGE_ORDER:
                response.setContentType("text/json; charset=UTF-8");
                response.getWriter().println(changeOrder(request));
                break;
            default:
                throw new AssertionError("Unidentified operation: " + getOperation(request));
        }
    }

    protected String getForm(final HttpServletRequest request) throws ServletException {
        return getFormPrefix(request) +
                getHTMLView(getBeanId(request, "id"), request).getHtmlForm() +
                getFormSuffix(request);
    }

    protected String getFormPrefix(final HttpServletRequest request) {
        return "";
    }

    private String getFormSuffix(final HttpServletRequest request) {
        return "";
    }

    protected abstract DbBeanHTMLViewInterface getHTMLView(final long id, final HttpServletRequest request) throws ServletException;

    protected abstract long getSubmitBeanId(final HttpServletRequest request);

    protected abstract DbBeanLanguage getLanguage(final HttpSession session);

    protected String submitForm(HttpServletRequest request) throws ServletException {
        return processBean(new HttpRequestParameters(request), getHTMLView(getSubmitBeanId(request), request));
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
        return deleteBean(getInstance(getBeanId(request, "id")));
    }

    protected abstract DbBeanInterface getInstance(final long id);

    protected String changeOrder(final HttpServletRequest request) throws ServletException {
        final long id = getBeanId(request, "id");

        final ChangeOrderDirection direction = getChangeOrderDirection(request);
        final long companionId = Strings.getLongVal(request.getParameter("companionId"));

        return changeOrder(id, direction, companionId);
    }

    protected abstract String changeOrder(final long id, final ChangeOrderDirection direction, final long companionId);
}
