package io.ebeanservice.docstore.api.mapping;

import io.ebeaninternal.server.json.PathStack;

/**
 * Adapter for DocPropertyVisitor that does not do anything.
 * Used to extend and implement only the desired methods.
 */
public abstract class DocPropertyAdapter implements DocPropertyVisitor {

  protected final PathStack pathStack = new PathStack();

  @Override
  public void visitProperty(DocPropertyMapping property) {
    // do nothing
  }

  @Override
  public void visitBegin() {
    // do nothing
  }

  @Override
  public void visitEnd() {
    // do nothing
  }

  @Override
  public void visitBeginObject(DocPropertyMapping property) {
    pathStack.push(property.name());
  }

  @Override
  public void visitEndObject(DocPropertyMapping property) {
    pathStack.pop();
  }

  @Override
  public void visitBeginList(DocPropertyMapping property) {
    pathStack.push(property.name());
  }

  @Override
  public void visitEndList(DocPropertyMapping property) {
    pathStack.pop();
  }
}
