/*
 * Copyright (c) 2002 Atanion GmbH, Germany
 * All rights reserved. Created 14.08.2002 at 14:39:25.
 * $Id$
 */
package com.atanion.tobago.taglib.component;

import static com.atanion.tobago.TobagoConstants.FACET_LAYOUT;
import com.atanion.tobago.component.ComponentUtil;
import com.atanion.tobago.component.UIInput;
import com.atanion.tobago.component.UIPage;
import com.atanion.tobago.component.UILabeledInputLayout;
import com.atanion.tobago.taglib.decl.HasIdBindingAndRendered;
import com.atanion.tobago.taglib.decl.HasLabelAndAccessKey;
import com.atanion.tobago.taglib.decl.HasOnchangeListener;
import com.atanion.tobago.taglib.decl.HasTip;
import com.atanion.tobago.taglib.decl.IsDisabled;
import com.atanion.tobago.TobagoConstants;
import com.atanion.util.annotation.Tag;
import com.atanion.util.annotation.TagAttribute;
import com.atanion.util.annotation.UIComponentTagAttribute;

import javax.servlet.jsp.JspException;
import javax.faces.component.UIComponent;

/**
 * Renders a file input field. 
 */
@Tag(name="file")
public class FileTag extends InputTag
    implements HasIdBindingAndRendered, IsDisabled,
               HasLabelAndAccessKey, HasOnchangeListener, HasTip {
  // ----------------------------------------------------------- business methods

  public int doStartTag() throws JspException {
    int result = super.doStartTag();
    UIPage form = ComponentUtil.findPage(getComponentInstance());
    form.getAttributes().put(ATTR_ENCTYPE, "multipart/form-data");
    return result;
  }

  public int doEndTag() throws JspException {
    UIComponent component = getComponentInstance();
    if (component.getFacet(FACET_LAYOUT) == null) {
      UIComponent layout = ComponentUtil.createLabeledInputLayoutComponent();
      component.getFacets().put(FACET_LAYOUT, layout);
    }
    return super.doEndTag();
  }

  public String getComponentType() {
    return UIInput.COMPONENT_TYPE;
  }

  /**
   * Value binding expression pointing to a
   * <code>org.apache.commons.fileupload.FileItem</code> property to store the
   * uploaded file.
   */
  @TagAttribute
  @UIComponentTagAttribute(type={"org.apache.commons.fileupload.FileItem"})
  public void setValue(String value) {
    super.setValue(value);
  }
}

