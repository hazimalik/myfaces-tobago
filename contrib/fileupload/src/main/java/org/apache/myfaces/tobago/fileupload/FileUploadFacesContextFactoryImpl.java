package org.apache.myfaces.tobago.fileupload;

/*
 * Copyright 2002-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.myfaces.tobago.webapp.TobagoMultipartFormdataRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

/*
 * Created by IntelliJ IDEA.
 * User: bommel
 * Date: Oct 8, 2006
 * Time: 4:45:47 PM
 */
public class FileUploadFacesContextFactoryImpl extends FacesContextFactory {
  private static final Log LOG = LogFactory.getLog(FileUploadFacesContextFactoryImpl.class);
  private FacesContextFactory facesContextFactory;
  private String repositoryPath = System.getProperty("java.io.tmpdir");
  private long maxSize = TobagoMultipartFormdataRequest.ONE_MB;

  public FileUploadFacesContextFactoryImpl(FacesContextFactory facesContextFactory) {
    // TODO get Configuration from env entries in the web.xml
    this.facesContextFactory = facesContextFactory;
    if (LOG.isDebugEnabled()) {
      LOG.debug("Wrap FacesContext for file upload");
    }
    try {
      InitialContext ic = new InitialContext();
      Context ctx = (Context) ic.lookup("java:comp/env");

      try {
        String repositoryPath = (String) ctx.lookup("uploadRepositoryPath");
        if (repositoryPath != null) {
          File file = new File(repositoryPath);
          if (!file.exists()) {
            LOG.error("Given repository Path for " + getClass().getName() + " " + repositoryPath + " doesn't exists");
          } else if (!file.isDirectory()) {
            LOG.error("Given repository Path for " + getClass().getName() + " " + repositoryPath + " is not a directory");
          } else {
            this.repositoryPath = repositoryPath;
          }
        }
      } catch (NamingException ne) {
        // ignore
      }

      try {
        maxSize = TobagoMultipartFormdataRequest.getMaxSize((String) ctx.lookup("uploadMaxFileSize"));
      } catch (NamingException ne) {
        // ignore
      }
    } catch (NamingException e) {
      LOG.error("Error getting env-entry", e);
    }
    LOG.info("Configure uploadMaxFileSize for "+ getClass().getName() + " to "+ this.maxSize);
    LOG.info("Configure uploadRepositryPath for "+ getClass().getName() + " to "+ this.repositoryPath);
  }

  public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
      throws FacesException {
    if (request instanceof HttpServletRequest && !(request instanceof TobagoMultipartFormdataRequest)) {
      String contentType = ((HttpServletRequest) request).getContentType();
      if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Wrap HttpServletRequest for file upload");
        }
        try {
          request = new TobagoMultipartFormdataRequest((HttpServletRequest) request, repositoryPath, maxSize);
        } catch (FacesException e) {
          LOG.error("", e);
          FacesContext facesContext = facesContextFactory.getFacesContext(context, request, response, lifecycle);
          // TODO  better Message i18n Message?
          FacesMessage facesMessage = new FacesMessage(e.getCause().getMessage());
          facesContext.addMessage(null, facesMessage);
          facesContext.renderResponse();
          return facesContext;
        }
      }
    }
    return facesContextFactory.getFacesContext(context, request, response, lifecycle);
  }
}
