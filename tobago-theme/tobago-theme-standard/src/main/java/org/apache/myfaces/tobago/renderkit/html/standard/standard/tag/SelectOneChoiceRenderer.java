/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.myfaces.tobago.renderkit.html.standard.standard.tag;

import org.apache.myfaces.tobago.component.UISelectOneChoice;
import org.apache.myfaces.tobago.renderkit.css.Classes;
import org.apache.myfaces.tobago.renderkit.html.BootstrapClass;
import org.apache.myfaces.tobago.renderkit.html.HtmlAttributes;
import org.apache.myfaces.tobago.renderkit.html.HtmlElements;
import org.apache.myfaces.tobago.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.tobago.renderkit.util.SelectItemUtils;
import org.apache.myfaces.tobago.util.ComponentUtils;
import org.apache.myfaces.tobago.webapp.TobagoResponseWriter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;

public class SelectOneChoiceRenderer extends SelectOneRendererBase {

  public boolean getRendersChildren() {
    return true;
  }

  protected void encodeBeginField(FacesContext facesContext, UIComponent component) throws IOException {
    final UISelectOneChoice select = (UISelectOneChoice) component;
    final TobagoResponseWriter writer = HtmlRendererUtils.getTobagoResponseWriter(facesContext);

    final String id = select.getClientId(facesContext);
    final Iterable<SelectItem> items = SelectItemUtils.getItemIterator(facesContext, select);
    final String title = HtmlRendererUtils.getTitleFromTipAndMessages(facesContext, select);
    final boolean disabled = !items.iterator().hasNext() || select.isDisabled() || select.isReadonly();

    writer.startElement(HtmlElements.SELECT, select);
    writer.writeNameAttribute(id);
    writer.writeIdAttribute(id);
    HtmlRendererUtils.writeDataAttributes(facesContext, writer, select);
    writer.writeAttribute(HtmlAttributes.DISABLED, disabled);
    final Integer tabIndex = select.getTabIndex();
    if (tabIndex != null) {
      writer.writeAttribute(HtmlAttributes.TABINDEX, tabIndex);
    }
    writer.writeStyleAttribute(select.getStyle());
    writer.writeClassAttribute(Classes.create(select), BootstrapClass.FORM_CONTROL);
    if (title != null) {
      writer.writeAttribute(HtmlAttributes.TITLE, title, true);
    }
    HtmlRendererUtils.renderCommandFacet(select, facesContext, writer);
    HtmlRendererUtils.renderFocus(id, select.isFocus(), ComponentUtils.isError(select), facesContext, writer);

    HtmlRendererUtils.renderSelectItems(select, items, select.getValue(), (String) select.getSubmittedValue(), writer,
        facesContext);

  }

  protected void encodeEndField(FacesContext facesContext, UIComponent component) throws IOException {
    final TobagoResponseWriter writer = HtmlRendererUtils.getTobagoResponseWriter(facesContext);
    writer.endElement(HtmlElements.SELECT);
  }
}
