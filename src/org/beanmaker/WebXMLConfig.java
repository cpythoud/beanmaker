package org.beanmaker;

import org.dbbeans.util.Strings;

import org.jcodegen.html.xmlbase.XMLElement;

public class WebXMLConfig {

    private static final String XML_CONFIG_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<web-app xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "\t\txmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
            "\t\txmlns:web=\"http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\"\n" +
            "\t\txsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\"\n" +
            "\t\tversion=\"3.0\"\n" +
            "\t\txmlns=\"http://caucho.com/ns/resin\"\n" +
            "\t\txmlns:resin=\"http://caucho.com/ns/resin/core\">\n\n";

    private static final String XML_CONFIG_SUFFIX = "\n</web-app>\n";

    private static final String URL_START = "/Control";

    private final String beanName;
    private final String packageName;

    public WebXMLConfig(final String beanName, final String packageName) {
        this.beanName = beanName;
        this.packageName = packageName;
    }

    private XMLElement getServletConfigXMLElement() {
        final XMLElement servlet = new XMLElement("servlet", 1);
        servlet.addChild(getOneLiner("servlet-name", getServletName()));
        servlet.addChild(getOneLiner("servlet-class", packageName + "." + beanName + "Servlet"));
        return servlet;
    }

    private XMLElement getOneLiner(final String name, final String value) {
        final XMLElement element = new XMLElement(name, value);
        element.setOnOneLine(true);
        return element;
    }

    private String getServletName() {
        return Strings.replace(packageName, ".", "-") + "-" + beanName;
    }

    private XMLElement getServletMappingConfigXMLElement() {
        final XMLElement servletMapping = new XMLElement("servlet-mapping", 1);
        servletMapping.addChild(getOneLiner("servlet-name", getServletName()));
        servletMapping.addChild(getOneLiner("url-pattern", getURLPattern()));
        return servletMapping;
    }

    private String getURLPattern() {
        String[] parts = packageName.split("\\.");
        StringBuilder pattern = new StringBuilder();
        pattern.append(URL_START);
        for (int index = 2; index < parts.length; ++index)
            pattern.append("/").append(Strings.capitalize(parts[index]));
        pattern.append("/").append(beanName);

        return pattern.toString();
    }

    public String getFilename() {
        return "web-" + getServletName() + ".xml";
    }

    public String getSourceCode() {
        return XML_CONFIG_PREFIX + getServletConfigXMLElement().toString() + "\n"
                + getServletMappingConfigXMLElement().toString() + XML_CONFIG_SUFFIX;
    }

}
