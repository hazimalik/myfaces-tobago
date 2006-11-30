package org.apache.myfaces.tobago.renderkit.html.scarborough.standard.tag;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created 07.02.2003 16:00:00.
 * $Id$
 */

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_ACTION_LINK;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_DISABLED;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_LABEL;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_ONCLICK;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_MUTABLE;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_SELECTABLE;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_TIP;
import org.apache.myfaces.tobago.component.ComponentUtil;
import org.apache.myfaces.tobago.component.UITree;
import org.apache.myfaces.tobago.component.UITreeNode;
import org.apache.myfaces.tobago.component.UITreeNodes;
import org.apache.myfaces.tobago.model.TreeState;
import org.apache.myfaces.tobago.renderkit.CommandRendererBase;

import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.util.Map;

public class TreeNodeRenderer extends CommandRendererBase {

  private static final Log LOG = LogFactory.getLog(TreeNodeRenderer.class);

  @Override
  public void decode(FacesContext facesContext, UIComponent component) {

    super.decode(facesContext, component);

    if (ComponentUtil.isOutputOnly(component)) {
      return;
    }

    UITreeNode node = (UITreeNode) component;
    UITree tree = findTree(node);
    TreeState state = tree.getState();
    String treeId = tree.getClientId(facesContext);
    String nodeId = node.getId();
    UIComponent parent = node.getParent();
    if (parent != null && parent instanceof UITreeNodes) { // todo cleanup
      nodeId += ((UITreeNodes) parent).getCurrentNodeId();
    }
    Map requestParameterMap
        = facesContext.getExternalContext().getRequestParameterMap();

    // expand state
    String expandState = (String) requestParameterMap.get(treeId);
    String searchString = ";" + nodeId + ";";
    if (expandState.indexOf(searchString) > -1) {
      state.addExpandState((DefaultMutableTreeNode) node.getValue());
    }


    if (TreeRenderer.isSelectable(tree)) { // selection
      String selected = (String) requestParameterMap.get(treeId + UITree.SELECT_STATE);
      searchString = ";" + nodeId + ";";
      if (selected.indexOf(searchString) > -1) {
        state.addSelection((DefaultMutableTreeNode) node.getValue());
      }
    }

    // marker
    String marked = (String) requestParameterMap.get(treeId + UITree.MARKER);
    if (marked != null) {
      searchString = treeId + NamingContainer.SEPARATOR_CHAR + nodeId;

      if (marked.equals(searchString)) {
        state.setMarker((DefaultMutableTreeNode) node.getValue());
      }
    }


    // link
    // FIXME: this is equal to the CommandRendererBase, why not use that code?
    String actionId = ComponentUtil.findPage(component).getActionId();
    if (LOG.isDebugEnabled()) {
      LOG.debug("actionId = '" + actionId + "'");
      LOG.debug("nodeId = '" + treeId + NamingContainer.SEPARATOR_CHAR
          + nodeId + "'");
    }
    if (actionId != null
        && actionId.equals(treeId + NamingContainer.SEPARATOR_CHAR + nodeId)) {
      UICommand treeNodeCommand
          = (UICommand) tree.getFacet(UITree.FACET_TREE_NODE_COMMAND);
      if (treeNodeCommand != null) {
        UIParameter parameter = ensureTreeNodeParameter(treeNodeCommand);
        parameter.setValue(node.getId());
//        LOG.error("no longer supported: treeNodeCommand.fireActionEvent(facesContext));");
//        treeNodeCommand.fireActionEvent(facesContext); // FIXME jsfbeta
//        component.queueEvent(new ActionEvent(component));
        treeNodeCommand.queueEvent(new ActionEvent(treeNodeCommand));
      }

      UIForm form = ComponentUtil.findForm(component);
      if (form != null) {
        form.setSubmitted(true);
        if (LOG.isDebugEnabled()) {
          LOG.debug("setting Form Active: " + form.getClientId(facesContext));
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("actionId: " + actionId);
        LOG.debug("nodeId: " + nodeId);
      }
    }

//    node.setValid(true);
  }

  private UIParameter ensureTreeNodeParameter(UICommand command) {
    UIParameter treeNodeParameter = null;
    for (Object o : command.getChildren()) {
      UIComponent component = (UIComponent) o;
      if (component instanceof UIParameter) {
        UIParameter parameter = (UIParameter) component;
        if (parameter.getName().equals(UITree.PARAMETER_TREE_NODE_ID)) {
          treeNodeParameter = parameter;
        }
      }
    }
    if (treeNodeParameter == null) {
      treeNodeParameter = new UIParameter();
      treeNodeParameter.setName(UITree.PARAMETER_TREE_NODE_ID);
    }
    return treeNodeParameter;
  }

  @Override
  public void encodeBegin(FacesContext facesContext, UIComponent component)
      throws IOException {

    UITreeNode treeNode = (UITreeNode) component;

    UIComponent parent = treeNode.getParent();
    String pos = null;

    boolean isFolder = treeNode.getChildCount() > 0;

    String parentClientId = null;
    if (parent != null && parent instanceof UITreeNode) { // if not the root node
      parentClientId = treeNode.getParent().getClientId(facesContext);
    } else if (parent != null && parent instanceof UITreeNodes) {
      pos = ((UITreeNodes) parent).getCurrentNodeId();
      if ("_0".equals(pos)) {
        UIComponent superParent = parent.getParent();
        parentClientId = superParent.getClientId(facesContext);
      } else {
        parentClientId = treeNode.getClientId(facesContext);
        parentClientId += ((UITreeNodes) parent).getCurrentParentNodeId();
      }
      DefaultMutableTreeNode currentNode =
          ((UITreeNodes) parent).getCurrentNode();
      if (currentNode != null) {
        isFolder = currentNode.getChildCount() > 0;
      }
    }

    UITree root = findTree(treeNode);
    String rootId = root.getClientId(facesContext);

    String clientId = treeNode.getClientId(facesContext);
    clientId += pos != null ? pos : "";

    String jsClientId = TreeRenderer.createJavascriptVariable(clientId);
    String jsParentClientId = TreeRenderer.createJavascriptVariable(
        parentClientId);
//  rootId = HtmlUtils.createJavascriptVariable(rootId);

    TreeState treeState = root.getState();
    if (treeState == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("No treeState found. clientId=" + clientId);
      }
    } else {

      DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeNode.getValue();

      ResponseWriter writer = facesContext.getResponseWriter();

      String debuging = null;

      writer.writeText("  var ", null);
      writer.writeText(jsClientId, null);
      writer.writeText(" = new TreeNode('", null);
      // label
      Object name = treeNode.getAttributes().get(ATTR_LABEL);
      if (LOG.isDebugEnabled()) {
        debuging += name + " : ";
      }
      if (name != null) {
        writer.writeText(StringEscapeUtils.escapeJavaScript(name.toString()), null);
      } else {
        LOG.warn("name = null");
      }
      writer.writeText("',", null);

      // tip
      Object tip = treeNode.getAttributes().get(ATTR_TIP);
      if (tip != null) {
        tip = StringEscapeUtils.escapeJavaScript(tip.toString());
        writer.writeText("'", null);
        writer.writeText((String) tip, null);
        writer.writeText("','", null);
      } else {
        writer.writeText("null,'", null);
      }

      // id
      writer.writeText(clientId, null);
      writer.writeText("','", null);

      writer.writeText(root.getMode(), null);
      writer.writeText("',", null);

      // is folder
      writer.writeText(isFolder, null);
      writer.writeText(",", null);
      writer.writeText(Boolean.toString(!root.isShowIcons()), null);
      writer.writeText(",", null);
      writer.writeText(Boolean.toString(!root.isShowJunctions()), null);
      writer.writeText(",", null);
      writer.writeText(Boolean.toString(!root.isShowRootJunction()), null);
      writer.writeText(",", null);
      writer.writeText(Boolean.toString(!root.isShowRoot()), null);
      writer.writeText(",'", null);
      writer.writeText(rootId, null);
      writer.writeText("',", null);
      String selectable = ComponentUtil.getStringAttribute(root, ATTR_SELECTABLE);
      if (selectable != null
          && (!(selectable.equals("multi") || selectable.equals("multiLeafOnly")
          || selectable.equals("single") || selectable.equals("singleLeafOnly")
          || selectable.equals("sibling") || selectable.equals("siblingLeafOnly")))) {
        selectable = null;
      }
      if (selectable != null) {
        writer.writeText("'", null);
        writer.writeText(selectable, null);
        writer.writeText("'", null);
      } else {
        writer.writeText("false", null);
      }
      writer.writeText(",", null);
      writer.writeText(Boolean.toString(ComponentUtil.getBooleanAttribute(root,
          ATTR_MUTABLE)), null);
      writer.writeText(",'", null);
      writer.writeText(
          ComponentUtil.findPage(treeNode).getFormId(facesContext), null);
      writer.writeText("',", null);
      if (treeNode.getChildCount() == 0
          || (selectable != null && !selectable.endsWith("LeafOnly"))) {
        boolean selected = treeState.isSelected(node);
        writer.writeText(Boolean.toString(selected), null);
        if (LOG.isDebugEnabled()) {
          debuging += selected ? "S" : "-";
        }
      } else {
        writer.writeText("false", null);
        if (LOG.isDebugEnabled()) {
          debuging += "-";
        }
        if (treeState.isSelected(node)) {
          LOG.warn("Ignore selected FolderNode in LeafOnly selection tree!");
        }
      }
      writer.writeText(",", null);
      writer.writeText(Boolean.toString(treeState.isMarked(node)), null);
      writer.writeText(",", null);
      // expanded
      boolean expanded = treeState.isExpanded(node);
      writer.writeText(Boolean.toString(expanded), null);
      if (LOG.isDebugEnabled()) {
        debuging += expanded ? "E" : "-";
      }
      writer.writeText(",", null);

      // required
      writer.writeText(Boolean.toString(root.isRequired()), null);
      writer.writeText(",", null);

      // disabled
      writer.writeText(ComponentUtil.getBooleanAttribute(treeNode, ATTR_DISABLED), null);
      writer.writeText(",", null);

      // resources
      writer.writeText("treeResourcesHelp,", null);

      // action link
      String actionLink =
          (String) treeNode.getAttributes().get(ATTR_ACTION_LINK);
      if (actionLink != null) {
        writer.writeText("'", null);
        writer.writeText(actionLink, null);
        writer.writeText("',", null);
      } else {
        writer.writeText("null,", null);
      }

      // onclick
      String onclick = (String) treeNode.getAttributes().get(ATTR_ONCLICK);
      if (onclick != null) {
        writer.writeText("'", null);
        onclick = onclick.replaceAll("\\'", "\\\\'");
        writer.writeText(onclick, null);
        writer.writeText("',", null);
      } else {
        writer.writeText("null,", null);
      }

      // parent
      if (jsParentClientId != null) {
        writer.writeText(jsParentClientId, null);
      } else {
        writer.writeText("null", null);
      }
      writer.writeText(",", null);

      // icon (not implemented)
      writer.writeText("null", null);
      writer.writeText(",", null);

      // open folder icon (not implemented)
      writer.writeText("null", null);
      writer.writeText(", '", null);

      // width
      writer.writeText("300", null);
      writer.writeText("');\n", null);

/*
      if (jsParentClientId != null) { // if not the root node
        writer.writeText("  ", null);
        writer.writeText(jsParentClientId, null);
        writer.writeText(".add(", null);
        writer.writeText(jsClientId, null);
        writer.writeText(");\n", null);
      }
*/
      if (LOG.isDebugEnabled()) {
        LOG.debug(debuging);
      }
    }
  }

  private UITree findTree(UIComponent component) {
    while (component != null) {
      if (component instanceof UITree) {
        return (UITree) component;
      }
      component = component.getParent();
    }
    return null;
  }
}
