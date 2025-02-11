// Copyright 2006-2019, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id$
package gov.nasa.pds.tools.validate.rule.pds4;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import gov.nasa.pds.tools.label.ExceptionType;
import gov.nasa.pds.tools.label.SourceLocation;
import gov.nasa.pds.tools.util.Utility;
import gov.nasa.pds.tools.validate.ProblemDefinition;
import gov.nasa.pds.tools.validate.ProblemType;
import gov.nasa.pds.tools.validate.ValidationProblem;
import gov.nasa.pds.tools.validate.ValidationTarget;
import gov.nasa.pds.tools.validate.rule.AbstractValidationRule;
import gov.nasa.pds.tools.validate.rule.ValidationTest;

/**
 * This class checks that local identifiers referenced in a label exist in that label.
 *
 * @author mcayanan
 *
 */
public class LocalIdentifierReferencesRule extends AbstractValidationRule {
  private static final String PDS4_NS = "http://pds.nasa.gov/pds4/pds/v1";

  private static final String LOCAL_IDENTIFIER_REF_PATH =
      "//*:local_identifier_reference[namespace-uri()='" + PDS4_NS + "']";

  private static final String LOCAL_IDENTIFIER_PATH =
      "//*:local_identifier[namespace-uri()='" + PDS4_NS + "']";

  private XPathFactory xPathFactory;

  public LocalIdentifierReferencesRule() {
    xPathFactory = new net.sf.saxon.xpath.XPathFactoryImpl();
  }

  @Override
  public boolean isApplicable(String location) {
    if (Utility.isDir(location) || !Utility.canRead(location)
        || !getContext().containsKey(PDS4Context.LABEL_DOCUMENT)) {
      return false;
    }

    Matcher matcher = getContext().getLabelPattern().matcher(FilenameUtils.getName(location));
    return matcher.matches();
  }

  @ValidationTest
  public void validateLocalIdentifiers() {
    List<ValidationProblem> problems = new ArrayList<>();
    ValidationTarget target = new ValidationTarget(getTarget());
    Document label = getContext().getContextValue(PDS4Context.LABEL_DOCUMENT, Document.class);
    DOMSource source = new DOMSource(label);

    NodeList localIdRefs = null;
    try {
      localIdRefs = (NodeList) xPathFactory.newXPath().evaluate(LOCAL_IDENTIFIER_REF_PATH, source,
          XPathConstants.NODESET);
    } catch (XPathExpressionException xe) {
      ProblemDefinition pd = new ProblemDefinition(ExceptionType.ERROR, ProblemType.INTERNAL_ERROR,
          "Error while finding local_identifier_reference attributes using XPath '"
              + LOCAL_IDENTIFIER_REF_PATH + "': " + xe.getMessage());
      getListener().addProblem(new ValidationProblem(pd, new ValidationTarget(getTarget())));
      return;
    }
    NodeList localIds = null;
    try {
      localIds = (NodeList) xPathFactory.newXPath().evaluate(LOCAL_IDENTIFIER_PATH, source,
          XPathConstants.NODESET);
    } catch (XPathExpressionException xe) {
      ProblemDefinition pd = new ProblemDefinition(ExceptionType.ERROR, ProblemType.INTERNAL_ERROR,
          "Error while finding local_identifier attributes using XPath '" + LOCAL_IDENTIFIER_PATH
              + "': " + xe.getMessage());
      getListener().addProblem(new ValidationProblem(pd, new ValidationTarget(getTarget())));
      return;
    }

    for (int i = 0; i < localIdRefs.getLength(); i++) {
      boolean found = false;
      SourceLocation locator =
          (SourceLocation) localIdRefs.item(i).getUserData(SourceLocation.class.getName());
      for (int j = 0; j < localIds.getLength(); j++) {
        if (localIdRefs.item(i).getTextContent().equals(localIds.item(j).getTextContent())) {
          SourceLocation idLocator =
              (SourceLocation) localIds.item(j).getUserData(SourceLocation.class.getName());
          ProblemDefinition def =
              new ProblemDefinition(ExceptionType.INFO, ProblemType.LOCAL_ID_FOUND,
                  "Referenced Local Identifier '" + localIdRefs.item(i).getTextContent()
                      + "' found in label on line '" + idLocator.getLineNumber() + "'.");
          problems.add(new ValidationProblem(def, target, locator.getLineNumber(), -1));
          found = true;
          break;
        }
      }
      if (!found) {
        ProblemDefinition def =
            new ProblemDefinition(ExceptionType.ERROR, ProblemType.LOCAL_IDENTIFIER_NOT_FOUND,
                "Referenced Local Identifier '" + localIdRefs.item(i).getTextContent()
                    + "' does not exist in any of the local_identifier attributes in the label.");
        problems.add(new ValidationProblem(def, target, locator.getLineNumber(), -1));
      }
    }

    // Add the problems to the exception container.
    for (ValidationProblem problem : problems) {
      getListener().addProblem(problem);
    }
  }
}
