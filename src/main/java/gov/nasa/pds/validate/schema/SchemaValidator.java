// Copyright © 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// • Redistributions of source code must retain the above copyright notice,
//   this list of conditions and the following disclaimer.
// • Redistributions must reproduce the above copyright notice, this list of
//   conditions and the following disclaimer in the documentation and/or other
//   materials provided with the distribution.
// • Neither the name of Caltech nor its operating division, the Jet Propulsion
//   Laboratory, nor the names of its contributors may be used to endorse or
//   promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package gov.nasa.pds.validate.schema;

import gov.nasa.pds.tools.label.CachedLSResourceResolver;
import gov.nasa.pds.tools.label.ExceptionType;
import gov.nasa.pds.tools.label.LabelErrorHandler;
import gov.nasa.pds.tools.validate.ProblemContainer;
import gov.nasa.pds.tools.validate.ProblemDefinition;
import gov.nasa.pds.tools.validate.ProblemType;
import gov.nasa.pds.tools.validate.ValidationProblem;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * Class to validate schemas.
 *
 * @author mcayanan
 *
 */
public class SchemaValidator {
  /**
   * Schema factory.
   */
  private SchemaFactory schemaFactory;

  /**
   * Constructor.
   *
   */
  public SchemaValidator() {
    // Support for XSD 1.1
    schemaFactory = SchemaFactory
        .newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
    schemaFactory.setResourceResolver(new CachedLSResourceResolver());
  }

  /**
   * Validate the given schema.
   *
   * @param schema URL of the schema.
   *
   * @return An ExceptionContainer that contains any problems
   * that were found during validation.
   */
  public ProblemContainer validate(StreamSource schema) {
    ProblemContainer container = new ProblemContainer();
    schemaFactory.setErrorHandler(new LabelErrorHandler(container));
    CachedLSResourceResolver resolver =
        (CachedLSResourceResolver) schemaFactory.getResourceResolver();
    resolver.setProblemHandler(container);
    try {
      schemaFactory.newSchema(schema);
    } catch (SAXException se) {
      if ( !(se instanceof SAXParseException) ) {
        URL schemaUrl = null;
        try {
          schemaUrl = new URL(schema.toString());
        } catch (MalformedURLException e) {
          //Ignore. Should not happen!!! 
        }
        ValidationProblem problem = new ValidationProblem(
            new ProblemDefinition(ExceptionType.FATAL,
                ProblemType.SCHEMA_ERROR,
                se.getMessage()), 
            schemaUrl);
        container.addProblem(problem);
      }
    }
    return container;
  }

  public void setExternalLocations(String locations)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    schemaFactory.setProperty(
        "http://apache.org/xml/properties/schema/external-schemaLocation",
         locations);
  }

  public CachedLSResourceResolver getCachedLSResolver() {
    return (CachedLSResourceResolver) schemaFactory.getResourceResolver();
  }
}
