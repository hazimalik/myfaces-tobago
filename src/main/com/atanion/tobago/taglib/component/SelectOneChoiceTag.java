/*
 * Copyright (c) 2002 Atanion GmbH, Germany. All rights reserved.
 * Created: Aug 13, 2002 3:04:03 PM
 * $Id$
 */
package com.atanion.tobago.taglib.component;

import com.atanion.tobago.component.UISelectOne;
import com.atanion.tobago.component.ComponentUtil;
import com.atanion.tobago.taglib.decl.HasBinding;
import com.atanion.tobago.taglib.decl.HasId;
import com.atanion.tobago.taglib.decl.HasLabelAndAccessKey;
import com.atanion.tobago.taglib.decl.HasOnchangeListener;
import com.atanion.tobago.taglib.decl.HasTip;
import com.atanion.tobago.taglib.decl.HasValue;
import com.atanion.tobago.taglib.decl.IsDisabled;
import com.atanion.tobago.taglib.decl.IsInline;
import com.atanion.tobago.taglib.decl.IsReadonly;
import com.atanion.tobago.taglib.decl.IsRendered;
import com.atanion.util.annotation.BodyContentDescription;
import com.atanion.util.annotation.Tag;

import javax.servlet.jsp.JspException;
import javax.faces.component.UIComponent;

@Tag(name="selectOneChoice")

/**
 * Render a single selection dropdown list.
 */
@BodyContentDescription(anyTagOf="(<f:selectItems>|<f:selectItem>|<t:selectItem>)+ <f:facet>* " )
public class SelectOneChoiceTag extends SelectOneTag
    implements HasId, HasValue, IsDisabled, IsReadonly, HasOnchangeListener, IsInline,
    HasLabelAndAccessKey, IsRendered, HasBinding, HasTip
    {

  public String getComponentType() {
    return UISelectOne.COMPONENT_TYPE;
  }

  public int doEndTag() throws JspException {

    UIComponent component = getComponentInstance();
    if (component.getFacet(FACET_LAYOUT) == null) {
      UIComponent layout = ComponentUtil.createLabeledInputLayoutComponent();
      component.getFacets().put(FACET_LAYOUT, layout);
    }
    return super.doEndTag();
  }
}
