package org.integra;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class ValidatorErrorHandler extends DefaultHandler {
    private final List<String> validationErrors = new ArrayList<>();

    @Override
    public void error(SAXParseException e) throws SAXException {
        validationErrors.add("Error at line " + e.getLineNumber() + ": " + e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        validationErrors.add("Fatal error at line " + e.getLineNumber() + ": " + e.getMessage());
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        validationErrors.add("Warning at line " + e.getLineNumber() + ": " + e.getMessage());
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
