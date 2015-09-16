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

package org.apache.myfaces.tobago.internal.component;

import org.apache.myfaces.tobago.component.Attributes;
import org.apache.myfaces.tobago.component.DeprecatedDimension;
import org.apache.myfaces.tobago.component.Facets;
import org.apache.myfaces.tobago.internal.util.FacesContextUtils;
import org.apache.myfaces.tobago.layout.LayoutContainer;
import org.apache.myfaces.tobago.layout.LayoutManager;
import org.apache.myfaces.tobago.layout.Measure;

import javax.el.ValueExpression;
import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Iterator;

public abstract class AbstractUIPopup extends AbstractUIPanel
    implements NamingContainer, DeprecatedDimension, LayoutContainer {

  private boolean activated;

  public void setActivated(final boolean activated) {
    this.activated = activated;
  }

  @Override
  public void processDecodes(final FacesContext facesContext) {
    if (isSubmitted()) {
      for (final Iterator it = getFacetsAndChildren(); it.hasNext();) {
        final UIComponent childOrFacet = (UIComponent) it.next();
        childOrFacet.processDecodes(facesContext);
      }
      try {
        decode(facesContext);
      } catch (final RuntimeException e) {
        facesContext.renderResponse();
        throw e;
      }
      if (facesContext.getRenderResponse()) {
        setActivated(true);
      }
    }
  }

  @Override
  public boolean isRendered() {
    final ValueExpression expression = getValueExpression("rendered");
    if (expression != null) {
      final FacesContext context = FacesContext.getCurrentInstance();
      return (Boolean) expression.getValue(context.getELContext());
    } else {
      return isActivated() || isRedisplay();
    }
  }

  private boolean isSubmitted() {
    final FacesContext facesContext = getFacesContext();
    final String action = FacesContextUtils.getActionId(facesContext);
    return action != null && action.startsWith(
        getClientId(facesContext) + UINamingContainer.getSeparatorChar(facesContext));
  }

  private boolean isRedisplay() {
    if (isSubmitted()) {
      final String action = FacesContextUtils.getActionId(getFacesContext());
      if (action != null) {
        final UIComponent command = getFacesContext().getViewRoot().findComponent(
            UINamingContainer.getSeparatorChar(getFacesContext()) + action);
        if (command != null && command instanceof UICommand) {
          return command.getAttributes().get(Attributes.POPUP_CLOSE) == null;
        }
      }
    }
    return false;
  }

  private boolean isActivated() {
    return activated;
  }


  @Override
  public void processValidators(final FacesContext context) {
    if (isSubmitted()) {
      for (final Iterator it = getFacetsAndChildren(); it.hasNext();) {
        final UIComponent childOrFacet = (UIComponent) it.next();
        childOrFacet.processValidators(context);
      }
      //TODO: check if validation has failed and reset rendered if needed
      if (context.getRenderResponse()) {
        setActivated(true);
      }
    }
  }

  @Override
  public void processUpdates(final FacesContext context) {
    if (isSubmitted()) {
      for (final Iterator it = getFacetsAndChildren(); it.hasNext();) {
        final UIComponent childOrFacet = (UIComponent) it.next();
        childOrFacet.processUpdates(context);
      }
    }
  }

  @Override
  public Object saveState(final FacesContext context) {
    final Object[] saveState = new Object[2];
    saveState[0] = super.saveState(context);
    saveState[1] = activated;
    return saveState;
  }

  @Override
  public void restoreState(final FacesContext context, final Object savedState) {
    final Object[] values = (Object[]) savedState;
    super.restoreState(context, values[0]);
    activated = (Boolean) values[1];
  }

  @Override
  public void encodeEnd(final FacesContext context) throws IOException {
    super.encodeEnd(context);
    activated = false;
  }

  public LayoutManager getLayoutManager() {
    return (LayoutManager) getFacet(Facets.LAYOUT);
  }

  public void setLayoutManager(final LayoutManager layoutManager) {
    getFacets().put(Facets.LAYOUT, (AbstractUILayoutBase) layoutManager);
  }

  public boolean isLayoutChildren() {
    return isRendered();
  }
  
  public abstract Measure getWidth();

  public abstract void setWidth(Measure width);

  public abstract Measure getHeight();

  public abstract void setHeight(Measure height);
}
